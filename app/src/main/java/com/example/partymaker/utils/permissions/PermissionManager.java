package com.example.partymaker.utils.permissions;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralized permission management with security-conscious handling
 */
public class PermissionManager {
    
    // Permission request codes
    public static final int REQUEST_LOCATION_PERMISSION = 1001;
    public static final int REQUEST_CAMERA_PERMISSION = 1002;
    public static final int REQUEST_STORAGE_PERMISSION = 1003;
    public static final int REQUEST_NOTIFICATION_PERMISSION = 1004;
    
    // Permission groups
    public static final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    
    public static final String[] CAMERA_PERMISSIONS = {
            Manifest.permission.CAMERA
    };
    
    public static final String[] STORAGE_PERMISSIONS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
            new String[]{Manifest.permission.READ_MEDIA_IMAGES} :
            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    
    public static final String[] NOTIFICATION_PERMISSIONS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
            new String[]{Manifest.permission.POST_NOTIFICATIONS} :
            new String[]{};
    
    /**
     * Check if a specific permission is granted
     */
    public static boolean isPermissionGranted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Check if all permissions in a group are granted
     */
    public static boolean arePermissionsGranted(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (!isPermissionGranted(context, permission)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Request permissions with proper handling
     */
    public static void requestPermissions(Activity activity, String[] permissions, int requestCode) {
        List<String> permissionsToRequest = new ArrayList<>();
        
        for (String permission : permissions) {
            if (!isPermissionGranted(activity, permission)) {
                permissionsToRequest.add(permission);
            }
        }
        
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                    activity,
                    permissionsToRequest.toArray(new String[0]),
                    requestCode
            );
        }
    }
    
    /**
     * Check if location features can be used
     */
    public static boolean canUseLocationFeatures(Context context) {
        return arePermissionsGranted(context, LOCATION_PERMISSIONS);
    }
    
    /**
     * Check if camera features can be used
     */
    public static boolean canUseCameraFeatures(Context context) {
        return arePermissionsGranted(context, CAMERA_PERMISSIONS);
    }
    
    /**
     * Check if storage features can be used
     */
    public static boolean canUseStorageFeatures(Context context) {
        return arePermissionsGranted(context, STORAGE_PERMISSIONS);
    }
    
    /**
     * Check if notification features can be used
     */
    public static boolean canUseNotificationFeatures(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return arePermissionsGranted(context, NOTIFICATION_PERMISSIONS);
        }
        // Pre-Android 13, notifications don't need runtime permission
        return true;
    }
    
    /**
     * Request location permissions if needed
     */
    public static void requestLocationPermissionsIfNeeded(Activity activity) {
        if (!canUseLocationFeatures(activity)) {
            requestPermissions(activity, LOCATION_PERMISSIONS, REQUEST_LOCATION_PERMISSION);
        }
    }
    
    /**
     * Request camera permissions if needed
     */
    public static void requestCameraPermissionsIfNeeded(Activity activity) {
        if (!canUseCameraFeatures(activity)) {
            requestPermissions(activity, CAMERA_PERMISSIONS, REQUEST_CAMERA_PERMISSION);
        }
    }
    
    /**
     * Should show permission rationale
     */
    public static boolean shouldShowPermissionRationale(Activity activity, String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }
    
    /**
     * Get a user-friendly message for why a permission is needed
     */
    public static String getPermissionRationale(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
            case Manifest.permission.ACCESS_COARSE_LOCATION:
                return "Location permission is needed to show party venues on the map and help you find nearby events.";
                
            case Manifest.permission.CAMERA:
                return "Camera permission is needed to take photos for your profile and party events.";
                
            case Manifest.permission.READ_EXTERNAL_STORAGE:
            case Manifest.permission.READ_MEDIA_IMAGES:
                return "Storage permission is needed to select photos for your profile and party events.";
                
            case Manifest.permission.POST_NOTIFICATIONS:
                return "Notification permission is needed to keep you updated about party invitations and messages.";
                
            default:
                return "This permission is needed for the app to function properly.";
        }
    }
    
    /**
     * Handle permission request results
     */
    public static class PermissionResult {
        public final boolean allGranted;
        public final List<String> grantedPermissions;
        public final List<String> deniedPermissions;
        
        public PermissionResult(String[] permissions, int[] grantResults) {
            grantedPermissions = new ArrayList<>();
            deniedPermissions = new ArrayList<>();
            
            for (int i = 0; i < permissions.length; i++) {
                if (i < grantResults.length && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    grantedPermissions.add(permissions[i]);
                } else {
                    deniedPermissions.add(permissions[i]);
                }
            }
            
            allGranted = deniedPermissions.isEmpty();
        }
    }
}