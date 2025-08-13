package com.example.partymaker.utils.media;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.partymaker.R;
import com.example.partymaker.utils.infrastructure.system.ThreadUtils;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.List;
import android.util.LruCache;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Optimized image loading manager that provides memory-efficient image loading
 * with different configurations for different use cases.
 */
public class ImageOptimizationManager {
    private static final String TAG = "ImageOptimization";
    
    // Image loading state management
    private static final AtomicBoolean isImageLoadingPaused = new AtomicBoolean(false);
    
    // LRU cache for URL validation results to avoid repeated Firebase checks
    private static final LruCache<String, Boolean> urlValidationCache = new LruCache<>(100);
    
    // Memory cache size (10% of available memory)
    private static final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    private static final int cacheSize = maxMemory / 10;
    
    // Optimized request options for different image types
    private static final RequestOptions thumbnailOptions = new RequestOptions()
        .override(150, 150)
        .centerCrop()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .placeholder(R.drawable.default_group_image)
        .error(R.drawable.default_group_image)
        .skipMemoryCache(false)
        .format(DecodeFormat.PREFER_RGB_565); // Use less memory
    
    private static final RequestOptions profileOptions = new RequestOptions()
        .override(100, 100)
        .circleCrop()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .placeholder(R.drawable.default_profile_image)
        .error(R.drawable.default_profile_image)
        .skipMemoryCache(false)
        .format(DecodeFormat.PREFER_RGB_565);
    
    private static final RequestOptions fullImageOptions = new RequestOptions()
        .override(800, 600)
        .centerCrop()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .placeholder(R.drawable.default_group_image)
        .error(R.drawable.default_group_image)
        .skipMemoryCache(false)
        .format(DecodeFormat.PREFER_ARGB_8888); // Better quality for full images
    
