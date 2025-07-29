package com.example.partymaker.ui.adapter;

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
import com.example.partymaker.util.GlideImageLoader;
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
    }

    void bind(Group group) {
      groupNameTextView.setText(group.getGroupName());

      // Format date
      String date =
          String.format(
              "%s/%s/%s %s",
              group.getGroupDays(),
              group.getGroupMonths(),
              group.getGroupYears(),
              group.getGroupHours());
      groupDateTextView.setText(date);

      // Load group image from Firebase Storage
      String groupKey = group.getGroupKey();
      if (groupKey != null && !groupKey.isEmpty()) {
        // Try to load from new path first
        com.example.partymaker.data.firebase.DBRef.refStorage
            .child("UsersImageProfile/Groups/" + groupKey)
            .getDownloadUrl()
            .addOnSuccessListener(
                uri ->
                    GlideImageLoader.loadImage(
                        context, uri.toString(), groupImageView, R.drawable.ic_launcher))
            .addOnFailureListener(
                e -> {
                  // Try old path as fallback
                  com.example.partymaker.data.firebase.DBRef.refStorage
                      .child("Groups/" + groupKey)
                      .getDownloadUrl()
                      .addOnSuccessListener(
                          uri ->
                              GlideImageLoader.loadImage(
                                  context,
                                  uri.toString(),
                                  groupImageView,
                                  R.drawable.ic_launcher))
                      .addOnFailureListener(
                          e2 -> {
                            // Use default image if both paths fail
                            groupImageView.setImageResource(R.drawable.ic_launcher);
                          });
                });
      } else {
        // No group key, use default image
        groupImageView.setImageResource(R.drawable.ic_launcher);
      }
    }
  }

  /** Interface for handling group click events. */
  public interface OnGroupClickListener {
    void onGroupClick(Group group);
  }
}
