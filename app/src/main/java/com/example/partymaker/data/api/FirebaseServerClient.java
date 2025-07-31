package com.example.partymaker.data.api;

import android.annotation.SuppressLint;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
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

/**
 * API client for communicating with the PartyMaker backend server (Spring Boot). Handles CRUD
 * operations for groups, users, and messages via HTTP requests. Implements singleton pattern for
 * global access.
 */
public class FirebaseServerClient {
    /**
     * Tag for logging.
     */
    private static final String TAG = "FirebaseServerClient";
    /**
     * Default server URL (for emulator).
     */
    private static final String DEFAULT_SERVER_URL =
            "https://partymaker.onrender.com"; // Changed to Render URL
    /**
     * SharedPreferences key for server URL.
     */
    private static final String PREF_SERVER_URL = "server_url";
    /**
     * Singleton instance.
     */
    private static FirebaseServerClient instance;
    /**
     * Gson instance for JSON serialization.
     */
    private final Gson gson = new Gson();
    /**
     * ExecutorService for background tasks.
     */
    private final ExecutorService executor = Executors.newCachedThreadPool();
    /**
     * Handler for posting to main thread.
     */
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    /**
     * NetworkManager instance for network operations.
     */
    private final NetworkManager networkManager = NetworkManager.getInstance();
    /**
     * The current server URL.
     */
    private String serverUrl = DEFAULT_SERVER_URL;
    /**
     * Application context - using WeakReference to prevent memory leaks.
     */
    private WeakReference<Context> contextRef;

    /**
     * Private constructor for singleton pattern.
     */
    private FirebaseServerClient() {
        // Private constructor for singleton
    }

    /**
     * Gets the singleton instance of the client.
     *
     * @return the instance
     */
    public static synchronized FirebaseServerClient getInstance() {
        if (instance == null) {
            instance = new FirebaseServerClient();
        }
        return instance;
    }

    /**
     * Initializes the client with application context and loads the server URL.
     *
     * @param context the application context
     */
    public void initialize(Context context) {
        this.contextRef = new WeakReference<>(context.getApplicationContext());
        loadServerUrl();
        Log.i(TAG, "FirebaseServerClient initialized with server URL: " + serverUrl);
    }

    /**
     * Gets the context from the WeakReference
     *
     * @return the context or null if it has been garbage collected
     */
    private Context getContext() {
        return contextRef != null ? contextRef.get() : null;
    }