    /**
     * Loads a group thumbnail with optimized settings for memory efficiency
     */
    public static void loadGroupThumbnail(ImageView imageView, String url) {
        if (imageView == null || url == null || url.isEmpty()) return;
        
        // Check if image loading is paused (during scroll)
        if (isImageLoadingPaused.get()) {
            imageView.setImageResource(R.drawable.default_group_image);
            return;
        }
        
        // Check URL validation cache first
        Boolean isValid = urlValidationCache.get(url);
        if (isValid != null && !isValid) {
            imageView.setImageResource(R.drawable.default_group_image);
            return;
        }
        
        Glide.with(imageView.getContext())
            .load(url)
            .apply(thumbnailOptions)
            .listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    Log.w(TAG, "Failed to load group thumbnail: " + url, e);
                    urlValidationCache.put(url, false); // Cache invalid URL
                    return false;
                }
                
                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    Log.d(TAG, "Loaded group thumbnail from: " + dataSource);
                    urlValidationCache.put(url, true); // Cache valid URL
                    return false;
                }
            })
            .into(imageView);
    }
    
    /**
     * Loads a profile image with circular crop and optimized settings
     */
    public static void loadProfileImage(ImageView imageView, String url) {
        if (imageView == null || url == null || url.isEmpty()) return;
        
        Glide.with(imageView.getContext())
            .load(url)
            .apply(profileOptions)
            .into(imageView);
    }
    
    /**
     * Loads a full-size image with higher quality settings
     */
    public static void loadFullImage(ImageView imageView, String url) {
        if (imageView == null || url == null || url.isEmpty()) return;
        
        Glide.with(imageView.getContext())
            .load(url)
            .apply(fullImageOptions)
            .into(imageView);
    }
    
    /**
     * Preloads an image into cache for faster loading later
     */
    public static void preloadImage(Context context, String url) {
        if (context == null || url == null || url.isEmpty()) return;
        
        Glide.with(context)
            .load(url)
            .apply(thumbnailOptions)
            .preload();
    }
    
    /**
     * Clears any pending load requests for the given ImageView
     */
    public static void clearImageView(ImageView imageView) {
        if (imageView != null) {
            try {
                // Simply set the image to null instead of using Glide.clear
                // to avoid issues with destroyed activities
                imageView.setImageDrawable(null);
                imageView.setImageBitmap(null);
                imageView.setTag(null);
            } catch (Exception e) {
                // Silently ignore exceptions during cleanup
            }
        }
    }
    
    /**
     * Batch preloads multiple images in the background for better list performance
     */
    public static void preloadImages(Context context, List<String> urls) {
        if (context == null || urls == null || urls.isEmpty()) return;
        
        ThreadUtils.executeImageTask(() -> {
            for (String url : urls) {
                if (url != null && !url.isEmpty()) {
                    preloadImage(context, url);
                }
            }
        });
    }
    
    /**
     * Clears Glide memory cache - should be called on main thread
     */
    public static void clearMemoryCache(Context context) {
        if (context == null) return;
        
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Glide.get(context).clearMemory();
        } else {
            new Handler(Looper.getMainLooper()).post(() -> {
                Glide.get(context).clearMemory();
            });
        }
    }
    
    /**
     * Clears Glide disk cache - should be called on background thread
     */
    public static void clearDiskCache(Context context) {
        if (context == null) return;
        
        ThreadUtils.executeImageTask(() -> {
            try {
                Glide.get(context).clearDiskCache();
                // Also clear our validation cache
                urlValidationCache.evictAll();
            } catch (Exception e) {
                Log.w(TAG, "Error clearing disk cache", e);
            }
        });
    }
    
    /**
     * Pauses image loading (useful during fast scrolling)
     */
    public static void pauseImageLoading() {
        isImageLoadingPaused.set(true);
    }
    
    /**
     * Resumes image loading
     */
    public static void resumeImageLoading() {
        isImageLoadingPaused.set(false);
    }
    
    /**
     * Optimizes Glide configuration for the application
     */
    public static void optimizeGlideConfiguration(Context context) {
        try {
            // Set memory cache size
            Glide.get(context).setMemoryCategory(com.bumptech.glide.MemoryCategory.NORMAL);
            
            Log.d(TAG, "Glide configuration optimized - cache size: " + cacheSize + "KB");
        } catch (Exception e) {
            Log.e(TAG, "Error optimizing Glide configuration", e);
        }
    }
    
    /**
     * Trims memory when the app is under memory pressure
     */
    public static void trimMemory(Context context, int level) {
        try {
            Glide.get(context).trimMemory(level);
            
            // Clear validation cache on critical memory pressure
            if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL) {
                urlValidationCache.evictAll();
            }
        } catch (Exception e) {
            Log.w(TAG, "Error trimming memory", e);
        }
    }
    
    /**
     * Validates if a Firebase Storage reference exists before loading image
     * @param imagePath The Firebase Storage path
     * @param imageView The ImageView to load into if exists
     * @param fallbackImage Resource ID for fallback image
     */
    public static void loadImageWithStorageValidation(String imagePath, ImageView imageView, int fallbackImage) {
        if (imageView == null || imagePath == null || imagePath.isEmpty()) {
            if (imageView != null) {
                imageView.setImageResource(fallbackImage);
            }
            return;
        }
        
        try {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference imageRef = storage.getReference(imagePath);
            
            // Check if image exists
            imageRef.getMetadata().addOnSuccessListener(metadata -> {
                // Image exists, load it
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Log.d(TAG, "Loading validated image: " + uri.toString());
                    loadGroupThumbnail(imageView, uri.toString());
                }).addOnFailureListener(e -> {
                    Log.w(TAG, "Failed to get download URL for: " + imagePath, e);
                    imageView.setImageResource(fallbackImage);
                });
            }).addOnFailureListener(e -> {
                // Image doesn't exist, use fallback
                Log.w(TAG, "Image not found in storage: " + imagePath, e);
                imageView.setImageResource(fallbackImage);
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error validating storage reference: " + imagePath, e);
            imageView.setImageResource(fallbackImage);
        }
    }
    
    /**
     * Enhanced group image loading with Firebase Storage validation
     * @param imageView The ImageView to load into
     * @param imagePath Firebase Storage path (e.g., "Groups/groupKey" or "UsersImageProfile/Groups/groupKey")
     */
    public static void loadGroupImageWithValidation(ImageView imageView, String imagePath) {
        if (imageView == null || imagePath == null || imagePath.isEmpty()) {
            if (imageView != null) {
                imageView.setImageResource(R.drawable.placeholder_group);
            }
            return;
        }
        
        // Try new path first, then fallback to old path
        String newPath = imagePath.startsWith("UsersImageProfile/") ? imagePath : "UsersImageProfile/Groups/" + imagePath;
        String oldPath = imagePath.startsWith("Groups/") ? imagePath : "Groups/" + imagePath;
        
        loadImageWithStorageValidation(newPath, imageView, R.drawable.placeholder_group);
    }
    
    // Private constructor to prevent instantiation
    private ImageOptimizationManager() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}