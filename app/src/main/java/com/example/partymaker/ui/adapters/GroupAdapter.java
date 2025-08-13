package com.example.partymaker.ui.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.partymaker.R;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.utils.business.sharing.ContentSharingManager;
import com.example.partymaker.utils.media.GlideImageLoader;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Adapter for displaying groups in a RecyclerView. Uses the OptimizedRecyclerAdapter base class for
 * efficient updates.
 */
public class GroupAdapter extends OptimizedRecyclerAdapter<Group, GroupAdapter.GroupViewHolder> {

  private static final String TAG = "GroupAdapter";
  private static final long FIREBASE_TIMEOUT_MS = 3000L;
  private static final float NORMAL_ALPHA = 1.0f;
  private static final float NORMAL_SCALE = 1.0f;
  private static final float ZERO_TRANSLATION = 0f;
  private static final float ZERO_ROTATION = 0f;
  private static final String DATE_FORMAT = "%s %s %s    %s";
  private static final String OBJECT_NOT_EXIST_ERROR = "Object does not exist";
  private static final String PRIMARY_STORAGE_PATH = "UsersImageProfile/Groups/";
  private static final String FALLBACK_STORAGE_PATH = "Groups/";
  private final Context context;
  private final OnGroupClickListener listener;

  /**
   * Constructs a GroupAdapter.
   *
   * @param context The context
   * @param listener The click listener
   */
  public GroupAdapter(Context context, OnGroupClickListener listener) {
    this.context = context;
    this.listener = listener;
  }

