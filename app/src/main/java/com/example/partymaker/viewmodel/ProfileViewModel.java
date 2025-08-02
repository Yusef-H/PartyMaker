package com.example.partymaker.viewmodel;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.partymaker.data.api.AppNetworkError;
import com.example.partymaker.data.api.ConnectivityManager;
import com.example.partymaker.data.api.NetworkUtils;
import com.example.partymaker.data.model.User;
import com.example.partymaker.data.repository.UserRepository;
import com.example.partymaker.utils.auth.AuthenticationManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ViewModel for managing user profile operations in EditProfileActivity. Handles user data loading,
 * updates, and profile management with network awareness.
 */
public class ProfileViewModel extends ViewModel {
  private static final String TAG = "ProfileViewModel";

  // LiveData for user list
  private final MutableLiveData<List<User>> userList = new MutableLiveData<>(new ArrayList<>());

  // LiveData for currently selected user
  private final MutableLiveData<User> selectedUser = new MutableLiveData<>();

  // LiveData for current logged-in user
  private final MutableLiveData<User> currentUser = new MutableLiveData<>();

  // LiveData for loading state
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

  // LiveData for error messages
  private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

  // LiveData for network error type
  private final MutableLiveData<NetworkUtils.ErrorType> networkErrorType = new MutableLiveData<>();

  // LiveData for success messages
  private final MutableLiveData<String> successMessage = new MutableLiveData<>();

  // Repository instance
  private final UserRepository repository;

  // Context reference for error handling
  private Context appContext;

  /** Constructor */
  public ProfileViewModel() {
    repository = UserRepository.getInstance();

    // Observe network availability changes
    ConnectivityManager.getInstance()
        .getNetworkAvailability()
        .observeForever(
            isAvailable -> {
              if (isAvailable && networkErrorType.getValue() == NetworkUtils.ErrorType.NO_NETWORK) {
                // Network is back, clear the error
                networkErrorType.setValue(null);
              }
            });
  }

  /**
   * Sets the application context
   *
   * @param context The application context
   */
  public void setAppContext(Context context) {
    if (context != null) {
      this.appContext = context.getApplicationContext();
    }
  }

  /**
   * Gets the list of users as LiveData
   *
   * @return LiveData containing list of users
   */
  public LiveData<List<User>> getUsers() {
    return userList;
  }

  /**
   * Gets the currently selected user as LiveData
   *
   * @return LiveData containing the selected user
   */
  public LiveData<User> getSelectedUser() {
    return selectedUser;
  }

  /**
   * Gets the current logged-in user as LiveData
   *
   * @return LiveData containing the current user
   */
  public LiveData<User> getCurrentUser() {
    return currentUser;
  }

  /**
   * Gets the loading state as LiveData
   *
   * @return LiveData containing the loading state
   */
  public LiveData<Boolean> getIsLoading() {
    return isLoading;
  }

  /**
   * Gets the error message as LiveData
   *
   * @return LiveData containing the error message
   */
  public LiveData<String> getErrorMessage() {
    return errorMessage;
  }

  /**
   * Gets the network error type as LiveData
   *
   * @return LiveData containing the network error type
   */
  public LiveData<NetworkUtils.ErrorType> getNetworkErrorType() {
    return networkErrorType;
  }

  /**
   * Gets the success message as LiveData
   *
   * @return LiveData containing the success message
   */
  public LiveData<String> getSuccessMessage() {
    return successMessage;
  }

  /**
   * Loads all users
   *
   * @param forceRefresh Whether to force a refresh from the server
   */
  public void loadAllUsers(boolean forceRefresh) {
    Log.d(TAG, "Loading all users, forceRefresh: " + forceRefresh);

    // Check network availability
    if (!forceRefresh
        && Boolean.FALSE.equals(
            ConnectivityManager.getInstance().getNetworkAvailability().getValue())) {
      Log.w(TAG, "Network not available, using cached data");
      errorMessage.setValue("Network not available. Using cached data.");
      networkErrorType.setValue(NetworkUtils.ErrorType.NO_NETWORK);
      return;
    }

    isLoading.setValue(true);

    repository.getAllUsers(
        new UserRepository.DataCallback<>() {
          @Override
          public void onDataLoaded(List<User> data) {
            Log.d(TAG, "Users loaded successfully: " + data.size() + " users");
            userList.setValue(data);
            isLoading.setValue(false);
            networkErrorType.setValue(null); // Clear any network error
          }

          @Override
          public void onError(String error) {
            Log.e(TAG, "Error loading users: " + error);
            errorMessage.setValue("Failed to load users: " + error);
            isLoading.setValue(false);

            // Determine error type and handle it
            NetworkUtils.ErrorType type = determineErrorType(error);
            networkErrorType.setValue(type);

            if (appContext != null) {
              AppNetworkError.handleNetworkError(appContext, error, type, true);
            }
          }
        },
        forceRefresh);
  }

