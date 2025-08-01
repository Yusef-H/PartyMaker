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
import com.example.partymaker.utils.auth.AuthHelper;
import com.squareup.picasso.Picasso;
import java.util.List;

/**
 * Adapter for displaying users in a ListView. Loads user profile images and displays user details.
 */
public class UserAdapter extends ArrayAdapter<User> {
  /** The context in which the adapter is used. */
  final Context context;

  /** The list of users to display. */
  final List<User> userList;

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

    LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
    @SuppressLint("ViewHolder")
    View view = layoutInflater.inflate(R.layout.item_user, parent, false);
    User temp = userList.get(position);

    TextView tvpUserName = view.findViewById(R.id.tvUserListUsername);
    tvpUserName.setText(temp.getUsername());

    TextView tvpEmail = view.findViewById(R.id.tvUserListEmail);
    tvpEmail.setText(temp.getEmail() != null ? temp.getEmail() : "No email");

    final ImageView imageView = view.findViewById(R.id.imgUserListProfile);

    String UserImageProfile = temp.getEmail();
    String email = UserImageProfile != null ? UserImageProfile.replace('.', ' ') : "unknown";

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

    return view;
  }
}
