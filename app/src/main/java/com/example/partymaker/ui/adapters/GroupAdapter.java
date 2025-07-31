package com.example.partymaker.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.partymaker.R;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.utils.media.GlideImageLoader;
import com.example.partymaker.utils.sharing.ShareHelper;
import java.util.Objects;

/**
 * Adapter for displaying groups in a RecyclerView. Uses the OptimizedRecyclerAdapter base class for
 * efficient updates.
 */
public class GroupAdapter extends OptimizedRecyclerAdapter<Group, GroupAdapter.GroupViewHolder> {

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
      holder.bind(group);
    }
  }

  @Override
  public void onViewRecycled(@NonNull GroupViewHolder holder) {
    super.onViewRecycled(holder);
    // Clear the ViewHolder when it's recycled to prevent image loading issues
    holder.clear();
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

      itemView.setOnClickListener(
          v -> {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && listener != null) {
              listener.onGroupClick(getItem(position));
            }
          });

      // Add long click for share functionality
      itemView.setOnLongClickListener(
          v -> {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
              Group group = getItem(position);
              ShareHelper.sharePartyText(context, group);
            }
            return true;
          });
    }

    /** Clears the ViewHolder content to prevent showing old data during recycling */
    void clear() {
      groupNameTextView.setText("");
      groupDateTextView.setText("");
      groupImageView.setImageResource(R.drawable.ic_launcher);
      groupImageView.setTag(null);
    }

    void bind(Group group) {
      // Clear previous content first to prevent flickering
      clear();

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

      // Load group image from Firebase Storage with proper placeholder handling
      String groupKey = group.getGroupKey();

      // First, set placeholder immediately to prevent flickering
      groupImageView.setImageResource(R.drawable.ic_launcher);

      if (groupKey != null && !groupKey.isEmpty()) {
        // Store the group key as tag to prevent loading wrong images during recycling
        groupImageView.setTag(groupKey);

        // Try to load from new path first
        com.example.partymaker.data.firebase.DBRef.refStorage
            .child("UsersImageProfile/Groups/" + groupKey)
            .getDownloadUrl()
            .addOnSuccessListener(
                uri -> {
                  // Check if this ViewHolder is still showing the same group
                  if (groupKey.equals(groupImageView.getTag())) {
                    GlideImageLoader.loadImage(
                        context, uri.toString(), groupImageView, R.drawable.ic_launcher);
                  }
                })
            .addOnFailureListener(
                e -> {
                  // Try old path as fallback, but only if ViewHolder still shows same group
                  if (groupKey.equals(groupImageView.getTag())) {
                    com.example.partymaker.data.firebase.DBRef.refStorage
                        .child("Groups/" + groupKey)
                        .getDownloadUrl()
                        .addOnSuccessListener(
                            uri -> {
                              // Double-check the tag again
                              if (groupKey.equals(groupImageView.getTag())) {
                                GlideImageLoader.loadImage(
                                    context,
                                    uri.toString(),
                                    groupImageView,
                                    R.drawable.ic_launcher);
                              }
                            })
                        .addOnFailureListener(
                            e2 -> {
                              // Keep default image if both paths fail and ViewHolder still shows
                              // same group
                              if (groupKey.equals(groupImageView.getTag())) {
                                // Image is already set to default, no need to change
                              }
                            });
                  }
                });
      }
      // If no group key, default image is already set above
    }
  }
}
