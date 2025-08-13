package com.example.partymaker.utils.ui;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.partymaker.R;
import java.util.HashMap;
import java.util.Map;

/**
 * Manager class for efficiently handling ViewStubs in activities.
 * Reduces initial layout inflation time and memory usage by deferring
 * heavy view inflation until needed.
 */
public class ViewStubManager {
    private static final String TAG = "ViewStubManager";
    
    private final Activity activity;
    private final Map<Integer, View> inflatedViews = new HashMap<>();
    private final Map<Integer, ViewStub> viewStubs = new HashMap<>();
    
    /**
     * Creates a ViewStubManager for the given activity
     * @param activity The activity containing ViewStubs
     */
    public ViewStubManager(@NonNull Activity activity) {
        this.activity = activity;
    }
    
    /**
     * Registers a ViewStub for management
     * @param stubId The resource ID of the ViewStub
     */
    public void registerViewStub(@IdRes int stubId) {
        ViewStub stub = activity.findViewById(stubId);
        if (stub != null) {
            viewStubs.put(stubId, stub);
            Log.d(TAG, "Registered ViewStub: " + activity.getResources().getResourceEntryName(stubId));
        } else {
            Log.w(TAG, "ViewStub not found: " + stubId);
        }
    }
    
    /**
     * Creates and registers a new ViewStub programmatically
     * @param parent The parent ViewGroup
     * @param layoutToInflate The layout resource to inflate
     * @param stubId A unique ID for this ViewStub
     * @return The created ViewStub
     */
    public ViewStub createViewStub(@NonNull ViewGroup parent, @LayoutRes int layoutToInflate, @IdRes int stubId) {
        ViewStub stub = new ViewStub(activity);
        stub.setId(stubId);
        stub.setLayoutResource(layoutToInflate);
        
        // Add to parent with minimal layout params
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        parent.addView(stub, params);
        
        viewStubs.put(stubId, stub);
        Log.d(TAG, "Created ViewStub programmatically: " + stubId);
        
        return stub;
    }
    
    /**
     * Inflates a ViewStub if not already inflated
     * @param stubId The resource ID of the ViewStub
     * @return The inflated view, or null if inflation failed
     */
    @Nullable
    public View inflateViewStub(@IdRes int stubId) {
        // Check if already inflated
        if (inflatedViews.containsKey(stubId)) {
            return inflatedViews.get(stubId);
        }
        
        ViewStub stub = viewStubs.get(stubId);
        if (stub == null) {
            Log.w(TAG, "ViewStub not registered: " + stubId);
            return null;
        }
        
        try {
            View inflatedView = stub.inflate();
            inflatedViews.put(stubId, inflatedView);
            viewStubs.remove(stubId); // Remove stub as it's no longer valid
            
            Log.d(TAG, "Inflated ViewStub: " + activity.getResources().getResourceEntryName(stubId));
            return inflatedView;
        } catch (Exception e) {
            Log.e(TAG, "Error inflating ViewStub: " + stubId, e);
            return null;
        }
    }
    
    /**
     * Inflates a ViewStub asynchronously with callback
     * @param stubId The resource ID of the ViewStub
     * @param callback Callback for inflation result
     */
    public void inflateViewStubAsync(@IdRes int stubId, @NonNull ViewStubInflationCallback callback) {
        // Check if already inflated
        if (inflatedViews.containsKey(stubId)) {
            callback.onInflationComplete(inflatedViews.get(stubId));
            return;
        }
        
        ViewStub stub = viewStubs.get(stubId);
        if (stub == null) {
            callback.onInflationFailed("ViewStub not registered: " + stubId);
            return;
        }
        
        // Inflate on UI thread with proper error handling
        activity.runOnUiThread(() -> {
            try {
                View inflatedView = stub.inflate();
                inflatedViews.put(stubId, inflatedView);
                viewStubs.remove(stubId);
                callback.onInflationComplete(inflatedView);
            } catch (Exception e) {
                Log.e(TAG, "Error inflating ViewStub asynchronously: " + stubId, e);
                callback.onInflationFailed(e.getMessage());
            }
        });
    }
    
    /**
     * Shows an inflated view or inflates and shows if not yet inflated
     * @param stubId The resource ID of the ViewStub
     * @return true if view was shown, false otherwise
     */
    public boolean showView(@IdRes int stubId) {
        View view = inflatedViews.get(stubId);
        if (view == null) {
            view = inflateViewStub(stubId);
        }
        
        if (view != null) {
            view.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }
    
    /**
     * Hides an inflated view
     * @param stubId The resource ID of the ViewStub
     * @return true if view was hidden, false if not inflated
     */
    public boolean hideView(@IdRes int stubId) {
        View view = inflatedViews.get(stubId);
        if (view != null) {
            view.setVisibility(View.GONE);
            return true;
        }
        return false;
    }
    
    /**
     * Gets an inflated view by its stub ID
     * @param stubId The resource ID of the ViewStub
     * @return The inflated view or null if not inflated
     */
    @Nullable
    public View getInflatedView(@IdRes int stubId) {
        return inflatedViews.get(stubId);
    }
    
    /**
     * Checks if a ViewStub has been inflated
     * @param stubId The resource ID of the ViewStub
     * @return true if inflated, false otherwise
     */
    public boolean isInflated(@IdRes int stubId) {
        return inflatedViews.containsKey(stubId);
    }
    
    /**
     * Clears all references to prevent memory leaks
     * Should be called in onDestroy()
     */
    public void clear() {
        inflatedViews.clear();
        viewStubs.clear();
        Log.d(TAG, "ViewStubManager cleared");
    }
    
    /**
     * Callback interface for async ViewStub inflation
     */
    public interface ViewStubInflationCallback {
        void onInflationComplete(View inflatedView);
        void onInflationFailed(String error);
    }
    
    /**
     * Helper method to create ViewStubs for common heavy layouts
     */
    public static class CommonViewStubs {
        public static final int LOADING_OVERLAY = View.generateViewId();
        public static final int EMPTY_STATE = View.generateViewId();
        public static final int ERROR_STATE = View.generateViewId();
        public static final int SHIMMER_LOADING = View.generateViewId();
        public static final int PROGRESS_BAR = View.generateViewId();
        
        /**
         * Adds common ViewStubs to an activity's root layout
         * @param activity The activity
         * @param rootView The root ViewGroup
         * @return A configured ViewStubManager
         */
        public static ViewStubManager setupCommonStubs(@NonNull Activity activity, @NonNull ViewGroup rootView) {
            ViewStubManager manager = new ViewStubManager(activity);
            
            // Create common ViewStubs programmatically if they don't exist
            // Note: These layouts need to be created for the specific app
            // Commented out as layouts don't exist yet
            /*
            if (activity.findViewById(LOADING_OVERLAY) == null) {
                manager.createViewStub(rootView, R.layout.layout_loading_overlay, LOADING_OVERLAY);
            }
            
            if (activity.findViewById(EMPTY_STATE) == null) {
                manager.createViewStub(rootView, R.layout.layout_empty_state, EMPTY_STATE);
            }
            
            if (activity.findViewById(ERROR_STATE) == null) {
                manager.createViewStub(rootView, R.layout.layout_error_state, ERROR_STATE);
            }
            */
            
            return manager;
        }
    }
}