    /**
     * Loads the server URL from SharedPreferences or uses the default.
     */
    private void loadServerUrl() {
        Context context = getContext();
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

    // Helper method to log API calls
    private void logApiCall(String method, String path) {
        Log.d(TAG, String.format("API %s: %s/api/firebase/%s", method, serverUrl, path));
    }

    // Groups methods
    public void getGroups(final DataCallback<Map<String, Group>> callback) {
        Log.d(TAG, "getGroups called");
        logApiCall("GET", "Groups");

        Context context = getContext();
        if (context == null) {
            Log.e(TAG, "Context is null");
            mainHandler.post(() -> callback.onError("Internal error: Context is null"));
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.e(TAG, "Network not available");
            mainHandler.post(
                    () ->
                            callback.onError(
                                    "No network connection available. Please check your internet connection."));
            return;
        }

        NetworkUtils.executeWithRetry(
                () -> {
                    String result = makeGetRequest("Groups");
                    if (result == null) {
                        throw new IOException("Failed to fetch groups data");
                    }

                    Map<String, Group> groups = new HashMap<>();
                    JSONObject jsonObject = new JSONObject(result);
                    Iterator<String> keys = jsonObject.keys();

                    while (keys.hasNext()) {
                        String key = keys.next();
                        JSONObject groupJson = jsonObject.getJSONObject(key);
                        Group group = gson.fromJson(groupJson.toString(), Group.class);
                        groups.put(key, group);
                    }

                    Log.d(TAG, "Successfully parsed " + groups.size() + " groups");
                    return groups;
                },
                new NetworkUtils.RetryCallback<>() {
                    @Override
                    public void onSuccess(Map<String, Group> result) {
                        Log.d(TAG, "getGroups completed successfully with " + result.size() + " groups");
                        callback.onSuccess(result);
                    }

                    @Override
                    public void onFailure(NetworkUtils.ErrorType errorType, String errorMessage) {
                        String userFriendlyError = NetworkUtils.getErrorMessage(errorType);
                        Log.e(TAG, "getGroups failed: " + errorMessage + " (" + errorType + ")");
                        callback.onError(userFriendlyError);
                    }

                    @Override
                    public void onRetry(int attemptCount, Exception e) {
                        Log.w(TAG, "Retrying getGroups (attempt " + attemptCount + "): " + e.getMessage());
                    }
                });
    }

    public void getGroup(String groupId, final DataCallback<Group> callback) {
        Log.d(TAG, "Starting getGroup for groupId: " + groupId);

        // Check if groupId is null or empty
        if (groupId == null || groupId.isEmpty()) {
            Log.e(TAG, "Invalid groupId: null or empty");
            mainHandler.post(() -> callback.onError("Invalid group ID"));
            return;
        }

        Context context = getContext();
        if (context == null) {
            Log.e(TAG, "Context is null");
            mainHandler.post(() -> callback.onError("Internal error: Context is null"));
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.e(TAG, "Network not available");
            mainHandler.post(
                    () ->
                            callback.onError(
                                    "No network connection available. Please check your internet connection."));
            return;
        }

        NetworkUtils.executeWithRetry(
                () -> {
                    // First try to fetch the group directly
                    Log.d(TAG, "Fetching group directly: " + groupId);
                    String groupJson = makeGetRequest("Groups/" + groupId, 15000);

                    // If direct fetch fails, try to get it from the general groups list
                    if (groupJson == null || groupJson.contains("\"status\":404")) {
                        Log.d(TAG, "Group not found directly, trying to get from general groups list");
                        String allGroupsResult = makeGetRequest("Groups");

                        if (allGroupsResult != null) {
                            JSONObject jsonObject = new JSONObject(allGroupsResult);

                            // Check if the group exists in the general list
                            if (jsonObject.has(groupId)) {
                                groupJson = jsonObject.getJSONObject(groupId).toString();
                                Log.d(TAG, "Found group in general list: " + groupId);
                            } else {
                                Log.e(TAG, "Group not found in general list: " + groupId);
                                throw new IOException("Group not found");
                            }
                        } else {
                            throw new IOException("Failed to fetch groups list");
                        }
                    }

                    Group group = gson.fromJson(groupJson, Group.class);
                    if (group == null) {
                        throw new IOException("Failed to parse group data");
                    }

                    // Set the group key if it's not already set
                    if (group.getGroupKey().isEmpty()) {
                        group.setGroupKey(groupId);
                    }

                    return group;
                },
                new NetworkUtils.RetryCallback<>() {
                    @Override
                    public void onSuccess(Group result) {
                        Log.d(TAG, "getGroup completed successfully: " + result.getGroupName());
                        callback.onSuccess(result);
                    }

                    @Override
                    public void onFailure(NetworkUtils.ErrorType errorType, String errorMessage) {
                        String userFriendlyError = NetworkUtils.getErrorMessage(errorType);
                        Log.e(TAG, "getGroup failed: " + errorMessage + " (" + errorType + ")");
                        callback.onError(userFriendlyError);
                    }

                    @Override
                    public void onRetry(int attemptCount, Exception e) {
                        Log.w(TAG, "Retrying getGroup (attempt " + attemptCount + "): " + e.getMessage());
                    }
                });
    }

    public void saveGroup(String groupId, Group group, final OperationCallback callback) {
        Log.d(TAG, "saveGroup called for groupId: " + groupId);

        Context context = getContext();
        if (context == null) {
            Log.e(TAG, "Context is null");
            mainHandler.post(() -> callback.onError("Internal error: Context is null"));
            return;
        }

        if (groupId == null || groupId.isEmpty() || group == null) {
            Log.e(TAG, "Invalid parameters for saveGroup");
            mainHandler.post(() -> callback.onError("Invalid group data"));
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.e(TAG, "Network not available");
            mainHandler.post(
                    () ->
                            callback.onError(
                                    "No network connection available. Please check your internet connection."));
            return;
        }

        NetworkUtils.executeWithRetry(
                () -> {
                    String jsonBody = gson.toJson(group);
                    // Use PUT instead of POST since the server only allows PUT for this endpoint
                    boolean success = makePutRequest("Groups/" + groupId, jsonBody);
                    if (!success) {
                        throw new IOException("Failed to save group");
                    }
                    return true;
                },
                new NetworkUtils.RetryCallback<>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        Log.d(TAG, "saveGroup completed successfully");
                        callback.onSuccess();
                    }

                    @Override
                    public void onFailure(NetworkUtils.ErrorType errorType, String errorMessage) {
                        String userFriendlyError = NetworkUtils.getErrorMessage(errorType);
                        Log.e(TAG, "saveGroup failed: " + errorMessage + " (" + errorType + ")");
                        callback.onError(userFriendlyError);
                    }

                    @Override
                    public void onRetry(int attemptCount, Exception e) {
                        Log.w(TAG, "Retrying saveGroup (attempt " + attemptCount + "): " + e.getMessage());
                    }
                });
    }

    public void updateGroup(
            String groupId, Map<String, Object> updates, final OperationCallback callback) {
        Log.d(TAG, "updateGroup called for groupId: " + groupId);

        Context context = getContext();
        if (context == null) {
            Log.e(TAG, "Context is null");
            mainHandler.post(() -> callback.onError("Internal error: Context is null"));
            return;
        }

        if (groupId == null || groupId.isEmpty() || updates == null || updates.isEmpty()) {
            Log.e(TAG, "Invalid parameters for updateGroup");
            mainHandler.post(() -> callback.onError("Invalid update data"));
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.e(TAG, "Network not available");
            mainHandler.post(
                    () ->
                            callback.onError(
                                    "No network connection available. Please check your internet connection."));
            return;
        }

        NetworkUtils.executeWithRetry(
                () -> {
                    String jsonBody = gson.toJson(updates);
                    boolean success = makePutRequest("Groups/" + groupId, jsonBody);
                    if (!success) {
                        throw new IOException("Failed to update group");
                    }
                    return true;
                },
                new NetworkUtils.RetryCallback<>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        Log.d(TAG, "updateGroup completed successfully");
                        callback.onSuccess();
                    }

                    @Override
                    public void onFailure(NetworkUtils.ErrorType errorType, String errorMessage) {
                        String userFriendlyError = NetworkUtils.getErrorMessage(errorType);
                        Log.e(TAG, "updateGroup failed: " + errorMessage + " (" + errorType + ")");
                        callback.onError(userFriendlyError);
                    }

                    @Override
                    public void onRetry(int attemptCount, Exception e) {
                        Log.w(TAG, "Retrying updateGroup (attempt " + attemptCount + "): " + e.getMessage());
                    }
                });
    }

    public void deleteGroup(String groupId, final OperationCallback callback) {
        Log.d(TAG, "deleteGroup called for groupId: " + groupId);

        Context context = getContext();
        if (context == null) {
            Log.e(TAG, "Context is null");
            mainHandler.post(() -> callback.onError("Internal error: Context is null"));
            return;
        }

        if (groupId == null || groupId.isEmpty()) {
            Log.e(TAG, "Invalid groupId for deleteGroup");
            mainHandler.post(() -> callback.onError("Invalid group ID"));
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.e(TAG, "Network not available");
            mainHandler.post(
                    () ->
                            callback.onError(
                                    "No network connection available. Please check your internet connection."));
            return;
        }

        NetworkUtils.executeWithRetry(
                () -> {
                    boolean success = makeDeleteRequest("Groups/" + groupId);
                    if (!success) {
                        throw new IOException("Failed to delete group");
                    }
                    return true;
                },
                new NetworkUtils.RetryCallback<>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        Log.d(TAG, "deleteGroup completed successfully");
                        callback.onSuccess();
                    }

                    @Override
                    public void onFailure(NetworkUtils.ErrorType errorType, String errorMessage) {
                        String userFriendlyError = NetworkUtils.getErrorMessage(errorType);
                        Log.e(TAG, "deleteGroup failed: " + errorMessage + " (" + errorType + ")");
                        callback.onError(userFriendlyError);
                    }

                    @Override
                    public void onRetry(int attemptCount, Exception e) {
                        Log.w(TAG, "Retrying deleteGroup (attempt " + attemptCount + "): " + e.getMessage());
                    }
                });
    }

    public void updateGroup(
            String groupId, String field, Object value, final DataCallback<Void> callback) {
        Log.d(TAG, "updateGroup field called for groupId: " + groupId + ", field: " + field);

        Context context = getContext();
        if (context == null) {
            Log.e(TAG, "Context is null");
            mainHandler.post(() -> callback.onError("Internal error: Context is null"));
            return;
        }

        if (groupId == null || groupId.isEmpty() || field == null || field.isEmpty()) {
            Log.e(TAG, "Invalid parameters for updateGroup field");
            mainHandler.post(() -> callback.onError("Invalid update data"));
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put(field, value);

        updateGroup(
                groupId,
                updates,
                new OperationCallback() {
                    @Override
                    public void onSuccess() {
                        callback.onSuccess(null);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        callback.onError(errorMessage);
                    }
                });
    }

    // Users methods
    public void getUsers(final DataCallback<Map<String, User>> callback) {
        Log.d(TAG, "getUsers called");
        logApiCall("GET", "Users");

        Context context = getContext();
        if (context == null) {
            Log.e(TAG, "Context is null");
            mainHandler.post(() -> callback.onError("Internal error: Context is null"));
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.e(TAG, "Network not available");
            mainHandler.post(
                    () ->
                            callback.onError(
                                    "No network connection available. Please check your internet connection."));
            return;
        }

        NetworkUtils.executeWithRetry(
                () -> {
                    String result = makeGetRequest("Users");
                    if (result == null) {
                        throw new IOException("Failed to fetch users data");
                    }

                    Map<String, User> users = new HashMap<>();
                    JSONObject jsonObject = new JSONObject(result);
                    Iterator<String> keys = jsonObject.keys();

                    while (keys.hasNext()) {
                        String key = keys.next();
                        JSONObject userJson = jsonObject.getJSONObject(key);
                        User user = gson.fromJson(userJson.toString(), User.class);
                        users.put(key, user);
                    }

                    Log.d(TAG, "Successfully parsed " + users.size() + " users");
                    return users;
                },
                new NetworkUtils.RetryCallback<>() {
                    @Override
                    public void onSuccess(Map<String, User> result) {
                        Log.d(TAG, "getUsers completed successfully with " + result.size() + " users");
                        callback.onSuccess(result);
                    }

                    @Override
                    public void onFailure(NetworkUtils.ErrorType errorType, String errorMessage) {
                        String userFriendlyError = NetworkUtils.getErrorMessage(errorType);
                        Log.e(TAG, "getUsers failed: " + errorMessage + " (" + errorType + ")");
                        callback.onError(userFriendlyError);
                    }

                    @Override
                    public void onRetry(int attemptCount, Exception e) {
                        Log.w(TAG, "Retrying getUsers (attempt " + attemptCount + "): " + e.getMessage());
                    }
                });
    }

    public void getUser(String userId, final DataCallback<User> callback) {
        // Make AsyncTask static to prevent memory leaks
        final String serverUrl = this.serverUrl;
        final Gson gson = this.gson;
        final Handler mainHandler = this.mainHandler;

        new UserAsyncTask(serverUrl, gson, mainHandler, callback).execute(userId);
    }

    public void saveUser(String userId, User user, final OperationCallback callback) {
        // Make AsyncTask static to prevent memory leaks
        final String serverUrl = this.serverUrl;
        final Gson gson = this.gson;
        final Handler mainHandler = this.mainHandler;

        new SaveUserAsyncTask(serverUrl, gson, mainHandler, callback).execute(userId, user);
    }

    public void updateUser(
            String userId, Map<String, Object> updates, final OperationCallback callback) {
        // Make AsyncTask static to prevent memory leaks
        final String serverUrl = this.serverUrl;
        final Gson gson = this.gson;
        final Handler mainHandler = this.mainHandler;

        new UpdateUserAsyncTask(serverUrl, gson, mainHandler, callback).execute(userId, updates);
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
                    if (("true".equals(value) || "false".equals(value))) {
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

        Context context = getContext();
        if (context == null) {
            Log.e(TAG, "Context is null");
            mainHandler.post(() -> callback.onError("Internal error: Context is null"));
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.e(TAG, "Network not available");
            mainHandler.post(
                    () ->
                            callback.onError(
                                    "No network connection available. Please check your internet connection."));
            return;
        }

        NetworkUtils.executeWithRetry(
                () -> {
                    // First, try to fetch the group to get message keys
                    Log.d(TAG, "Fetching group to get message keys: " + groupId);
                    String groupJson = makeGetRequest("Groups/" + groupId, 15000);

                    // If direct fetch fails, try to get it from the general groups list
                    if (groupJson == null || groupJson.contains("\"status\":404")) {
                        Log.d(TAG, "Group not found directly, trying to get from general groups list");
                        String allGroupsResult = makeGetRequest("Groups");

                        if (allGroupsResult != null) {
                            JSONObject jsonObject = new JSONObject(allGroupsResult);

                            // Check if the group exists in the general list
                            if (jsonObject.has(groupId)) {
                                groupJson = jsonObject.getJSONObject(groupId).toString();
                                Log.d(TAG, "Found group in general list for messages");
                            } else {
                                Log.e(TAG, "Group not found in general list: " + groupId);
                                throw new IOException("Group not found");
                            }
                        } else {
                            throw new IOException("Failed to fetch groups list");
                        }
                    }

                    // Parse the group
                    JSONObject groupObj = new JSONObject(groupJson);
                    Map<String, Boolean> messageKeys = new HashMap<>();

                    // Check if the group has message keys
                    if (groupObj.has("MessageKeys")) {
                        Log.d(TAG, "Group has MessageKeys");
                        JSONObject messageKeysObj = groupObj.getJSONObject("MessageKeys");
                        Log.d(TAG, "MessageKeys structure: " + messageKeysObj);

                        // Use the recursive method to extract message keys
                        extractMessageKeys(messageKeysObj, messageKeys);

                        Log.d(
                                TAG, "Extracted " + messageKeys.size() + " message keys: " + messageKeys.keySet());
                    } else {
                        Log.d(TAG, "Group does not have MessageKeys");
                        return new ArrayList<>(); // No messages to fetch
                    }

                    if (messageKeys.isEmpty()) {
                        Log.d(TAG, "No message keys found for group: " + groupId);
                        return new ArrayList<>(); // No messages to fetch
                    }

                    // Try to fetch messages directly for this group
                    Log.d(TAG, "Fetching messages for group: " + groupId);
                    String messagesPath = "GroupsMessages?groupId=" + groupId;
                    String messagesJson = makeGetRequest(messagesPath, 15000);

                    // If direct query doesn't work (depends on server implementation),
                    // fall back to fetching specific messages by keys
                    if (messagesJson == null
                            || messagesJson.trim().equals("{}")
                            || messagesJson.trim().equals("[]")) {
                        Log.d(TAG, "No messages found with direct query, fetching by keys");
                        List<ChatMessage> messages = new ArrayList<>();

                        // Fetch each message individually by key
                        for (String messageKey : messageKeys.keySet()) {
                            String messagePath = "GroupsMessages/" + messageKey;
                            String messageJson = makeGetRequest(messagePath, 5000);

                            if (messageJson != null && !messageJson.trim().equals("{}")) {
                                try {
                                    ChatMessage message = gson.fromJson(messageJson, ChatMessage.class);
                                    if (message != null) {
                                        // Ensure the message has the key set
                                        if (message.getMessageKey().isEmpty()) {
                                            message.setMessageKey(messageKey);
                                        }
                                        messages.add(message);
                                        Log.d(TAG, "Added message with key: " + messageKey);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing message: " + messageKey, e);
                                }
                            }
                        }

                        Log.d(TAG, "Successfully fetched " + messages.size() + " messages by keys");
                        return messages;
                    }

                    // Parse the messages from direct query
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

                                    if (groupId.equals(msgGroupId)
                                            || messageKeys.containsKey(messageKey)
                                            || messageKeys.containsKey(key)) {
                                        ChatMessage message = gson.fromJson(messageObj.toString(), ChatMessage.class);
                                        messages.add(message);
                                        Log.d(TAG, "Added message with key: " + messageKey + " to results");
                                    }
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
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing messages JSON", e);
                        return new ArrayList<>();
                    }

                    Log.d(
                            TAG, "Successfully processed " + messages.size() + " messages for group: " + groupId);
                    return messages;
                },
                new NetworkUtils.RetryCallback<List<ChatMessage>>() {
                    @Override
                    public void onSuccess(List<ChatMessage> result) {
                        Log.d(TAG, "getMessages completed successfully with " + result.size() + " messages");
                        callback.onSuccess(result);
                    }

                    @Override
                    public void onFailure(NetworkUtils.ErrorType errorType, String errorMessage) {
                        String userFriendlyError = NetworkUtils.getErrorMessage(errorType);
                        Log.e(TAG, "getMessages failed: " + errorMessage + " (" + errorType + ")");
                        callback.onError(userFriendlyError);
                    }

                    @Override
                    public void onRetry(int attemptCount, Exception e) {
                        Log.w(TAG, "Retrying getMessages (attempt " + attemptCount + "): " + e.getMessage());
                    }
                });
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
        if (message.getMessageKey().isEmpty()) {
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
                                        Log.d(TAG, "Group already has MessageKeys: " + messageKeysObj);
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
                                    callback.onError(finalErrorMessage);
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

        if (messageKey.isEmpty()) {
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

    @SuppressLint("StaticFieldLeak")
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

    /**
     * Updates data at a specific path. If value is null, the data will be deleted.
     *
     * @param path     the path to update
     * @param value    the value to set, or null to delete
     * @param callback the callback to handle success or failure
     */
    public void updateData(String path, Object value, final OperationCallback callback) {
        Log.d(TAG, "updateData called for path: " + path);

        Context context = getContext();
        if (context == null) {
            Log.e(TAG, "Context is null");
            mainHandler.post(() -> callback.onError("Internal error: Context is null"));
            return;
        }

        if (path == null || path.isEmpty()) {
            Log.e(TAG, "Invalid path for updateData");
            mainHandler.post(() -> callback.onError("Invalid path"));
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.e(TAG, "Network not available");
            mainHandler.post(
                    () ->
                            callback.onError(
                                    "No network connection available. Please check your internet connection."));
            return;
        }

        NetworkUtils.executeWithRetry(
                () -> {
                    boolean success;
                    if (value == null) {
                        // If value is null, delete the data
                        success = makeDeleteRequest(path);
                    } else {
                        // Otherwise update with the new value
                        String jsonBody = gson.toJson(value);
                        success = makePutRequest(path, jsonBody);
                    }

                    if (!success) {
                        throw new IOException("Failed to update data at " + path);
                    }
                    return true;
                },
                new NetworkUtils.RetryCallback<>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        Log.d(TAG, "updateData completed successfully for path: " + path);
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }

                    @Override
                    public void onFailure(NetworkUtils.ErrorType errorType, String errorMessage) {
                        String userFriendlyError = NetworkUtils.getErrorMessage(errorType);
                        Log.e(TAG, "updateData failed: " + errorMessage + " (" + errorType + ")");
                        if (callback != null) {
                            callback.onError(userFriendlyError);
                        }
                    }

                    @Override
                    public void onRetry(int attemptCount, Exception e) {
                        Log.w(TAG, "Retrying updateData (attempt " + attemptCount + "): " + e.getMessage());
                    }
                });
    }

    @SuppressLint("StaticFieldLeak")
    public void getUserGroups(String userId, final DataCallback<Map<String, Group>> callback) {
        // Check network availability first
        Context context = getContext();
        if (context == null) {
            Log.e(TAG, "Context is null");
            mainHandler.post(() -> callback.onError("Internal error: Context is null"));
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.e(TAG, "Network not available");
            mainHandler.post(
                    () -> callback.onError(NetworkUtils.getErrorMessage(NetworkUtils.ErrorType.NO_NETWORK)));
            return;
        }

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
                        return null; // Return null to indicate error
                    }
                } else {
                    Log.w(TAG, "Failed to fetch groups, server may be down");
                    errorMessage = "Failed to connect to server. Please check your internet connection.";
                    return null; // Return null to indicate error
                }
            }

            @Override
            protected void onPostExecute(Map<String, Group> groups) {
                if (groups != null) {
                    callback.onSuccess(groups);
                } else {
                    callback.onError(errorMessage != null ? errorMessage : "Unknown error occurred");
                }
            }
        }.execute(userId);
    }

    // Helper methods for HTTP requests
    private String makeGetRequest(String path) {
        return makeGetRequest(path, 10000); // Default timeout 10 seconds
    }

    private String makeGetRequest(String path, int timeout) {
        logApiCall("GET", path);
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(serverUrl + "/api/firebase/" + path);
            Log.d(TAG, "Making GET request to URL: " + url);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);

            Log.d(TAG, "Executing GET request to " + url + " with timeout: " + timeout + "ms");
            int responseCode = connection.getResponseCode();
            Log.d(TAG, "GET response code: " + responseCode + " for URL: " + url);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                String result = response.toString();
                Log.d(TAG, "GET response length: " + result.length() + " chars for URL: " + url);
                return result;
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                Log.e(TAG, "GET request returned 404 Not Found for URL: " + url);
                return null;
            } else {
                Log.e(TAG, "GET request failed with response code: " + responseCode + " for URL: " + url);

                // Try to read error stream if available
                try {
                    reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        errorResponse.append(line);
                    }

                    if (errorResponse.length() > 0) {
                        Log.e(TAG, "Error response: " + errorResponse);
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
        logApiCall("POST", path);
        HttpURLConnection connection = null;

        try {
            URL url = new URL(serverUrl + "/api/firebase/" + path);
            Log.d(TAG, "Making POST request to URL: " + url + " with body: " + jsonBody);

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
            Log.d(TAG, "POST response code: " + responseCode + " for URL: " + url);

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
                        Log.e(TAG, "Error response: " + errorResponse);
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
                        Log.d(TAG, "Success response: " + response);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Could not read response", e);
                }
            }

            return responseCode == HttpURLConnection.HTTP_OK
                    || responseCode == HttpURLConnection.HTTP_CREATED;
        } catch (Exception e) {
            Log.e(TAG, "Error making POST request to path: " + path, e);
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
        logApiCall("PUT", path);
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
        logApiCall("DELETE", path);
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

    /**
     * Cleanup resources when the app is shutting down
     */
    public void cleanup() {
        Log.d(TAG, "Cleaning up FirebaseServerClient resources");
        executor.shutdownNow();
        NetworkUtils.cancelAllOperations();
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

    // Static AsyncTask class for getUser
    private static class UserAsyncTask extends AsyncTask<String, Void, User> {
        private final String serverUrl;
        private final Gson gson;
        private final Handler mainHandler;
        private final DataCallback<User> callback;
        private String errorMessage = null;

        public UserAsyncTask(
                String serverUrl, Gson gson, Handler mainHandler, DataCallback<User> callback) {
            this.serverUrl = serverUrl;
            this.gson = gson;
            this.mainHandler = mainHandler;
            this.callback = callback;
        }

        @Override
        protected User doInBackground(String... params) {
            String result = makeGetRequest("Users/" + params[0]);
            if (result != null) {
                try {
                    return gson.fromJson(result, User.class);
                } catch (Exception e) {
                    Log.e("FirebaseServerClient", "Error parsing user", e);
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
                mainHandler.post(() -> callback.onSuccess(user));
            } else {
                mainHandler.post(
                        () -> callback.onError(errorMessage != null ? errorMessage : "Failed to fetch user"));
            }
        }

        // Helper method for making GET requests
        private String makeGetRequest(String path) {
            return makeGetRequest(path, 15000);
        }

        private String makeGetRequest(String path, int timeout) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(serverUrl + "/api/firebase/" + path);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(timeout);
                connection.setReadTimeout(timeout);

                int responseCode = connection.getResponseCode();
                Log.d("FirebaseServerClient", "GET response code: " + responseCode + " for URL: " + url);

                if (responseCode >= 200 && responseCode < 300) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    return result.toString();
                } else {
                    Log.e("FirebaseServerClient", "GET request failed with response code: " + responseCode);
                    return null;
                }
            } catch (Exception e) {
                Log.e("FirebaseServerClient", "Error making GET request", e);
                errorMessage = e.getMessage();
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e("FirebaseServerClient", "Error closing reader", e);
                    }
                }
            }
        }
    }

    // Static AsyncTask class for saveUser
    private static class SaveUserAsyncTask extends AsyncTask<Object, Void, Boolean> {
        private final String serverUrl;
        private final Gson gson;
        private final Handler mainHandler;
        private final OperationCallback callback;

        public SaveUserAsyncTask(
                String serverUrl, Gson gson, Handler mainHandler, OperationCallback callback) {
            this.serverUrl = serverUrl;
            this.gson = gson;
            this.mainHandler = mainHandler;
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            String path = "Users/" + params[0];
            User userObj = (User) params[1];
            return makePostRequest(path, gson.toJson(userObj));
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                mainHandler.post(callback::onSuccess);
            } else {
                mainHandler.post(() -> callback.onError("Failed to save user"));
            }
        }

        // Helper method for making POST requests
        private boolean makePostRequest(String path, String jsonBody) {
            return makePostRequest(path, jsonBody, 15000);
        }

        private boolean makePostRequest(String path, String jsonBody, int timeout) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(serverUrl + "/api/firebase/" + path);
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
                Log.d("FirebaseServerClient", "POST response code: " + responseCode + " for URL: " + url);
                return responseCode >= 200 && responseCode < 300;
            } catch (Exception e) {
                Log.e("FirebaseServerClient", "Error making POST request", e);
                return false;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }

    // Static AsyncTask class for updateUser
    private static class UpdateUserAsyncTask extends AsyncTask<Object, Void, Boolean> {
        private final String serverUrl;
        private final Gson gson;
        private final Handler mainHandler;
        private final OperationCallback callback;

        public UpdateUserAsyncTask(
                String serverUrl, Gson gson, Handler mainHandler, OperationCallback callback) {
            this.serverUrl = serverUrl;
            this.gson = gson;
            this.mainHandler = mainHandler;
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            String path = "Users/" + params[0];
            @SuppressWarnings("unchecked")
            Map<String, Object> updatesObj = (Map<String, Object>) params[1];
            return makePutRequest(path, gson.toJson(updatesObj));
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                mainHandler.post(callback::onSuccess);
            } else {
                mainHandler.post(() -> callback.onError("Failed to update user"));
            }
        }

        // Helper method for making PUT requests
        private boolean makePutRequest(String path, String jsonBody) {
            return makePutRequest(path, jsonBody, 15000);
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
                Log.d("FirebaseServerClient", "PUT response code: " + responseCode + " for URL: " + url);
                return responseCode >= 200 && responseCode < 300;
            } catch (Exception e) {
                Log.e("FirebaseServerClient", "Error making PUT request", e);
                return false;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }
}
