package com.example.partymaker;

import android.app.Application;
import android.util.Log;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.utilities.ServerModeHelper;

public class PartyMakerApplication extends Application {
  private static final String TAG = "PartyMakerApplication";

  @Override
  public void onCreate() {
    super.onCreate();

    // Initialize the Firebase server client
    FirebaseServerClient.getInstance().initialize(this);

    // Force server mode to be always enabled for stability
    ServerModeHelper.setServerModeEnabled(this, true);

    // Log that server mode is enabled
    Log.d(TAG, "Server mode is enabled for stable authentication");
  }
}
