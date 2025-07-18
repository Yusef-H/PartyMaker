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
import com.example.partymaker.data.model.User;
import com.example.partymaker.utilities.AuthHelper;
import com.squareup.picasso.Picasso;
import java.util.List;

/** Adapter for displaying invited users in a ListView. Shows user details and admin flag. */
public class InvitedAdapter extends ArrayAdapter<User> {
  /** The context in which the adapter is used. */
  Context context;
  /** The list of invited users to display. */
  List<User> invitedList;
  /** The admin key for identifying admin users. */
  String adminKey;

  /**
   * Constructor for InvitedAdapter.
   *
   * @param context the context
   * @param resource the layout resource ID
   * @param textViewResourceId the text view resource ID
   * @param invitedList the list of invited users
   * @param adminKey the admin key
   */
  public InvitedAdapter(
      @NonNull Context context,
      @LayoutRes int resource,
      @IdRes int textViewResourceId,
      @NonNull List<User> invitedList,
      String adminKey) {
    super(context, resource, textViewResourceId, invitedList);
    this.context = context;
    this.invitedList = invitedList;
    this.adminKey = adminKey;
  }

  /**
   * Returns the view for a specific invited user in the list. Shows user details and admin flag.
   *
   * @param position the position in the list
   * @param convertView the recycled view
   * @param parent the parent view group
   * @return the view for the invited user
   */
  @NonNull
  @Override
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {
    LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
    @SuppressLint("ViewHolder")
    View view = layoutInflater.inflate(R.layout.item_invited, parent, false);
    User temp = invitedList.get(position);

    TextView tvpUserName = view.findViewById(R.id.tvInvitedUsername);
    tvpUserName.setText(temp.getUsername());

    TextView tvpEmail = view.findViewById(R.id.tvInvitedEmail);
    tvpEmail.setText(temp.getEmail());

    final ImageView imageView = view.findViewById(R.id.imgInvitedProfile);
    String UserImageProfile = temp.getEmail();
    String email = UserImageProfile.replace('.', ' ');

    // Only try to load image if Firebase Auth is available
    if (AuthHelper.isFirebaseAuthAvailable(context)) {
      DBRef.refStorage
          .child("Users/" + email)
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
                // Set default image on failure
                imageView.setImageResource(R.drawable.ic_person);
              });
    } else {
      // Set default image when Firebase Auth is not available
      imageView.setImageResource(R.drawable.ic_person);
    }

    // Show flag for admin
    ImageView adminFlag = view.findViewById(R.id.imgAdminFlag);

    if (adminFlag != null && adminKey != null) {
      // Remove dot
      String tempEmailFormatted = temp.getEmail().replace('.', ' ');
      String adminKeyFormatted = adminKey.replace('.', ' ');

      if (adminKeyFormatted.equals(tempEmailFormatted)) {
        adminFlag.setVisibility(View.VISIBLE);
      } else {
        adminFlag.setVisibility(View.INVISIBLE);
      }
    }

    return view;
  }
}