  /**
   * Loads a specific user by ID
   *
   * @param userId The user ID
   * @param forceRefresh Whether to force a refresh from the server
   */
  public void loadUser(String userId, boolean forceRefresh) {
    if (userId == null || userId.isEmpty()) {
      Log.e(TAG, "Cannot load user: userId is null or empty");
      errorMessage.setValue("Invalid user ID");
      return;
    }

    Log.d(TAG, "Loading user with ID: " + userId);

    // Check network availability for forced refresh
    if (forceRefresh
        && Boolean.FALSE.equals(
            ConnectivityManager.getInstance().getNetworkAvailability().getValue())) {
      Log.w(TAG, "Network not available for forced refresh");
      errorMessage.setValue("Network not available. Using cached data.");
      networkErrorType.setValue(NetworkUtils.ErrorType.NO_NETWORK);

      if (appContext != null) {
        AppNetworkError.handleNetworkError(
            appContext, "Network not available", NetworkUtils.ErrorType.NO_NETWORK, false);
      }
    }

    isLoading.setValue(true);

    repository.getUser(
        userId,
        new UserRepository.DataCallback<>() {
          @Override
          public void onDataLoaded(User data) {
            Log.d(TAG, "User loaded successfully: " + data.getUsername());
            selectedUser.setValue(data);
            isLoading.setValue(false);
            networkErrorType.setValue(null); // Clear any network error
          }

          @Override
          public void onError(String error) {
            Log.e(TAG, "Error loading user: " + error);
            errorMessage.setValue("Failed to load user: " + error);
            isLoading.setValue(false);

            // Determine error type and handle it
            NetworkUtils.ErrorType type = determineErrorType(error);
            networkErrorType.setValue(type);

            if (appContext != null) {
              AppNetworkError.handleNetworkError(appContext, error, type, false);
            }
          }
        },
        forceRefresh);
  }

