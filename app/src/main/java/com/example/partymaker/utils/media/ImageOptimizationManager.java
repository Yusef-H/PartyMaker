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
import java.util.List;

/**
 * Optimized image loading manager that provides memory-efficient image loading
 * with different configurations for different use cases.
 */
public class ImageOptimizationManager {
    private static final String TAG = "ImageOptimization";
    
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
        
        Glide.with(imageView.getContext())
            .load(url)
            .apply(thumbnailOptions)
            .listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    Log.w(TAG, "Failed to load group thumbnail: " + url, e);
                    return false;
                }
                
                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    Log.d(TAG, "Loaded group thumbnail from: " + dataSource);
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
            } catch (Exception e) {
                Log.w(TAG, "Error clearing disk cache", e);
            }
        });
    }
    
    // Private constructor to prevent instantiation
    private ImageOptimizationManager() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}