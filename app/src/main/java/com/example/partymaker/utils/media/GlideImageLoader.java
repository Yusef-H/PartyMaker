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

/**
 * Utility class for loading images efficiently using Glide. Provides centralized image loading with
 * consistent caching and error handling.
 */
public class GlideImageLoader {

    /**
     * Loads an image from a URL into an ImageView.
     *
     * @param context   The context
     * @param imageUrl  The URL of the image
     * @param imageView The ImageView to load the image into
     */
    public static void loadImage(Context context, String imageUrl, ImageView imageView) {
        loadImage(context, imageUrl, imageView, R.drawable.default_profile_image);
    }

    /**
     * Loads an image from a URL into an ImageView with a custom placeholder.
     *
     * @param context          The context
     * @param imageUrl         The URL of the image
     * @param imageView        The ImageView to load the image into
     * @param placeholderResId Resource ID of the placeholder drawable
     */
    public static void loadImage(
            Context context, String imageUrl, ImageView imageView, @DrawableRes int placeholderResId) {
        if (context == null || imageView == null) {
            return;
        }

        // Create request with error handling, caching, and crossfade
        RequestBuilder<Drawable> requestBuilder =
                Glide.with(context)
                        .load(imageUrl)
                        .apply(getDefaultRequestOptions(placeholderResId))
                        .transition(DrawableTransitionOptions.withCrossFade(300))
                        .listener(
                                new RequestListener<>() {
                                    @Override
                                    public boolean onLoadFailed(
                                            @Nullable GlideException e,
                                            Object model,
                                            Target<Drawable> target,
                                            boolean isFirstResource) {
                                        if (e != null) {
                                            e.logRootCauses("GlideImageLoader");
                                        }
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(
                                            Drawable resource,
                                            Object model,
                                            Target<Drawable> target,
                                            DataSource dataSource,
                                            boolean isFirstResource) {
                                        return false;
                                    }
                                });

        // Execute the request
        requestBuilder.into(imageView);
    }

    /**
     * Loads a circular profile image from a URL.
     *
     * @param context   The context
     * @param imageUrl  The URL of the image
     * @param imageView The ImageView to load the image into
     */
    public static void loadProfileImage(Context context, String imageUrl, ImageView imageView) {
        if (context == null || imageView == null) {
            return;
        }

        Glide.with(context)
                .load(imageUrl)
                .apply(getDefaultRequestOptions(R.drawable.default_profile_image).circleCrop())
                .into(imageView);
    }

    /**
     * Preloads an image into the memory cache.
     *
     * @param context  The context
     * @param imageUrl The URL of the image
     */
    public static void preloadImage(Context context, String imageUrl) {
        if (context == null || imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        Glide.with(context)
                .load(imageUrl)
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .preload();
    }

    /**
     * Clears the memory cache.
     *
     * @param context The context
     */
    public static void clearMemoryCache(Context context) {
        if (context == null) {
            return;
        }

        Glide.get(context).clearMemory();
    }

    /**
     * Clears the disk cache.
     *
     * @param context The context
     */
    public static void clearDiskCache(final Context context) {
        if (context == null) {
            return;
        }

        new Thread(() -> Glide.get(context).clearDiskCache()).start();
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
}
