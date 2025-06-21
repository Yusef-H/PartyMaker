package com.example.partymaker.ui.adapters;

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
import com.example.partymaker.ui.group.MembersActivity;
import com.example.partymaker.R;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.model.Group;
import com.squareup.picasso.Picasso;
import java.util.List;

public class GroupAdpter extends ArrayAdapter<Group> {
  Context context;
  List<Group> GroupList;
  Context applicationContext;

  public GroupAdpter(
      @NonNull Context context,
      @LayoutRes int resource,
      @IdRes int textViewResourceId,
      @NonNull List<Group> GroupList) {
    super(context, resource, textViewResourceId, GroupList);
    this.context = context;
    this.GroupList = GroupList;
    this.applicationContext = MembersActivity.getContextOfApplication();
  }

  @NonNull
  @Override
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {

    LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
    View view = layoutInflater.inflate(R.layout.group_list, parent, false);
    Group temp = GroupList.get(position);

    TextView tvpGroupName = view.findViewById(R.id.tvGLgroupname);
    tvpGroupName.setText(temp.getGroupName());

    TextView tvpGroupDate = view.findViewById(R.id.tvGLgroupdate);
    String GroupDate =
        (temp.getGroupDays() + " " + temp.getGroupMonths() + " " + temp.getGroupYears());
    tvpGroupDate.setText(GroupDate);

    final ImageView imageView = view.findViewById(R.id.imgGLpicture);

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