  /**
   * Loads the current logged-in user
   *
   * @param context Application context
   * @param forceRefresh Whether to force a refresh from the server
   */
  public void loadCurrentUser(Context context, boolean forceRefresh) {
    Log.d(TAG, "Loading current user, forceRefresh: " + forceRefresh);

    // Save context for error handling
    if (context != null) {
      this.appContext = context.getApplicationContext();
    }

    // Check network availability for forced refresh
    if (forceRefresh
        && Boolean.FALSE.equals(
            ConnectivityManager.getInstance().getNetworkAvailability().getValue())) {
      Log.w(TAG, "Network not available for forced refresh");
      errorMessage.setValue("Network not available. Using cached data.");
      networkErrorType.setValue(NetworkUtils.ErrorType.NO_NETWORK);

      if (appContext != null) {
        AppNetworkError.handleNetworkError(
            appContext, "Network not available", NetworkUtils.ErrorType.NO_NETWORK, false);
      }
    }

    isLoading.setValue(true);

    // Use direct Firebase access instead of repository to avoid User class serialization issues
    try {
      String userEmail = AuthenticationManager.getCurrentUserEmail(context);
      if (userEmail == null || userEmail.isEmpty()) {
        errorMessage.setValue("No current user found");
        isLoading.setValue(false);
        return;
      }

      String userKey = userEmail.replace('.', ' ');

      com.example.partymaker.data.firebase.DBRef.refUsers
          .child(userKey)
          .get()
          .addOnSuccessListener(
              dataSnapshot -> {
                if (dataSnapshot.exists()) {
                  // Use Map instead of User.class to avoid Firebase serialization issues
                  @SuppressWarnings("unchecked")
                  Map<String, Object> userData = (Map<String, Object>) dataSnapshot.getValue();
                  if (userData != null) {
                    // Create a User object manually from the Map
                    User user = new User();

                    // Set the userKey
                    user.setUserKey(userKey);

                    // Set email
                    if (userData.containsKey("email")) {
                      user.setEmail((String) userData.get("email"));
                    } else {
                      user.setEmail(userEmail); // Use the email we already have
                    }

                    // Set username - check both possible field names
                    if (userData.containsKey("username")) {
                      user.setUsername((String) userData.get("username"));
                    } else if (userData.containsKey("userName")) {
                      user.setUsername((String) userData.get("userName"));
                    }

                    // Set profile image URL
                    if (userData.containsKey("profileImageUrl")) {
                      user.setProfileImageUrl((String) userData.get("profileImageUrl"));
                    }

                    // Update the LiveData
                    currentUser.setValue(user);
                  }
                } else {
                  Log.w(TAG, "No user data found for key: " + userKey);
                  errorMessage.setValue("User data not found");
                }
                isLoading.setValue(false);
              })
          .addOnFailureListener(
              e -> {
                Log.e(TAG, "Error loading user data", e);
                errorMessage.setValue("Failed to load user data: " + e.getMessage());
                isLoading.setValue(false);

                // Determine error type and handle it
                NetworkUtils.ErrorType type = determineErrorType(e.getMessage());
                networkErrorType.setValue(type);

                if (appContext != null) {
                  AppNetworkError.handleNetworkError(appContext, e.getMessage(), type, false);
                }
              });
    } catch (Exception e) {
      Log.e(TAG, "Error in loadCurrentUser", e);
      errorMessage.setValue("Error: " + e.getMessage());
      isLoading.setValue(false);
    }
  }

