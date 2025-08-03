package com.example.partymaker.utils.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.support.media.ExifInterface;
import android.util.Log;
import androidx.annotation.NonNull;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Utility class for compressing and resizing images. This improves performance by reducing memory
 * usage and network transfer size.
 */
public class ImageCompressor {
  private static final String TAG = "ImageCompressor";
  private static final int MAX_WIDTH = 1024;
  private static final int MAX_HEIGHT = 1024;
  private static final int QUALITY = 80;

  private static final Executor executor = Executors.newSingleThreadExecutor();

  /**
   * Compresses an image from a Uri.
   *
   * @param context The context
   * @param imageUri The Uri of the image
   * @param callback Callback to receive the compressed image file
   */
  public static void compressImage(Context context, Uri imageUri, CompressCallback callback) {
    executor.execute(
        () -> {
          try {
            File compressedFile = compressImageInternal(context, imageUri);
            if (compressedFile != null) {
              callback.onCompressSuccess(compressedFile);
            } else {
              callback.onCompressError("Failed to compress image");
            }
          } catch (Exception e) {
            Log.e(TAG, "Error compressing image", e);
            callback.onCompressError("Error: " + e.getMessage());
          }
        });
  }

  /**
   * Compresses an image from a Uri synchronously.
   *
   * @param context The context
   * @param imageUri The Uri of the image
   * @return The compressed image file
   */
  private static File compressImageInternal(Context context, Uri imageUri) {
    try {
      // Create a temporary file to store the compressed image
      File outputDir = context.getCacheDir();
      File outputFile = new File(outputDir, "compressed_" + UUID.randomUUID().toString() + ".jpg");

      // Load the bitmap with appropriate sampling to avoid OOM
      Bitmap bitmap = loadScaledBitmapFromUri(context, imageUri, MAX_WIDTH, MAX_HEIGHT);
      if (bitmap == null) {
        return null;
      }

      // Rotate the bitmap if needed
      bitmap = rotateBitmapIfNeeded(context, imageUri, bitmap);

      // Compress the bitmap to the output file
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, outputStream);

      FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
      fileOutputStream.write(outputStream.toByteArray());
      fileOutputStream.flush();
      fileOutputStream.close();

      // Recycle the bitmap to free memory
      bitmap.recycle();

      return outputFile;
    } catch (Exception e) {
      Log.e(TAG, "Error compressing image", e);
      return null;
    }
  }

  /**
   * Loads a scaled bitmap from a Uri.
   *
   * @param context The context
   * @param imageUri The Uri of the image
   * @param maxWidth The maximum width
   * @param maxHeight The maximum height
   * @return The scaled bitmap
   */
  private static Bitmap loadScaledBitmapFromUri(
      Context context, Uri imageUri, int maxWidth, int maxHeight) {
    try {
      // Get the dimensions of the image
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inJustDecodeBounds = true;

      InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
      BitmapFactory.decodeStream(inputStream, null, options);
      if (inputStream != null) {
        inputStream.close();
      }

      // Calculate the sample size
      int sampleSize = calculateInSampleSize(options, maxWidth, maxHeight);

      // Load the bitmap with the calculated sample size
      options.inJustDecodeBounds = false;
      options.inSampleSize = sampleSize;
      options.inPreferredConfig = Bitmap.Config.RGB_565; // Use less memory

      inputStream = context.getContentResolver().openInputStream(imageUri);
      Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
      if (inputStream != null) {
        inputStream.close();
      }

      return bitmap;
    } catch (Exception e) {
      Log.e(TAG, "Error loading scaled bitmap", e);
      return null;
    }
  }

  /**
   * Calculates the sample size for loading a bitmap.
   *
   * @param options The BitmapFactory.Options
   * @param maxWidth The maximum width
   * @param maxHeight The maximum height
   * @return The sample size
   */
  private static int calculateInSampleSize(
      BitmapFactory.Options options, int maxWidth, int maxHeight) {
    int height = options.outHeight;
    int width = options.outWidth;
    int sampleSize = 1;

    if (height > maxHeight || width > maxWidth) {
      int halfHeight = height / 2;
      int halfWidth = width / 2;

      while ((halfHeight / sampleSize) >= maxHeight && (halfWidth / sampleSize) >= maxWidth) {
        sampleSize *= 2;
      }
    }

    return sampleSize;
  }

  /**
   * Rotates a bitmap if needed based on EXIF data.
   *
   * @param context The context
   * @param imageUri The Uri of the image
   * @param bitmap The bitmap to rotate
   * @return The rotated bitmap
   */
  private static Bitmap rotateBitmapIfNeeded(Context context, Uri imageUri, Bitmap bitmap) {
    try {
      InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
      if (inputStream == null) {
        return bitmap;
      }

      ExifInterface exif = new ExifInterface(inputStream);
      int orientation =
          exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
      inputStream.close();

      Matrix matrix = new Matrix();
      switch (orientation) {
        case ExifInterface.ORIENTATION_ROTATE_90:
          matrix.postRotate(90);
          break;
        case ExifInterface.ORIENTATION_ROTATE_180:
          matrix.postRotate(180);
          break;
        case ExifInterface.ORIENTATION_ROTATE_270:
          matrix.postRotate(270);
          break;
        default:
          return bitmap;
      }

      return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    } catch (IOException e) {
      Log.e(TAG, "Error rotating bitmap", e);
      return bitmap;
    }
  }

  /** Callback interface for image compression. */
  public interface CompressCallback {
    void onCompressSuccess(@NonNull File compressedFile);

    void onCompressError(String error);
  }
}
