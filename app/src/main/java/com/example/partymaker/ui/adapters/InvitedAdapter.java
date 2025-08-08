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
import com.example.partymaker.R;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.model.User;
import com.example.partymaker.utils.auth.AuthenticationManager;
import com.squareup.picasso.Picasso;
import java.util.List;

/** Adapter for displaying invited users in a ListView. Shows user details and admin flag. */
public class InvitedAdapter extends ArrayAdapter<User> {

  // Character constants for email formatting
  private static final char DOT_CHAR = '.';
  private static final char SPACE_CHAR = ' ';

  /** The context in which the adapter is used. */
  private final Context context;

  /** The list of invited users to display. */
  private final List<User> invitedList;

  /** The admin key for identifying admin users. */
  private final String adminKey;

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
    View view = inflateItemView(parent);
    User user = invitedList.get(position);

    setupUserViews(view, user);
    return view;
  }

  /** Inflates the item view layout. */
  private View inflateItemView(ViewGroup parent) {
    LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
    return layoutInflater.inflate(R.layout.item_invited, parent, false);
  }

  /** Sets up all user views in the item layout. */
  private void setupUserViews(View view, User user) {
    setupUserTextViews(view, user);
    setupUserImage(view, user);
    setupAdminFlag(view, user);
  }

  /** Sets up the text views for username and email. */
  private void setupUserTextViews(View view, User user) {
    TextView tvUserName = view.findViewById(R.id.tvInvitedUsername);
    tvUserName.setText(user.getUsername());

    TextView tvEmail = view.findViewById(R.id.tvInvitedEmail);
    tvEmail.setText(user.getEmail());
  }

  /** Sets up the user profile image. */
  private void setupUserImage(View view, User user) {
    ImageView imageView = view.findViewById(R.id.imgInvitedProfile);

    if (AuthenticationManager.isFirebaseAuthAvailable(context)) {
      loadUserProfileImage(imageView, user);
    } else {
      setDefaultImage(imageView);
    }
  }

  /** Loads the user profile image from Firebase Storage. */
  private void loadUserProfileImage(ImageView imageView, User user) {
    String userEmail = user.getEmail();
    if (userEmail != null) {
      String formattedEmail = userEmail.replace(DOT_CHAR, SPACE_CHAR);
      String imagePath = "Users/" + formattedEmail;

      DBRef.refStorage
          .child(imagePath)
          .getDownloadUrl()
          .addOnSuccessListener(uri -> loadImageWithPicasso(imageView, uri))
          .addOnFailureListener(exception -> setDefaultImage(imageView));
    } else {
      setDefaultImage(imageView);
    }
  }

  /** Loads image using Picasso library. */
  private void loadImageWithPicasso(ImageView imageView, android.net.Uri uri) {
    Picasso.get().load(uri).fit().centerCrop().into(imageView);
  }

  /** Sets the default image for user profile. */
  private void setDefaultImage(ImageView imageView) {
    imageView.setImageResource(R.drawable.ic_person);
  }

  /** Sets up the admin flag visibility. */
  private void setupAdminFlag(View view, User user) {
    ImageView adminFlag = view.findViewById(R.id.imgAdminFlag);

    if (adminFlag != null && adminKey != null && user.getEmail() != null) {
      boolean isAdmin = isUserAdmin(user.getEmail());
      adminFlag.setVisibility(isAdmin ? View.VISIBLE : View.INVISIBLE);
    }
  }

  /** Checks if the user is an admin by comparing formatted emails. */
  private boolean isUserAdmin(String userEmail) {
    String formattedUserEmail = userEmail.replace(DOT_CHAR, SPACE_CHAR);
    String formattedAdminKey = adminKey.replace(DOT_CHAR, SPACE_CHAR);
    return formattedAdminKey.equals(formattedUserEmail);
  }
}
