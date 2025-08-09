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

/**
 * Adapter for displaying users in a ListView. Loads user profile images and displays user details.
 */
public class UserAdapter extends ArrayAdapter<User> {
  private static final String NO_EMAIL_TEXT = "No email";
  private static final String UNKNOWN_EMAIL = "unknown";
  private static final char DOT_CHAR = '.';
  private static final char SPACE_CHAR = ' ';

  private final Context context;
  private final List<User> userList;

  /**
   * Constructor for UserAdapter.
   *
   * @param context the context
   * @param resource the layout resource ID
   * @param textViewResourceId the text view resource ID
   * @param userList the list of users
   */
  public UserAdapter(
      @NonNull Context context,
      @LayoutRes int resource,
      @IdRes int textViewResourceId,
      @NonNull List<User> userList) {
    super(context, resource, textViewResourceId, userList);
    this.context = context;
    this.userList = userList;
  }

  /**
   * Returns the view for a specific user in the list. Loads user profile image and displays user
   * details.
   *
   * @param position the position in the list
   * @param convertView the recycled view
   * @param parent the parent view group
   * @return the view for the user
   */
  @NonNull
  @Override
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {
    View view = inflateUserItemView(parent);
    User user = userList.get(position);

    setupUserViews(view, user);
    return view;
  }

  private View inflateUserItemView(ViewGroup parent) {
    LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
    return layoutInflater.inflate(R.layout.item_user, parent, false);
  }

  private void setupUserViews(View view, User user) {
    setupUserNameView(view, user);
    setupUserEmailView(view, user);
    setupUserImageView(view, user);
  }

  private void setupUserNameView(View view, User user) {
    TextView tvUserName = view.findViewById(R.id.tvUserListUsername);
    tvUserName.setText(user.getUsername());
  }

  private void setupUserEmailView(View view, User user) {
    TextView tvEmail = view.findViewById(R.id.tvUserListEmail);
    String email = user.getEmail() != null ? user.getEmail() : NO_EMAIL_TEXT;
    tvEmail.setText(email);
  }

  private void setupUserImageView(View view, User user) {
    ImageView imageView = view.findViewById(R.id.imgUserListProfile);

    if (AuthenticationManager.isLoggedIn(context)) {
      loadUserProfileImage(imageView, user);
    } else {
      setDefaultImage(imageView);
    }
  }

  private void loadUserProfileImage(ImageView imageView, User user) {
    String email = getProcessedEmail(user);

    // Try primary path first: UsersImageProfile/Users/[email]
    String primaryPath = "UsersImageProfile/Users/" + email;

    DBRef.refStorage
        .child(primaryPath)
        .getDownloadUrl()
        .addOnSuccessListener(uri -> loadImageWithPicasso(imageView, uri))
        .addOnFailureListener(
            primaryException -> {
              // Try fallback path: Users/[email]
              String fallbackPath = "Users/" + email;
              DBRef.refStorage
                  .child(fallbackPath)
                  .getDownloadUrl()
                  .addOnSuccessListener(uri -> loadImageWithPicasso(imageView, uri))
                  .addOnFailureListener(fallbackException -> setDefaultImage(imageView));
            });
  }

  private String getProcessedEmail(User user) {
    String userEmail = user.getEmail();
    return userEmail != null ? userEmail.replace(DOT_CHAR, SPACE_CHAR) : UNKNOWN_EMAIL;
  }

  private void loadImageWithPicasso(ImageView imageView, android.net.Uri uri) {
    Picasso.get().load(uri).fit().centerCrop().into(imageView);
  }

  private void setDefaultImage(ImageView imageView) {
    imageView.setImageResource(R.drawable.ic_person);
  }
}
