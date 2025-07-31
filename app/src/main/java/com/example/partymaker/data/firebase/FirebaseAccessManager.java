package com.example.partymaker.data.firebase;

import android.content.Context;
import android.util.Log;

import com.example.partymaker.data.api.FirebaseServerClient;

// import com.google.firebase.database.DatabaseReference;

public class FirebaseAccessManager {
    private static final String TAG = "FirebaseAccessManager";
    private final Context context;

    public FirebaseAccessManager(Context context) {
        this.context = context;
    }

    public boolean isServerModeEnabled() {
        Log.d(TAG, "Server mode is enabled");
        return true;
    }

    public FirebaseServerClient getGroupsRef() {
        Log.d(TAG, "Using server mode for groups");
        FirebaseServerClient client = FirebaseServerClient.getInstance();
        client.initialize(context);
        return client;
    }

    public FirebaseServerClient getUsersRef() {
        Log.d(TAG, "Using server mode for users");
        FirebaseServerClient client = FirebaseServerClient.getInstance();
        client.initialize(context);
        return client;
    }

    public FirebaseServerClient getMessagesRef() {
        Log.d(TAG, "Using server mode for messages");
        FirebaseServerClient client = FirebaseServerClient.getInstance();
        client.initialize(context);
        return client;
    }
}
