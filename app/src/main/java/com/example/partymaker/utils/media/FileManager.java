package com.example.partymaker.utils.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/** Utility class for managing files. Provides methods for creating, saving, and sharing files. */
public class FileManager {
  private static final String TAG = "FileManager";
  private static final String FILE_PROVIDER_AUTHORITY = "com.example.partymaker.fileprovider";
  private static final Executor executor = Executors.newSingleThreadExecutor();

  /**
   * Creates a temporary image file.
   *
   * @param context The context
   * @return The temporary image file
   * @throws IOException If an error occurs
   */
  public static File createImageFile(Context context) throws IOException {
    // Create an image file name
    String timeStamp =
        new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
    String imageFileName = "JPEG_" + timeStamp + "_";
    File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

    return File.createTempFile(imageFileName, ".jpg", storageDir);
  }

  /**
   * Gets a content URI for a file.
   *
   * @param context The context
   * @param file The file
   * @return The content URI
   */
  public static Uri getUriForFile(Context context, File file) {
    return FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file);
  }

  /**
   * Saves a bitmap to a file.
   *
   * @param bitmap The bitmap
   * @param file The file
   * @param callback Callback for the operation
   */
  public static void saveBitmapToFile(Bitmap bitmap, File file, FileOperationCallback callback) {
    executor.execute(
        () -> {
          try {
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            outputStream.flush();
            outputStream.close();

            callback.onSuccess(file);
          } catch (IOException e) {
            Log.e(TAG, "Error saving bitmap to file", e);
            callback.onError("Error saving image: " + e.getMessage());
          }
        });
  }

  /**
   * Copies a file from a URI to a destination file.
   *
   * @param context The context
   * @param sourceUri The source URI
   * @param destFile The destination file
   * @param callback Callback for the operation
   */
  public static void copyFile(
      Context context, Uri sourceUri, File destFile, FileOperationCallback callback) {
    executor.execute(
        () -> {
          try {
            InputStream inputStream = context.getContentResolver().openInputStream(sourceUri);
            if (inputStream == null) {
              callback.onError("Could not open input stream");
              return;
            }

            OutputStream outputStream = new FileOutputStream(destFile);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
              outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            callback.onSuccess(destFile);
          } catch (IOException e) {
            Log.e(TAG, "Error copying file", e);
            callback.onError("Error copying file: " + e.getMessage());
          }
        });
  }

  /**
   * Deletes a file.
   *
   * @param file The file to delete
   * @return true if the file was deleted, false otherwise
   */
  public static boolean deleteFile(File file) {
    if (file != null && file.exists()) {
      return file.delete();
    }
    return false;
  }

  /**
   * Clears the cache directory.
   *
   * @param context The context
   */
  public static void clearCache(Context context) {
    executor.execute(
        () -> {
          try {
            File cacheDir = context.getCacheDir();
            deleteRecursive(cacheDir);
            Log.d(TAG, "Cache cleared");
          } catch (Exception e) {
            Log.e(TAG, "Error clearing cache", e);
          }
        });
  }

  /**
   * Recursively deletes a directory and its contents.
   *
   * @param fileOrDirectory The file or directory to delete
   */
  private static void deleteRecursive(File fileOrDirectory) {
    if (fileOrDirectory.isDirectory()) {
      File[] files = fileOrDirectory.listFiles();
      if (files != null) {
        for (File child : files) {
          deleteRecursive(child);
        }
      }
    }

    // Don't delete the cache directory itself, just its contents
    if (!fileOrDirectory
        .getAbsolutePath()
        .equals(Objects.requireNonNull(fileOrDirectory.getParentFile()).getAbsolutePath())) {
      fileOrDirectory.delete();
    }
  }

  /**
   * Gets the size of a file or directory.
   *
   * @param fileOrDirectory The file or directory
   * @return The size in bytes
   */
  public static long getSize(File fileOrDirectory) {
    if (fileOrDirectory == null || !fileOrDirectory.exists()) {
      return 0;
    }

    if (fileOrDirectory.isFile()) {
      return fileOrDirectory.length();
    }

    long size = 0;
    File[] files = fileOrDirectory.listFiles();
    if (files != null) {
      for (File file : files) {
        size += getSize(file);
      }
    }

    return size;
  }

  /**
   * Formats a file size to a human-readable string.
   *
   * @param size The size in bytes
   * @return The formatted size
   */
  public static String formatSize(long size) {
    if (size <= 0) {
      return "0 B";
    }

    final String[] units = new String[] {"B", "KB", "MB", "GB", "TB"};
    int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

    return String.format(
        Locale.getDefault(), "%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
  }

  /** Callback interface for file operations. */
  public interface FileOperationCallback {
    void onSuccess(@NonNull File file);

    void onError(String error);
  }
}
