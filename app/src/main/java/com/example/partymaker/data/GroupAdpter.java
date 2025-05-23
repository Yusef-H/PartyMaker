package com.example.partymaker.data;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import com.example.partymaker.FriendList;
import com.example.partymaker.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
    this.applicationContext = FriendList.getContextOfApplication();
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
    View view = layoutInflater.inflate(R.layout.group_list, parent, false);
    Group temp = GroupList.get(position);

    TextView tvpGroupName = (TextView) view.findViewById(R.id.tvGLgroupname);
    tvpGroupName.setText(temp.getGroupName());

    TextView tvpGroupDate = (TextView) view.findViewById(R.id.tvGLgroupdate);
    String GroupDate =
        (temp.getGroupDays() + " " + temp.getGroupMonths() + " " + temp.getGroupYears());
    tvpGroupDate.setText(GroupDate);

    final ImageView imageView = (ImageView) view.findViewById(R.id.imgGLpicture);

    String GroupKey = temp.getGroupKey();

    DBref.refStorage
        .child("Groups/" + GroupKey)
        .getDownloadUrl()
        .addOnSuccessListener(
            new OnSuccessListener<Uri>() {
              @Override
              public void onSuccess(Uri uri) {
                Picasso.get()
                    .load(uri) // image url goes here
                    .fit()
                    .centerCrop()
                    .into(imageView);
              }
            })
        .addOnFailureListener(
            new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception exception) {
                // Handle any errors
              }
            });

    return view;
  }
}
