package com.example.partymaker.data.api;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.preference.PreferenceManager;
import com.example.partymaker.data.model.ChatMessage;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.data.model.User;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FirebaseServerClient {
  private static final String TAG = "FirebaseServerClient";
  private static FirebaseServerClient instance;
  private static final String DEFAULT_SERVER_URL = "http://10.0.2.2:8080"; // Default for emulator
  private static final String PREF_SERVER_URL = "server_url";

  private String serverUrl = DEFAULT_SERVER_URL;
  private final Gson gson = new Gson();
  private Context context;

  private FirebaseServerClient() {
    // Private constructor for singleton
  }

  public static synchronized FirebaseServerClient getInstance() {
    if (instance == null) {
      instance = new FirebaseServerClient();
    }
    return instance;
  }

  public void initialize(Context context) {
    this.context = context.getApplicationContext();
    loadServerUrl();
    Log.i(TAG, "FirebaseServerClient initialized with server URL: " + serverUrl);
  }

  private void loadServerUrl() {
    if (context != null) {
      serverUrl =
          PreferenceManager.getDefaultSharedPreferences(context)
              .getString(PREF_SERVER_URL, DEFAULT_SERVER_URL);
      Log.d(TAG, "Server URL loaded from preferences: " + serverUrl);
    } else {
      Log.w(TAG, "Context is null, using default server URL: " + DEFAULT_SERVER_URL);
      serverUrl = DEFAULT_SERVER_URL;
    }
  }

  // Groups methods
  public void getGroups(final DataCallback<Map<String, Group>> callback) {
    Log.d(TAG, "getGroups called");

    new AsyncTask<Void, Void, Map<String, Group>>() {
      private String errorMessage = null;

      @Override
      protected Map<String, Group> doInBackground(Void... params) {
        try {
          Log.d(TAG, "Making GET request to URL: " + serverUrl + "/api/firebase/Groups");
          String result = makeGetRequest("Groups");

          if (result != null) {
            Log.d(TAG, "Received groups data");
            try {
              Map<String, Group> groups = new HashMap<>();
              JSONObject jsonObject = new JSONObject(result);
              Iterator<String> keys = jsonObject.keys();

              while (keys.hasNext()) {
                String key = keys.next();
                JSONObject groupJson = jsonObject.getJSONObject(key);
                Group group = gson.fromJson(groupJson.toString(), Group.class);
                if (group.getGroupKey() == null) {
                  group.setGroupKey(key);
                }
                groups.put(key, group);
              }

              Log.d(TAG, "Successfully parsed " + groups.size() + " groups");
              return groups;
            } catch (Exception e) {
              Log.e(TAG, "Error parsing groups", e);
              errorMessage = e.getMessage();
              return new HashMap<>(); // Return empty map instead of null
            }
          } else {
            Log.e(TAG, "Failed to fetch groups, server may be down");
            errorMessage = "Failed to fetch groups";
            return new HashMap<>(); // Return empty map instead of null
          }
        } catch (Exception e) {
          Log.e(TAG, "Error fetching groups", e);
          errorMessage = "Error fetching groups: " + e.getMessage();
          return new HashMap<>(); // Return empty map instead of null
        }
      }

      @Override
      protected void onPostExecute(Map<String, Group> result) {
        if (result != null) {
          Log.d(TAG, "getGroups completed successfully with " + result.size() + " groups");
          callback.onSuccess(result);
        } else {
          Log.e(TAG, "getGroups failed: " + errorMessage);
          callback.onError(errorMessage != null ? errorMessage : "Failed to fetch groups");
        }
      }
    }.execute();
  }

  public void getGroup(String groupId, final DataCallback<Group> callback) {
    Log.d(TAG, "Starting getGroup for groupId: " + groupId);

    // Check if groupId is null or empty
    if (groupId == null || groupId.isEmpty()) {
      Log.e(TAG, "Invalid groupId: null or empty");
      callback.onError("Invalid group ID");
      return;
    }

    new AsyncTask<String, Void, Group>() {
      private String errorMessage = null;

      @Override
      protected Group doInBackground(String... params) {
        String groupId = params[0];
        try {
          // First try to fetch the group directly
          Log.d(TAG, "Fetching group directly: " + groupId);
          String groupJson = makeGetRequest("Groups/" + groupId, 15000);

          // If direct fetch fails, try to get it from the general groups list
          if (groupJson == null || groupJson.contains("\"status\":404")) {
            Log.d(TAG, "Group not found directly, trying to get from general groups list");
            String allGroupsResult = makeGetRequest("Groups");

            if (allGroupsResult != null) {
              try {
                JSONObject jsonObject = new JSONObject(allGroupsResult);

                // Check if the group exists in the general list
                if (jsonObject.has(groupId)) {
                  groupJson = jsonObject.getJSONObject(groupId).toString();
                  Log.d(TAG, "Found group in general list: " + groupId);
                } else {
                  Log.e(TAG, "Group not found in general list: " + groupId);
                  errorMessage = "Group not found";
                  return null;
                }
              } catch (Exception e) {
                Log.e(TAG, "Error parsing groups list", e);
                errorMessage = e.getMessage();
                return null;
              }
            } else {
              Log.e(TAG, "Failed to fetch groups list");
              errorMessage = "Failed to fetch groups";
              return null;
            }
          }

          if (groupJson == null) {
            Log.e(TAG, "Failed to fetch group: " + groupId);
            errorMessage = "Failed to fetch group";
            return null;
          }

          // Parse the group JSON
          Group group = gson.fromJson(groupJson, Group.class);
          Log.d(TAG, "Successfully parsed group: " + groupId);
          return group;
        } catch (Exception e) {
          Log.e(TAG, "Error fetching group", e);
          errorMessage = "Error fetching group: " + e.getMessage();
          return null;
        }
      }

      @Override
      protected void onPostExecute(Group result) {
        if (result != null) {
          Log.d(TAG, "getGroup completed successfully");
          callback.onSuccess(result);
        } else {
          Log.e(TAG, "getGroup failed: " + errorMessage);
          callback.onError(errorMessage != null ? errorMessage : "Failed to fetch group");
        }
      }
    }.execute(groupId);
  }

  public void saveGroup(String groupId, Group group, final OperationCallback callback) {
    new AsyncTask<Object, Void, Boolean>() {
      @Override
      protected Boolean doInBackground(Object... params) {
        String path = "Groups/" + params[0];
        Group groupObj = (Group) params[1];
        return makePostRequest(path, gson.toJson(groupObj));
      }

      @Override
      protected void onPostExecute(Boolean success) {
        if (success) {
          callback.onSuccess();
        } else {
          callback.onError("Failed to save group");
        }
      }
    }.execute(groupId, group);
  }

  public void updateGroup(
      String groupId, Map<String, Object> updates, final OperationCallback callback) {
    new AsyncTask<Object, Void, Boolean>() {
      @Override
      protected Boolean doInBackground(Object... params) {
        String path = "Groups/" + params[0];
        Map<String, Object> updatesObj = (Map<String, Object>) params[1];
        return makePutRequest(path, gson.toJson(updatesObj));
      }

      @Override
      protected void onPostExecute(Boolean success) {
        if (success) {
          callback.onSuccess();
        } else {
          callback.onError("Failed to update group");
        }
      }
    }.execute(groupId, updates);
  }

  public void deleteGroup(String groupId, final OperationCallback callback) {
    new AsyncTask<String, Void, Boolean>() {
      @Override
      protected Boolean doInBackground(String... params) {
        String path = "Groups/" + params[0];
        return makeDeleteRequest(path);
      }

      @Override
      protected void onPostExecute(Boolean success) {
        if (success) {
          callback.onSuccess();
        } else {
          callback.onError("Failed to delete group");
        }
      }
    }.execute(groupId);
  }

  // Add a method to update a specific field in a group
  public void updateGroup(
      String groupId, String field, Object value, final DataCallback<Void> callback) {
    Map<String, Object> updates = new HashMap<>();
    updates.put(field, value);

    new AsyncTask<Object, Void, Boolean>() {
      @Override
      protected Boolean doInBackground(Object... params) {
        String path = "Groups/" + params[0];
        Map<String, Object> updatesObj = (Map<String, Object>) params[1];
        return makePutRequest(path, gson.toJson(updatesObj));
      }

      @Override
      protected void onPostExecute(Boolean success) {
        if (success) {
          callback.onSuccess(null);
        } else {
          callback.onError("Failed to update group field");
        }
      }
    }.execute(groupId, updates);
  }

  // Users methods
  public void getUsers(final DataCallback<Map<String, User>> callback) {
    new AsyncTask<Void, Void, Map<String, User>>() {
      private String errorMessage = null;

      @Override
      protected Map<String, User> doInBackground(Void... voids) {
        String result = makeGetRequest("Users");
        if (result != null) {
          try {
            JSONObject jsonObject = new JSONObject(result);
            Map<String, User> users = new HashMap<>();

            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
              String key = keys.next();
              JSONObject userJson = jsonObject.getJSONObject(key);
              User user = gson.fromJson(userJson.toString(), User.class);
              users.put(key, user);
            }

            return users;
          } catch (JSONException e) {
            Log.e(TAG, "Error parsing users", e);
            errorMessage = e.getMessage();
            return null;
          }
        } else {
          errorMessage = "Failed to fetch users";
          return null;
        }
      }

      @Override
      protected void onPostExecute(Map<String, User> users) {
        if (users != null) {
          callback.onSuccess(users);
        } else {
          callback.onError(errorMessage != null ? errorMessage : "Failed to fetch users");
        }
      }
    }.execute();
  }

  public void getUser(String userId, final DataCallback<User> callback) {
    new AsyncTask<String, Void, User>() {
      private String errorMessage = null;

      @Override
      protected User doInBackground(String... params) {
        String result = makeGetRequest("Users/" + params[0]);
        if (result != null) {
          try {
            return gson.fromJson(result, User.class);
          } catch (Exception e) {
            Log.e(TAG, "Error parsing user", e);
            errorMessage = e.getMessage();
            return null;
          }
        } else {
          errorMessage = "Failed to fetch user";
          return null;
        }
      }

      @Override
      protected void onPostExecute(User user) {
        if (user != null) {
          callback.onSuccess(user);
        } else {
          callback.onError(errorMessage != null ? errorMessage : "Failed to fetch user");
        }
      }
    }.execute(userId);
  }

  public void saveUser(String userId, User user, final OperationCallback callback) {
    new AsyncTask<Object, Void, Boolean>() {
      @Override
      protected Boolean doInBackground(Object... params) {
        String path = "Users/" + params[0];
        User userObj = (User) params[1];
        return makePostRequest(path, gson.toJson(userObj));
      }

      @Override
      protected void onPostExecute(Boolean success) {
        if (success) {
          callback.onSuccess();
        } else {
          callback.onError("Failed to save user");
        }
      }
    }.execute(userId, user);
  }

  public void updateUser(
      String userId, Map<String, Object> updates, final OperationCallback callback) {
    new AsyncTask<Object, Void, Boolean>() {
      @Override
      protected Boolean doInBackground(Object... params) {
        String path = "Users/" + params[0];
        Map<String, Object> updatesObj = (Map<String, Object>) params[1];
        return makePutRequest(path, gson.toJson(updatesObj));
      }

      @Override
      protected void onPostExecute(Boolean success) {
        if (success) {
          callback.onSuccess();
        } else {
          callback.onError("Failed to update user");
        }
      }
    }.execute(userId, updates);
  }

  // Messages methods
  // Helper method to recursively extract message keys from nested nameValuePairs structure
  private void extractMessageKeys(JSONObject obj, Map<String, Boolean> messageKeys) {
    if (obj == null) return;

    Iterator<String> keys = obj.keys();
    while (keys.hasNext()) {
      String key = keys.next();

      if ("nameValuePairs".equals(key)) {
        // Recursively process nested nameValuePairs
        try {
          JSONObject nestedObj = obj.getJSONObject(key);
          extractMessageKeys(nestedObj, messageKeys);
        } catch (JSONException e) {
          Log.e(TAG, "Error processing nested nameValuePairs", e);
        }
      } else {
        // This is a message key, add it to the map
        try {
          Object value = obj.get(key);
          if (value instanceof String && ("true".equals(value) || "false".equals(value))) {
            messageKeys.put(key, Boolean.parseBoolean((String) value));
            Log.d(TAG, "Extracted message key: " + key + " = " + value);
          } else if (value instanceof Boolean) {
            messageKeys.put(key, (Boolean) value);
            Log.d(TAG, "Extracted message key: " + key + " = " + value);
          } else if (value instanceof JSONObject) {
            // This might be a nested structure, continue recursively
            extractMessageKeys((JSONObject) value, messageKeys);
          }
        } catch (JSONException e) {
          Log.e(TAG, "Error processing message key: " + key, e);
        }
      }
    }
  }

  public void getMessages(String groupId, final DataCallback<List<ChatMessage>> callback) {
    Log.d(TAG, "Starting getMessages for groupId: " + groupId);

    // Check if groupId is null or empty
    if (groupId == null || groupId.isEmpty()) {
      Log.e(TAG, "Invalid groupId: null or empty");
      callback.onError("Invalid group ID");
      return;
    }

    new AsyncTask<String, Void, List<ChatMessage>>() {
      private String errorMessage = null;

      @Override
      protected List<ChatMessage> doInBackground(String... params) {
        String groupId = params[0];
        try {
          // First, try to fetch the group to get message keys
          Log.d(TAG, "Fetching group to get message keys: " + groupId);
          String groupJson = makeGetRequest("Groups/" + groupId, 15000);

          // If direct fetch fails, try to get it from the general groups list
          if (groupJson == null || groupJson.contains("\"status\":404")) {
            Log.d(TAG, "Group not found directly, trying to get from general groups list");
            String allGroupsResult = makeGetRequest("Groups");

            if (allGroupsResult != null) {
              try {
                JSONObject jsonObject = new JSONObject(allGroupsResult);

                // Check if the group exists in the general list
                if (jsonObject.has(groupId)) {
                  groupJson = jsonObject.getJSONObject(groupId).toString();
                  Log.d(TAG, "Found group in general list for messages");
                } else {
                  Log.e(TAG, "Group not found in general list: " + groupId);
                  errorMessage = "Group not found";
                  return new ArrayList<>(); // Return empty list instead of null
                }
              } catch (Exception e) {
                Log.e(TAG, "Error parsing groups list", e);
                errorMessage = e.getMessage();
                return new ArrayList<>(); // Return empty list instead of null
              }
            } else {
              Log.e(TAG, "Failed to fetch groups list");
              errorMessage = "Failed to fetch groups";
              return new ArrayList<>(); // Return empty list instead of null
            }
          }

          if (groupJson == null) {
            Log.e(TAG, "Failed to fetch group: " + groupId);
            errorMessage = "Failed to fetch group";
            return new ArrayList<>(); // Return empty list instead of null
          }

          // Parse the group
          JSONObject groupObj = new JSONObject(groupJson);
          Map<String, Boolean> messageKeys = new HashMap<>();

          // Check if the group has message keys
          if (groupObj.has("MessageKeys")) {
            Log.d(TAG, "Group has MessageKeys");
            JSONObject messageKeysObj = groupObj.getJSONObject("MessageKeys");
            Log.d(TAG, "MessageKeys structure: " + messageKeysObj.toString());

            // Use the recursive method to extract message keys
            extractMessageKeys(messageKeysObj, messageKeys);

            Log.d(
                TAG, "Extracted " + messageKeys.size() + " message keys: " + messageKeys.keySet());
          } else {
            Log.d(TAG, "Group does not have MessageKeys");
          }

          // Fetch all messages from GroupsMessages
          Log.d(TAG, "Fetching all messages from path: GroupsMessages");
          String messagesJson = makeGetRequest("GroupsMessages", 15000);
          if (messagesJson == null) {
            Log.e(TAG, "Failed to fetch messages");
            errorMessage = "Failed to fetch messages";
            return new ArrayList<>(); // Return empty list instead of null
          }

          // Parse the messages
          List<ChatMessage> messages = new ArrayList<>();

          try {
            // Check if we have a JSON object or array
            if (messagesJson.trim().startsWith("{")) {
              // JSON Object - process each key as a potential message
              JSONObject messagesObj = new JSONObject(messagesJson);

              Iterator<String> keys = messagesObj.keys();
              while (keys.hasNext()) {
                String key = keys.next();
                Object value = messagesObj.get(key);

                if (value instanceof JSONObject) {
                  JSONObject messageObj = (JSONObject) value;

                  // Check if this message belongs to our group
                  String msgGroupId = messageObj.optString("groupId", "");
                  // Check both messageKey and MessageKey fields
                  String messageKey = messageObj.optString("messageKey", "");
                  if (messageKey.isEmpty()) {
                    messageKey = messageObj.optString("MessageKey", "");
                  }

                  Log.d(
                      TAG,
                      "Checking message with key: "
                          + messageKey
                          + ", groupId: "
                          + msgGroupId
                          + " against target groupId: "
                          + groupId);

                  if (groupId.equals(msgGroupId)
                      || messageKeys.containsKey(messageKey)
                      || messageKeys.containsKey(key)) {
                    ChatMessage message = gson.fromJson(messageObj.toString(), ChatMessage.class);
                    messages.add(message);
                    Log.d(TAG, "Added message with key: " + messageKey + " to results");
                  }
                } else if (value instanceof String) {
                  // This might be a single field message, check if it's a message for our group
                  // For now, skip string values as they're likely individual fields
                  Log.d(TAG, "Skipping string value for key: " + key);
                }
              }
            } else if (messagesJson.trim().startsWith("[")) {
              // Array of messages
              JSONArray messagesArray = new JSONArray(messagesJson);

              for (int i = 0; i < messagesArray.length(); i++) {
                JSONObject messageObj = messagesArray.getJSONObject(i);

                // Check if this message belongs to our group
                String msgGroupId = messageObj.optString("groupId", "");
                // Check both messageKey and MessageKey fields
                String messageKey = messageObj.optString("messageKey", "");
                if (messageKey.isEmpty()) {
                  messageKey = messageObj.optString("MessageKey", "");
                }

                if (groupId.equals(msgGroupId) || messageKeys.containsKey(messageKey)) {
                  ChatMessage message = gson.fromJson(messageObj.toString(), ChatMessage.class);
                  messages.add(message);
                  Log.d(TAG, "Added message with key: " + messageKey + " to results");
                }
              }
            } else {
              // Handle empty response or other formats
              Log.d(TAG, "Messages response is not in expected format: " + messagesJson);
            }
          } catch (JSONException e) {
            Log.e(TAG, "Error parsing messages JSON", e);
            // Try to parse as a single message if it's not an array
            try {
              JSONObject messageObj = new JSONObject(messagesJson);

              // Check if this message belongs to our group
              String msgGroupId = messageObj.optString("groupId", "");
              // Check both messageKey and MessageKey fields
              String messageKey = messageObj.optString("messageKey", "");
              if (messageKey.isEmpty()) {
                messageKey = messageObj.optString("MessageKey", "");
              }

              if (groupId.equals(msgGroupId) || messageKeys.containsKey(messageKey)) {
                ChatMessage message = gson.fromJson(messageObj.toString(), ChatMessage.class);
                messages.add(message);
                Log.d(TAG, "Added message with key: " + messageKey + " to results");
              }
            } catch (JSONException innerE) {
              Log.e(TAG, "Error parsing message as single object", innerE);
              errorMessage = "Error parsing messages: " + e.getMessage();
            }
          }

          Log.d(
              TAG, "Successfully processed " + messages.size() + " messages for group: " + groupId);
          return messages;
        } catch (Exception e) {
          Log.e(TAG, "Error fetching messages", e);
          errorMessage = "Error fetching messages: " + e.getMessage();
          return new ArrayList<>(); // Return empty list instead of null
        }
      }

      @Override
      protected void onPostExecute(List<ChatMessage> result) {
        if (result != null) {
          Log.d(TAG, "getMessages completed successfully with " + result.size() + " messages");
          callback.onSuccess(result);
        } else {
          Log.e(TAG, "getMessages failed: " + errorMessage);
          callback.onError(errorMessage != null ? errorMessage : "Failed to fetch messages");
        }
      }
    }.execute(groupId);
  }

  public void saveMessage(
      String groupId, String messageId, ChatMessage message, final OperationCallback callback) {

    // Check if groupId or messageId is null or empty
    if (groupId == null || groupId.isEmpty()) {
      Log.e(TAG, "Invalid groupId: null or empty");
      callback.onError("Invalid group ID");
      return;
    }

    if (messageId == null || messageId.isEmpty()) {
      Log.e(TAG, "Invalid messageId: null or empty");
      callback.onError("Invalid message ID");
      return;
    }

    Log.d(TAG, "Saving message with ID: " + messageId + " for group: " + groupId);
    Log.d(
        TAG,
        "Message content: "
            + message.getMessageText()
            + ", from user: "
            + message.getMessageUser());
    Log.d(TAG, "Server URL: " + serverUrl);

    // Set the groupId in the message object
    message.setGroupId(groupId);
    // Set the messageKey in the message object if not already set
    if (message.getMessageKey() == null || message.getMessageKey().isEmpty()) {
      message.setMessageKey(messageId);
    }

    // Use ExecutorService and Handler instead of AsyncTask
    Handler mainHandler = new Handler(Looper.getMainLooper());
    ExecutorService executor = Executors.newSingleThreadExecutor();

    executor.execute(
        () -> {
          String errorMessage = null;
          boolean success = false;

          try {
            // Save the message directly to GroupsMessages endpoint with messageId
            Log.d(TAG, "Making POST request to path: GroupsMessages/" + messageId);
            Log.d(
                TAG,
                "Making POST request to URL: "
                    + serverUrl
                    + "/api/firebase/GroupsMessages/"
                    + messageId
                    + " with body: "
                    + gson.toJson(message));
            boolean messageSaved =
                makePostRequest("GroupsMessages/" + messageId, gson.toJson(message), 15000);

            if (!messageSaved) {
              Log.e(TAG, "Failed to save message: " + messageId);
              errorMessage = "Failed to save message";
              success = false;
            } else {
              Log.d(TAG, "Message saved successfully: " + messageId);
              // Then update the group's message keys
              try {
                // Get the current group first
                Log.d(TAG, "Fetching group to update message keys: " + groupId);
                String groupJson = makeGetRequest("Groups/" + groupId, 15000);
                if (groupJson == null) {
                  Log.e(TAG, "Failed to fetch group for updating message keys");
                  errorMessage = "Failed to fetch group";
                  // Still return true since the message was saved
                  success = true;
                } else {
                  Log.d(TAG, "Group fetched successfully: " + groupId);
                  Log.d(TAG, "Group JSON: " + groupJson);
                  JSONObject groupObj = new JSONObject(groupJson);
                  JSONObject messageKeysObj;

                  // Check if the group already has message keys
                  if (groupObj.has("MessageKeys")) {
                    messageKeysObj = groupObj.getJSONObject("MessageKeys");
                    Log.d(TAG, "Group already has MessageKeys: " + messageKeysObj.toString());
                  } else {
                    messageKeysObj = new JSONObject();
                    Log.d(TAG, "Creating new MessageKeys object for group");
                  }

                  // Add the new message key
                  messageKeysObj.put(messageId, "true");
                  Log.d(TAG, "Added message key to MessageKeys: " + messageId);

                  // Update the group with the new message keys
                  Map<String, Object> updates = new HashMap<>();
                  updates.put("MessageKeys", messageKeysObj);

                  Log.d(TAG, "Updating group with new MessageKeys: " + gson.toJson(updates));
                  boolean groupUpdated =
                      makePutRequest("Groups/" + groupId, gson.toJson(updates), 15000);

                  if (!groupUpdated) {
                    Log.e(TAG, "Failed to update group message keys for: " + groupId);
                    errorMessage = "Failed to update group message keys";
                    // We still return true since the message was saved
                  } else {
                    Log.d(TAG, "Group message keys updated successfully for: " + groupId);
                  }

                  success = true;
                }
              } catch (JSONException e) {
                Log.e(TAG, "Error updating group message keys", e);
                errorMessage = "Error updating group message keys: " + e.getMessage();
                // We still return true since the message was saved
                success = true;
              }
            }
          } catch (Exception e) {
            Log.e(TAG, "Error saving message", e);
            errorMessage = "Error saving message: " + e.getMessage();
            e.printStackTrace();
            success = false;
          }

          // Post the result back to the main thread
          final boolean finalSuccess = success;
          final String finalErrorMessage = errorMessage;

          mainHandler.post(
              () -> {
                if (finalSuccess) {
                  Log.d(TAG, "Message save operation completed successfully for ID: " + messageId);
                  callback.onSuccess();
                } else {
                  Log.e(
                      TAG,
                      "Message save operation failed for ID: "
                          + messageId
                          + " with error: "
                          + finalErrorMessage);
                  callback.onError(
                      finalErrorMessage != null ? finalErrorMessage : "Failed to save message");
                }
              });
        });

    // Shutdown the executor when done
    executor.shutdown();
  }

  // Add a new saveMessage method that takes just a ChatMessage and DataCallback<Boolean>
  public void saveMessage(ChatMessage message, final DataCallback<Boolean> callback) {
    Log.d(TAG, "saveMessage called with ChatMessage: " + message.getMessageText());

    // Extract groupId and messageKey from the message
    String groupId = message.getGroupId();
    String messageKey = message.getMessageKey();

    if (groupId == null || groupId.isEmpty()) {
      Log.e(TAG, "saveMessage: groupId is null or empty");
      callback.onError("Missing groupId in message");
      return;
    }

    if (messageKey == null || messageKey.isEmpty()) {
      Log.e(TAG, "saveMessage: messageKey is null or empty");
      callback.onError("Missing messageKey in message");
      return;
    }

    // Call the existing saveMessage method with the extracted parameters
    saveMessage(
        groupId,
        messageKey,
        message,
        new OperationCallback() {
          @Override
          public void onSuccess() {
            Log.d(TAG, "Message saved successfully via wrapper method");
            callback.onSuccess(true);
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Error saving message via wrapper method: " + errorMessage);
            callback.onError(errorMessage);
          }
        });
  }

  public void deleteData(String path, final OperationCallback callback) {
    // Check if path is null or empty
    if (path == null || path.isEmpty()) {
      Log.e(TAG, "Invalid path: null or empty in deleteData");
      callback.onError("Invalid path");
      return;
    }

    new AsyncTask<String, Void, Boolean>() {
      @Override
      protected Boolean doInBackground(String... params) {
        return makeDeleteRequest(params[0]);
      }

      @Override
      protected void onPostExecute(Boolean success) {
        if (success) {
          callback.onSuccess();
        } else {
          callback.onError("Failed to delete data at path: " + path);
        }
      }
    }.execute(path);
  }

  public void getUserGroups(String userId, final DataCallback<Map<String, Group>> callback) {
    new AsyncTask<String, Void, Map<String, Group>>() {
      private String errorMessage = null;

      @Override
      protected Map<String, Group> doInBackground(String... params) {
        // Clean up the userId to replace spaces with proper encoding
        String cleanUserId = params[0].replace(" ", "%20");
        Log.d(TAG, "Getting groups for user: " + cleanUserId);

        // First try to get all groups
        String result = makeGetRequest("Groups");

        if (result != null) {
          try {
            JSONObject jsonObject = new JSONObject(result);
            Map<String, Group> userGroups = new HashMap<>();

            // Filter groups that the user is part of
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
              String key = keys.next();
              JSONObject groupJson = jsonObject.getJSONObject(key);
              Group group = gson.fromJson(groupJson.toString(), Group.class);

              // Check if user is in friendKeys or is the admin
              if ((group.getFriendKeys() != null && group.getFriendKeys().containsKey(params[0]))
                  || (group.getAdminKey() != null && group.getAdminKey().equals(params[0]))) {
                userGroups.put(key, group);
                Log.d(TAG, "Added group " + key + " to user's groups: " + group.getGroupName());
              }
            }

            Log.d(TAG, "Found " + userGroups.size() + " groups for user: " + params[0]);
            return userGroups;
          } catch (JSONException e) {
            Log.e(TAG, "Error parsing groups", e);
            errorMessage = e.getMessage();
            return new HashMap<>(); // Return empty map instead of null
          }
        } else {
          Log.w(TAG, "Failed to fetch groups, server may be down");
          return new HashMap<>(); // Return empty map instead of null
        }
      }

      @Override
      protected void onPostExecute(Map<String, Group> groups) {
        // Always return success, even with an empty map
        callback.onSuccess(groups != null ? groups : new HashMap<>());
      }
    }.execute(userId);
  }

  // Helper methods for HTTP requests
  private String makeGetRequest(String path) {
    return makeGetRequest(path, 10000); // Default timeout 10 seconds
  }

  private String makeGetRequest(String path, int timeout) {
    HttpURLConnection connection = null;
    BufferedReader reader = null;

    try {
      URL url = new URL(serverUrl + "/api/firebase/" + path);
      Log.d(TAG, "Making GET request to URL: " + url.toString());

      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setConnectTimeout(timeout);
      connection.setReadTimeout(timeout);

      Log.d(TAG, "Executing GET request to " + url.toString() + " with timeout: " + timeout + "ms");
      int responseCode = connection.getResponseCode();
      Log.d(TAG, "GET response code: " + responseCode + " for URL: " + url.toString());

      if (responseCode == HttpURLConnection.HTTP_OK) {
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
          response.append(line);
        }

        String result = response.toString();
        Log.d(TAG, "GET response length: " + result.length() + " chars for URL: " + url.toString());
        return result;
      } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
        Log.e(TAG, "GET request returned 404 Not Found for URL: " + url.toString());
        return null;
      } else {
        Log.e(
            TAG,
            "GET request failed with response code: "
                + responseCode
                + " for URL: "
                + url.toString());

        // Try to read error stream if available
        try {
          reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
          StringBuilder errorResponse = new StringBuilder();
          String line;

          while ((line = reader.readLine()) != null) {
            errorResponse.append(line);
          }

          if (errorResponse.length() > 0) {
            Log.e(TAG, "Error response: " + errorResponse.toString());
          }
        } catch (Exception e) {
          Log.e(TAG, "Could not read error stream", e);
        }

        return null;
      }
    } catch (Exception e) {
      Log.e(TAG, "Error making GET request to path: " + path, e);
      return null;
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          Log.e(TAG, "Error closing reader", e);
        }
      }
    }
  }

  private boolean makePostRequest(String path, String jsonBody) {
    return makePostRequest(path, jsonBody, 10000); // Default timeout 10 seconds
  }

  private boolean makePostRequest(String path, String jsonBody, int timeout) {
    HttpURLConnection connection = null;

    try {
      URL url = new URL(serverUrl + "/api/firebase/" + path);
      Log.d(TAG, "Making POST request to URL: " + url.toString() + " with body: " + jsonBody);

      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setConnectTimeout(timeout);
      connection.setReadTimeout(timeout);
      connection.setDoOutput(true);

      try (OutputStream os = connection.getOutputStream()) {
        byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
      }

      int responseCode = connection.getResponseCode();
      Log.d(TAG, "POST response code: " + responseCode + " for URL: " + url.toString());

      if (responseCode != HttpURLConnection.HTTP_OK
          && responseCode != HttpURLConnection.HTTP_CREATED) {
        // Try to read error stream if available
        try {
          BufferedReader reader =
              new BufferedReader(new InputStreamReader(connection.getErrorStream()));
          StringBuilder errorResponse = new StringBuilder();
          String line;

          while ((line = reader.readLine()) != null) {
            errorResponse.append(line);
          }

          if (errorResponse.length() > 0) {
            Log.e(TAG, "Error response: " + errorResponse.toString());
          }
        } catch (Exception e) {
          Log.e(TAG, "Could not read error stream", e);
        }

        // Try to read response headers
        try {
          Log.d(TAG, "Response headers:");
          for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
            if (header.getKey() != null) {
              Log.d(TAG, header.getKey() + ": " + header.getValue());
            }
          }
        } catch (Exception e) {
          Log.e(TAG, "Could not read response headers", e);
        }
      } else {
        // Read successful response
        try {
          BufferedReader reader =
              new BufferedReader(new InputStreamReader(connection.getInputStream()));
          StringBuilder response = new StringBuilder();
          String line;

          while ((line = reader.readLine()) != null) {
            response.append(line);
          }

          if (response.length() > 0) {
            Log.d(TAG, "Success response: " + response.toString());
          }
        } catch (Exception e) {
          Log.e(TAG, "Could not read response", e);
        }
      }

      return responseCode == HttpURLConnection.HTTP_OK
          || responseCode == HttpURLConnection.HTTP_CREATED;
    } catch (Exception e) {
      Log.e(TAG, "Error making POST request to path: " + path, e);
      e.printStackTrace();
      return false;
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  private boolean makePutRequest(String path, String jsonBody) {
    return makePutRequest(path, jsonBody, 10000); // Default timeout 10 seconds
  }

  private boolean makePutRequest(String path, String jsonBody, int timeout) {
    HttpURLConnection connection = null;

    try {
      URL url = new URL(serverUrl + "/api/firebase/" + path);

      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("PUT");
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setConnectTimeout(timeout);
      connection.setReadTimeout(timeout);
      connection.setDoOutput(true);

      try (OutputStream os = connection.getOutputStream()) {
        byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
      }

      int responseCode = connection.getResponseCode();
      return responseCode == HttpURLConnection.HTTP_OK
          || responseCode == HttpURLConnection.HTTP_CREATED;
    } catch (Exception e) {
      Log.e(TAG, "Error making PUT request", e);
      return false;
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  private boolean makeDeleteRequest(String path) {
    return makeDeleteRequest(path, 10000); // Default timeout 10 seconds
  }

  private boolean makeDeleteRequest(String path, int timeout) {
    HttpURLConnection connection = null;

    try {
      URL url = new URL(serverUrl + "/api/firebase/" + path);

      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("DELETE");
      connection.setConnectTimeout(timeout);
      connection.setReadTimeout(timeout);

      int responseCode = connection.getResponseCode();
      return responseCode == HttpURLConnection.HTTP_OK
          || responseCode == HttpURLConnection.HTTP_NO_CONTENT;
    } catch (Exception e) {
      Log.e(TAG, "Error making DELETE request", e);
      return false;
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  // Callback interfaces
  public interface DataCallback<T> {
    void onSuccess(T data);

    void onError(String errorMessage);
  }

  public interface OperationCallback {
    void onSuccess();

    void onError(String errorMessage);
  }
}
