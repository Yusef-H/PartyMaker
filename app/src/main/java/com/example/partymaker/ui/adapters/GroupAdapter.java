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

  private static final long FIREBASE_TIMEOUT_MS = 3000L;
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

  /** Ensures view is in completely normal state to prevent spacing issues */
  private void resetViewState(View view) {
    view.setAlpha(1.0f);
    view.setScaleX(1.0f);
    view.setScaleY(1.0f);
    view.setTranslationX(0f);
    view.setTranslationY(0f);
    view.setRotation(0f);
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

  /** Interface for handling group click events. */
  public interface OnGroupClickListener {
    void onGroupClick(Group group);
  }

  /** ViewHolder for group items. */
  class GroupViewHolder extends RecyclerView.ViewHolder {
    private final TextView groupNameTextView;
    private final TextView groupDateTextView;
    private final ImageView groupImageView;

    GroupViewHolder(@NonNull View itemView) {
      super(itemView);
      groupNameTextView = itemView.findViewById(R.id.tvGroupName);
      groupDateTextView = itemView.findViewById(R.id.tvGroupDate);
      groupImageView = itemView.findViewById(R.id.imgGroupPicture);

      // Removed press animations to prevent spacing issues
      // Keep cards at consistent elevation and spacing

      itemView.setOnClickListener(
          v -> {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && listener != null) {
              // Immediate navigation without animation to prevent spacing issues
              listener.onGroupClick(getItem(position));
            }
          });

      // Add long click for share functionality with subtle haptic feedback only
      itemView.setOnLongClickListener(
          v -> {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
              Group group = getItem(position);
              // Only haptic feedback, no visual animation to prevent layout issues
              v.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
              ContentSharingManager.sharePartyText(context, group);
            }
            return true;
          });
    }

    /** Clears the ViewHolder content to prevent showing old data during recycling */
    void clear() {
      groupNameTextView.setText("");
      groupDateTextView.setText("");
      groupImageView.setImageResource(R.drawable.default_group_image);
      groupImageView.setTag(null);
    }

    void bind(Group group) {
      // Set content immediately for text to prevent layout shifts
      groupNameTextView.setText(group.getGroupName());

      // Format date
      String date =
          String.format(
              "%s %s %s    %s",
              group.getGroupDays(),
              group.getGroupMonths(),
              group.getGroupYears(),
              group.getGroupHours());
      groupDateTextView.setText(date);

      // Handle image loading separately to avoid blocking text display
      loadGroupImage(group.getGroupKey());
    }

    /** Improved image loading that doesn't interfere with text display */
    private void loadGroupImage(String groupKey) {
      // Set default image first
      groupImageView.setImageResource(R.drawable.default_group_image);

      if (groupKey == null || groupKey.isEmpty()) {
        return;
      }

      // Store group key as tag for recycling check
      groupImageView.setTag(groupKey);

      // Load image with shorter timeout to prevent delays
      loadGroupImageWithTimeout(groupKey, groupImageView);
    }

    /**
     * Loads group image with timeout and enhanced error handling. Tries multiple Firebase Storage
     * paths with fallback mechanism.
     */
    private void loadGroupImageWithTimeout(String groupKey, ImageView imageView) {
      // Shorter timeout for Firebase Storage requests to prevent delays
      final long timeoutMs = FIREBASE_TIMEOUT_MS;

      // Try primary path with timeout
      Task<android.net.Uri> primaryTask =
          com.example.partymaker.data.firebase.DBRef.refStorage
              .child("UsersImageProfile/Groups/" + groupKey)
              .getDownloadUrl();

      // Apply timeout to the task
      Task<android.net.Uri> timedPrimaryTask =
          Tasks.withTimeout(primaryTask, timeoutMs, TimeUnit.MILLISECONDS);

      timedPrimaryTask
          .addOnSuccessListener(
              uri -> {
                // Check if this ViewHolder is still showing the same group
                if (groupKey.equals(imageView.getTag())) {
                  Log.d(
                      "GroupAdapter",
                      "Successfully loaded image from primary path for group: " + groupKey);
                  GlideImageLoader.loadImage(
                      context, uri.toString(), imageView, R.drawable.default_group_image);
                }
              })
          .addOnFailureListener(
              e -> {
                // Log the error but don't spam logcat for missing images
                if (!(e.getMessage() != null && e.getMessage().contains("Object does not exist"))) {
                  Log.w(
                      "GroupAdapter",
                      "Primary path failed for group " + groupKey + ": " + e.getMessage());
                }

                // Try fallback path with timeout, but only if ViewHolder still shows same group
                if (groupKey.equals(imageView.getTag())) {
                  tryFallbackImagePath(groupKey, imageView);
                }
              });
    }

    /** Tries the fallback Firebase Storage path for group images. */
    private void tryFallbackImagePath(String groupKey, ImageView imageView) {
      final long timeoutMs = FIREBASE_TIMEOUT_MS;

      Task<android.net.Uri> fallbackTask =
          com.example.partymaker.data.firebase.DBRef.refStorage
              .child("Groups/" + groupKey)
              .getDownloadUrl();

      // Apply timeout to fallback task
      Task<android.net.Uri> timedFallbackTask =
          Tasks.withTimeout(fallbackTask, timeoutMs, TimeUnit.MILLISECONDS);

      timedFallbackTask
          .addOnSuccessListener(
              uri -> {
                // Double-check the tag again
                if (groupKey.equals(imageView.getTag())) {
                  Log.d(
                      "GroupAdapter",
                      "Successfully loaded image from fallback path for group: " + groupKey);
                  GlideImageLoader.loadImage(
                      context, uri.toString(), imageView, R.drawable.default_group_image);
                }
              })
          .addOnFailureListener(
              e -> {
                // Only log non-404 errors to reduce log spam
                if (!(e.getMessage() != null && e.getMessage().contains("Object does not exist"))) {
                  Log.w(
                      "GroupAdapter",
                      "Fallback path also failed for group " + groupKey + ": " + e.getMessage());
                }

                // Keep default image - it's already set, no need to change
                if (groupKey.equals(imageView.getTag())) {
                  Log.d(
                      "GroupAdapter",
                      "Using default image for group: " + groupKey + " (both paths failed)");
                }
              });
    }
  }
}