  /**
   * Updates the current user's profile
   *
   * @param updates Map of fields to update
   */
  public void updateCurrentUser(Map<String, Object> updates) {
    User user = currentUser.getValue();
    if (user == null) {
      Log.e(TAG, "Cannot update current user: No current user loaded");
      errorMessage.setValue("No current user loaded");
      return;
    }

    if (updates == null || updates.isEmpty()) {
      Log.e(TAG, "Cannot update current user: No updates provided");
      errorMessage.setValue("No updates provided");
      return;
    }

    String userId = user.getUserKey();
    if (userId.isEmpty()) {
      Log.e(TAG, "Cannot update current user: Invalid user key");
      errorMessage.setValue("Invalid user key");
      return;
    }

    // Check network availability
    if (Boolean.FALSE.equals(
        ConnectivityManager.getInstance().getNetworkAvailability().getValue())) {
      Log.e(TAG, "Network not available for update");
      errorMessage.setValue("Network not available. Cannot update profile.");
      networkErrorType.setValue(NetworkUtils.ErrorType.NO_NETWORK);

      if (appContext != null) {
        AppNetworkError.handleNetworkError(
            appContext, "Network not available", NetworkUtils.ErrorType.NO_NETWORK, false);
      }
      return;
    }

    Log.d(TAG, "Updating current user: " + userId);
    isLoading.setValue(true);

    repository.updateUser(
        userId,
        updates,
        new UserRepository.OperationCallback() {
          @Override
          public void onComplete() {
            Log.d(TAG, "Current user updated successfully");

            // Apply updates to the current user object
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
              applyUpdateToUser(user, entry.getKey(), entry.getValue());
            }

            currentUser.setValue(user);
            isLoading.setValue(false);
            networkErrorType.setValue(null); // Clear any network error
            successMessage.setValue("Username updated successfully");
          }

          @Override
          public void onError(String error) {
            Log.e(TAG, "Error updating current user: " + error);
            errorMessage.setValue("Failed to update profile: " + error);
            isLoading.setValue(false);

            // Determine error type and handle it
            NetworkUtils.ErrorType type = determineErrorType(error);
            networkErrorType.setValue(type);

            if (appContext != null) {
              AppNetworkError.handleNetworkError(appContext, error, type, false);
            }
          }
        });
  }

  /**
   * Creates a new user
   *
   * @param userId The user ID
   * @param user The user to create
   */
  public void createUser(String userId, User user) {
    if (userId == null || userId.isEmpty()) {
      Log.e(TAG, "Cannot create user: userId is null or empty");
      errorMessage.setValue("Invalid user ID");
      return;
    }

    if (user == null) {
      Log.e(TAG, "Cannot create user: user object is null");
      errorMessage.setValue("Invalid user data");
      return;
    }

    // Check network availability
    if (Boolean.FALSE.equals(
        ConnectivityManager.getInstance().getNetworkAvailability().getValue())) {
      Log.e(TAG, "Network not available for creating user");
      errorMessage.setValue("Network not available. Cannot create user.");
      networkErrorType.setValue(NetworkUtils.ErrorType.NO_NETWORK);

      if (appContext != null) {
        AppNetworkError.handleNetworkError(
            appContext, "Network not available", NetworkUtils.ErrorType.NO_NETWORK, false);
      }
      return;
    }

    Log.d(TAG, "Creating new user: " + user.getUsername());
    isLoading.setValue(true);

    repository.saveUser(
        userId,
        user,
        new UserRepository.OperationCallback() {
          @Override
          public void onComplete() {
            Log.d(TAG, "User created successfully");

            // Add the new user to the list
            List<User> currentList = userList.getValue();
            if (currentList != null) {
              currentList.add(user);
              userList.setValue(currentList);
            }

            selectedUser.setValue(user);
            isLoading.setValue(false);
            networkErrorType.setValue(null); // Clear any network error
          }

          @Override
          public void onError(String error) {
            Log.e(TAG, "Error creating user: " + error);
            errorMessage.setValue("Failed to create user: " + error);
            isLoading.setValue(false);

            // Determine error type and handle it
            NetworkUtils.ErrorType type = determineErrorType(error);
            networkErrorType.setValue(type);

            if (appContext != null) {
              AppNetworkError.handleNetworkError(appContext, error, type, false);
            }
          }
        });
  }

  /**
   * Determines the type of error from an error message
   *
   * @param errorMessage The error message
   * @return The error type
   */
  private NetworkUtils.ErrorType determineErrorType(String errorMessage) {
    if (errorMessage == null) {
      return NetworkUtils.ErrorType.UNKNOWN;
    }

    String lowerCaseError = errorMessage.toLowerCase();

    if (lowerCaseError.contains("network")
        || lowerCaseError.contains("internet")
        || lowerCaseError.contains("connection")) {
      return NetworkUtils.ErrorType.NO_NETWORK;
    } else if (lowerCaseError.contains("timeout") || lowerCaseError.contains("timed out")) {
      return NetworkUtils.ErrorType.TIMEOUT;
    } else if (lowerCaseError.contains("server")) {
      return NetworkUtils.ErrorType.SERVER_ERROR;
    } else if (lowerCaseError.contains("authentication")
        || lowerCaseError.contains("auth")
        || lowerCaseError.contains("permission")
        || lowerCaseError.contains("access")) {
      return NetworkUtils.ErrorType.CLIENT_ERROR;
    }

    return NetworkUtils.ErrorType.UNKNOWN;
  }

  /** Clears the error message */
  public void clearError() {
    errorMessage.setValue(null);
  }

  /** Clears the success message */
  public void clearSuccess() {
    successMessage.setValue(null);
  }

  /**
   * Applies an update to a user object
   *
   * @param user The user to update
   * @param field The field to update
   * @param value The new value
   */
  @SuppressWarnings("unchecked")
  private void applyUpdateToUser(User user, String field, Object value) {
    if (user == null) {
      return;
    }

    switch (field) {
      case "username":
        if (value instanceof String) {
          user.setUsername((String) value);
        }
        break;
      case "email":
        if (value instanceof String) {
          user.setEmail((String) value);
        }
        break;
      case "profileImageUrl":
        if (value instanceof String) {
          user.setProfileImageUrl((String) value);
        }
        break;
      case "friendKeys":
        if (value instanceof Map) {
          user.setFriendKeys((Map<String, Boolean>) value);
        }
        break;
        // Add more fields as needed
    }
  }

  /** Clears all data */
  @Override
  protected void onCleared() {
    super.onCleared();
    // Clean up resources if needed
  }
}