  @NonNull
  @Override
  public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.item_group, parent, false);
    // Reduce overdraw by setting background only where needed
    view.setWillNotDraw(false);
    // Disable child clipping for better performance
    if (view instanceof ViewGroup) {
      ((ViewGroup) view).setClipChildren(false);
      ((ViewGroup) view).setClipToPadding(false);
    }
    return new GroupViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
    Group group = getItem(position);
    if (group != null) {
      // Ensure view is in normal state to prevent spacing issues
      resetViewState(holder.itemView);
      holder.bind(group);

      // No entrance animations to prevent spacing issues
      // Items will appear immediately without animation interference
    }
  }
  
  @Override
  public void onBindViewHolder(@NonNull GroupViewHolder holder, int position, @NonNull java.util.List<Object> payloads) {
    if (payloads.isEmpty()) {
      // Full bind
      onBindViewHolder(holder, position);
    } else {
      // Partial bind for better performance - only update what changed
      Group group = getItem(position);
      if (group != null) {
        for (Object payload : payloads) {
          if ("name".equals(payload)) {
            holder.setGroupName(group);
          } else if ("date".equals(payload)) {
            holder.setGroupDate(group);
          } else if ("image".equals(payload)) {
            holder.loadGroupImage(group.getGroupKey());
          }
        }
      }
    }
  }

  /**
   * Ensures view is in completely normal state to prevent spacing issues
   *
   * @param view the view to reset to normal state
   */
  private void resetViewState(View view) {
    view.setAlpha(NORMAL_ALPHA);
    view.setScaleX(NORMAL_SCALE);
    view.setScaleY(NORMAL_SCALE);
    view.setTranslationX(ZERO_TRANSLATION);
    view.setTranslationY(ZERO_TRANSLATION);
    view.setRotation(ZERO_ROTATION);
  }

  @Override
  public void onViewRecycled(@NonNull GroupViewHolder holder) {
    super.onViewRecycled(holder);
    // Clear the ViewHolder when it's recycled to prevent image loading issues
    holder.clear();
    // Reset view state to prevent spacing issues from animations
    resetViewState(holder.itemView);
    // Also clear any pending Glide requests
    GlideImageLoader.clearImageView(context, holder.groupImageView);
  }

  @Override
  protected boolean areItemsTheSame(Group oldItem, Group newItem) {
    return oldItem != null
        && newItem != null
        && Objects.equals(oldItem.getGroupKey(), newItem.getGroupKey());
  }

  @Override
  protected boolean areContentsTheSame(Group oldItem, Group newItem) {
    return oldItem != null
        && newItem != null
        && Objects.equals(oldItem.getGroupName(), newItem.getGroupName())
        && Objects.equals(oldItem.getGroupLocation(), newItem.getGroupLocation())
        && Objects.equals(oldItem.getGroupDays(), newItem.getGroupDays())
        && Objects.equals(oldItem.getGroupMonths(), newItem.getGroupMonths())
        && Objects.equals(oldItem.getGroupYears(), newItem.getGroupYears())
        && Objects.equals(oldItem.getGroupHours(), newItem.getGroupHours())
        && Objects.equals(oldItem.getGroupPrice(), newItem.getGroupPrice())
        && oldItem.getGroupType() == newItem.getGroupType();
  }

  /** Interface for handling group click events */
  public interface OnGroupClickListener {
    /**
     * Called when a group item is clicked
     *
     * @param group the clicked group
     */
    void onGroupClick(Group group);
  }

  /** ViewHolder for group items */
  class GroupViewHolder extends RecyclerView.ViewHolder {
    private final TextView groupNameTextView;
    private final TextView groupDateTextView;
    private final ImageView groupImageView;

    GroupViewHolder(@NonNull View itemView) {
      super(itemView);
      groupNameTextView = itemView.findViewById(R.id.tvGroupName);
      groupDateTextView = itemView.findViewById(R.id.tvGroupDate);
      groupImageView = itemView.findViewById(R.id.imgGroupPicture);
      
      // Optimize text views to reduce GPU operations
      if (groupNameTextView != null) {
        groupNameTextView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        groupNameTextView.setIncludeFontPadding(false);
      }
      if (groupDateTextView != null) {
        groupDateTextView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        groupDateTextView.setIncludeFontPadding(false);
      }
      // Image view uses software layer for static images
      if (groupImageView != null) {
        groupImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
      }
      
      setupClickListeners();
    }

    /** Sets up click and long click listeners */
    private void setupClickListeners() {
      itemView.setOnClickListener(this::handleItemClick);
      itemView.setOnLongClickListener(this::handleItemLongClick);
    }

    /**
     * Handles regular click events on group items
     *
     * @param view the clicked view
     */
    private void handleItemClick(View view) {
      int position = getAdapterPosition();
      if (position != RecyclerView.NO_POSITION && listener != null) {
        listener.onGroupClick(getItem(position));
      }
    }

    /**
     * Handles long click events for sharing functionality
     *
     * @param view the long-clicked view
     * @return true if the event was handled
     */
    private boolean handleItemLongClick(View view) {
      int position = getAdapterPosition();
      if (position != RecyclerView.NO_POSITION) {
        Group group = getItem(position);
        view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
        ContentSharingManager.sharePartyText(context, group);
        return true;
      }
      return false;
    }

    /** Clears the ViewHolder content to prevent showing old data during recycling */
    void clear() {
      groupNameTextView.setText("");
      groupDateTextView.setText("");
      groupImageView.setImageResource(R.drawable.default_group_image);
      groupImageView.setTag(null);
    }

    /**
     * Binds group data to the view holder
     *
     * @param group the group data to display
     */
    void bind(Group group) {
      setGroupName(group);
      setGroupDate(group);
      loadGroupImage(group.getGroupKey());
    }

    /**
     * Sets the group name in the text view
     *
     * @param group the group containing the name
     */
    private void setGroupName(Group group) {
      groupNameTextView.setText(group.getGroupName());
    }

    /**
     * Formats and sets the group date in the text view
     *
     * @param group the group containing date information
     */
    private void setGroupDate(Group group) {
      String formattedDate =
          String.format(
              DATE_FORMAT,
              group.getGroupDays(),
              group.getGroupMonths(),
              group.getGroupYears(),
              group.getGroupHours());
      groupDateTextView.setText(formattedDate);
    }

    /**
     * Improved image loading that doesn't interfere with text display
     *
     * @param groupKey the key of the group to load image for
     */
    private void loadGroupImage(String groupKey) {
      setDefaultImage();

      if (isGroupKeyValid(groupKey)) {
        prepareImageViewForLoading(groupKey);
        loadGroupImageWithTimeout(groupKey, groupImageView);
      }
    }

    /** Sets the default image in the image view */
    private void setDefaultImage() {
      groupImageView.setImageResource(R.drawable.default_group_image);
    }

    /**
     * Checks if the group key is valid for image loading
     *
     * @param groupKey the group key to validate
     * @return true if key is valid, false otherwise
     */
    private boolean isGroupKeyValid(String groupKey) {
      return groupKey != null && !groupKey.isEmpty();
    }

    /**
     * Prepares the image view for loading by setting the group key as tag
     *
     * @param groupKey the group key to store as tag
     */
    private void prepareImageViewForLoading(String groupKey) {
      groupImageView.setTag(groupKey);
    }

    /**
     * Loads group image with timeout and enhanced error handling. Tries multiple Firebase Storage
     * paths with fallback mechanism.
     *
     * @param groupKey the key of the group to load image for
     * @param imageView the image view to load the image into
     */
    private void loadGroupImageWithTimeout(String groupKey, ImageView imageView) {
      Task<android.net.Uri> primaryTask = createPrimaryImageTask(groupKey);
      Task<android.net.Uri> timedPrimaryTask =
          Tasks.withTimeout(primaryTask, FIREBASE_TIMEOUT_MS, TimeUnit.MILLISECONDS);

      timedPrimaryTask
          .addOnSuccessListener(uri -> handlePrimaryImageSuccess(groupKey, imageView, uri))
          .addOnFailureListener(error -> handlePrimaryImageFailure(groupKey, imageView, error));
    }

    /**
     * Creates the primary image loading task
     *
     * @param groupKey the group key for the image
     * @return the Firebase task for loading the image
     */
    private Task<android.net.Uri> createPrimaryImageTask(String groupKey) {
      return com.example.partymaker.data.firebase.DBRef.refStorage
          .child(PRIMARY_STORAGE_PATH + groupKey)
          .getDownloadUrl();
    }

    /**
     * Handles successful loading of image from primary path
     *
     * @param groupKey the group key
     * @param imageView the image view to load into
     * @param uri the image URI
     */
    private void handlePrimaryImageSuccess(
        String groupKey, ImageView imageView, android.net.Uri uri) {
      if (isImageViewStillValid(groupKey, imageView)) {
        Log.d(TAG, "Successfully loaded image from primary path for group: " + groupKey);
        GlideImageLoader.loadImage(
            context, uri.toString(), imageView, R.drawable.default_group_image);
      }
    }

    /**
     * Handles failure to load image from primary path
     *
     * @param groupKey the group key
     * @param imageView the image view
     * @param error the error that occurred
     */
    private void handlePrimaryImageFailure(String groupKey, ImageView imageView, Exception error) {
      logPrimaryImageError(groupKey, error);

      if (isImageViewStillValid(groupKey, imageView)) {
        tryFallbackImagePath(groupKey, imageView);
      }
    }

    /**
     * Logs errors from primary image loading, filtering out common "not found" errors
     *
     * @param groupKey the group key
     * @param error the error to log
     */
    private void logPrimaryImageError(String groupKey, Exception error) {
      if (isObjectNotExistError(error)) {
        Log.w(TAG, "Primary path failed for group " + groupKey + ": " + error.getMessage());
      }
    }

    /**
     * Checks if the error is a common "object does not exist" error
     *
     * @param error the error to check
     * @return true if it's an object not exist error
     */
    private boolean isObjectNotExistError(Exception error) {
      return error.getMessage() == null || !error.getMessage().contains(OBJECT_NOT_EXIST_ERROR);
    }

    /**
     * Checks if the image view is still valid for the given group key
     *
     * @param groupKey the group key to check
     * @param imageView the image view to validate
     * @return true if the image view still corresponds to the group key
     */
    private boolean isImageViewStillValid(String groupKey, ImageView imageView) {
      return groupKey.equals(imageView.getTag());
    }

    /**
     * Tries the fallback Firebase Storage path for group images
     *
     * @param groupKey the group key
     * @param imageView the image view to load into
     */
    private void tryFallbackImagePath(String groupKey, ImageView imageView) {
      Task<android.net.Uri> fallbackTask = createFallbackImageTask(groupKey);
      Task<android.net.Uri> timedFallbackTask =
          Tasks.withTimeout(fallbackTask, FIREBASE_TIMEOUT_MS, TimeUnit.MILLISECONDS);

      timedFallbackTask
          .addOnSuccessListener(uri -> handleFallbackImageSuccess(groupKey, imageView, uri))
          .addOnFailureListener(error -> handleFallbackImageFailure(groupKey, imageView, error));
    }

    /**
     * Creates the fallback image loading task
     *
     * @param groupKey the group key for the image
     * @return the Firebase task for loading the fallback image
     */
    private Task<android.net.Uri> createFallbackImageTask(String groupKey) {
      return com.example.partymaker.data.firebase.DBRef.refStorage
          .child(FALLBACK_STORAGE_PATH + groupKey)
          .getDownloadUrl();
    }

    /**
     * Handles successful loading of image from fallback path
     *
     * @param groupKey the group key
     * @param imageView the image view to load into
     * @param uri the image URI
     */
    private void handleFallbackImageSuccess(
        String groupKey, ImageView imageView, android.net.Uri uri) {
      if (isImageViewStillValid(groupKey, imageView)) {
        Log.d(TAG, "Successfully loaded image from fallback path for group: " + groupKey);
        GlideImageLoader.loadImage(
            context, uri.toString(), imageView, R.drawable.default_group_image);
      }
    }

    /**
     * Handles failure to load image from fallback path
     *
     * @param groupKey the group key
     * @param imageView the image view
     * @param error the error that occurred
     */
    private void handleFallbackImageFailure(String groupKey, ImageView imageView, Exception error) {
      logFallbackImageError(groupKey, error);
      logDefaultImageUsage(groupKey, imageView);
    }

    /**
     * Logs errors from fallback image loading
     *
     * @param groupKey the group key
     * @param error the error to log
     */
    private void logFallbackImageError(String groupKey, Exception error) {
      if (isObjectNotExistError(error)) {
        Log.w(TAG, "Fallback path also failed for group " + groupKey + ": " + error.getMessage());
      }
    }

    /**
     * Logs that default image is being used
     *
     * @param groupKey the group key
     * @param imageView the image view
     */
    private void logDefaultImageUsage(String groupKey, ImageView imageView) {
      if (isImageViewStillValid(groupKey, imageView)) {
        Log.d(TAG, "Using default image for group: " + groupKey + " (both paths failed)");
      }
    }
  }
}
