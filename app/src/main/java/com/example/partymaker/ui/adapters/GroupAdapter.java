package com.example.partymaker.ui.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import com.example.partymaker.R;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.ui.group.UsersListActivity;
import com.squareup.picasso.Picasso;
import java.util.List;

/** Adapter for displaying groups in a ListView. Loads group images and displays group details. */
public class GroupAdapter extends ArrayAdapter<Group> {
  /** The context in which the adapter is used. */
  Context context;
  /** The list of groups to display. */
  List<Group> groupList;
  /** Application context for image loading. */
  Context applicationContext;

  /**
   * Constructor for GroupAdapter.
   *
   * @param context the context
   * @param resource the layout resource ID
   * @param textViewResourceId the text view resource ID
   * @param groupList the list of groups
   */
  public GroupAdapter(
      @NonNull Context context,
      @LayoutRes int resource,
      @IdRes int textViewResourceId,
      @NonNull List<Group> groupList) {
    super(context, resource, textViewResourceId, groupList);
    this.context = context;
    this.groupList = groupList;
    this.applicationContext = UsersListActivity.getContextOfApplication();
  }

  /**
   * Returns the view for a specific group in the list. Loads group image and displays group
   * details.
   *
   * @param position the position in the list
   * @param convertView the recycled view
   * @param parent the parent view group
   * @return the view for the group
   */
  @NonNull
  @Override
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {

    LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
    @SuppressLint("ViewHolder")
    View view = layoutInflater.inflate(R.layout.item_group, parent, false);
    Group temp = groupList.get(position);

    TextView tvpGroupName = view.findViewById(R.id.tvGroupName);
    tvpGroupName.setText(temp.getGroupName());

    TextView tvpGroupDate = view.findViewById(R.id.tvGroupDate);
    String GroupDate =
        (temp.getGroupDays() + " " + temp.getGroupMonths() + " " + temp.getGroupYears());
    tvpGroupDate.setText(GroupDate);

    final ImageView imageView = view.findViewById(R.id.imgGroupPicture);

    String GroupKey = temp.getGroupKey();

    DBRef.refStorage
        .child("Groups/" + GroupKey)
        .getDownloadUrl()
        .addOnSuccessListener(
            uri ->
                Picasso.get()
                    .load(uri) // image url goes here
                    .fit()
                    .centerCrop()
                    .into(imageView))
        .addOnFailureListener(
            exception -> {
              // Handle any errors
            });

    return view;
  }
}
