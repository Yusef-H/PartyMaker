package com.example.partymaker.utils.media;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.partymaker.R;
import com.example.partymaker.utils.media.ImageOptimizationManager;

/**
 * Utility class for loading images efficiently using Glide. Provides centralized image loading with
 * consistent caching and error handling.
 */
public class GlideImageLoader {

  // Animation constants
  private static final int CROSSFADE_DURATION_MS = 200;

  // Timeout constants
  private static final int NETWORK_TIMEOUT_MS = 10000; // 10 seconds

  // Default placeholder
  private static final int DEFAULT_PLACEHOLDER = R.drawable.default_profile_image;

  /**
   * Loads an image from a URL into an ImageView using optimized manager.
   *
   * @param context The context
   * @param imageUrl The URL of the image
   * @param imageView The ImageView to load the image into
   */
  public static void loadImage(Context context, String imageUrl, ImageView imageView) {
    ImageOptimizationManager.loadGroupThumbnail(imageView, imageUrl);
  }

  /**
   * Loads an image from a URL into an ImageView with optimized settings.
   * Now uses ImageOptimizationManager for better memory management.
   *
   * @param context The context
   * @param imageUrl The URL of the image
   * @param imageView The ImageView to load the image into
   * @param placeholderResId Resource ID of the placeholder drawable (ignored, using optimized defaults)
   */
  public static void loadImage(
      Context context, String imageUrl, ImageView imageView, @DrawableRes int placeholderResId) {
    ImageOptimizationManager.loadGroupThumbnail(imageView, imageUrl);
  }

  /**
   * Loads a circular profile image from a URL using optimized manager.
   *
   * @param context The context (unused but kept for backward compatibility)
   * @param imageUrl The URL of the image
   * @param imageView The ImageView to load the image into
   */
  public static void loadProfileImage(Context context, String imageUrl, ImageView imageView) {
    ImageOptimizationManager.loadProfileImage(imageView, imageUrl);
  }

  /**
   * Preloads an image into the memory cache using optimized manager.
   *
   * @param context The context
   * @param imageUrl The URL of the image
   */
  public static void preloadImage(Context context, String imageUrl) {
    ImageOptimizationManager.preloadImage(context, imageUrl);
  }

  /**
   * Preloads multiple images into the cache using optimized manager.
   * Useful for preloading group images to improve RecyclerView scrolling performance.
   *
   * @param context The context
   * @param imageUrls List of image URLs to preload
   */
  public static void preloadImages(Context context, java.util.List<String> imageUrls) {
    ImageOptimizationManager.preloadImages(context, imageUrls);
  }

  /**
   * Clears the memory cache using optimized manager.
   *
   * @param context The context
   */
  public static void clearMemoryCache(Context context) {
    ImageOptimizationManager.clearMemoryCache(context);
  }

  /**
   * Clears the disk cache using optimized manager.
   *
   * @param context The context
   */
  public static void clearDiskCache(final Context context) {
    ImageOptimizationManager.clearDiskCache(context);
  }

  /**
   * Clears any pending requests for a specific ImageView using optimized manager.
   * This is useful for RecyclerView to prevent wrong images during recycling.
   *
   * @param context The context (unused but kept for backward compatibility)
   * @param imageView The ImageView to clear
   */
  public static void clearImageView(Context context, ImageView imageView) {
    ImageOptimizationManager.clearImageView(imageView);
  }

  /**
   * Gets the default request options for image loading.
   *
   * @param placeholderResId Resource ID of the placeholder drawable
   * @return The request options
   */
  private static RequestOptions getDefaultRequestOptions(@DrawableRes int placeholderResId) {
    return new RequestOptions()
        .placeholder(placeholderResId)
        .error(placeholderResId)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .skipMemoryCache(false);
  }

  /**
   * Gets optimized request options for RecyclerView usage.
   *
   * @param placeholderResId Resource ID of the placeholder drawable
   * @return The request options optimized for RecyclerView
   */
  private static RequestOptions getRecyclerViewRequestOptions(@DrawableRes int placeholderResId) {
    return new RequestOptions()
        .placeholder(placeholderResId)
        .error(placeholderResId)
        .fallback(placeholderResId)
        .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache both original and resized images
        .skipMemoryCache(false) // Enable memory caching for better performance
        .timeout(NETWORK_TIMEOUT_MS) // 10 second timeout for network requests
        .dontTransform(); // Prevent unnecessary transformations for better performance
  }

  /** Create request options for preloading */
  private static RequestOptions createPreloadRequestOptions() {
    return new RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .timeout(NETWORK_TIMEOUT_MS);
  }
}
