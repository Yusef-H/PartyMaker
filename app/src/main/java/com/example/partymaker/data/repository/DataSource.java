package com.example.partymaker.data.repository;

import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.Map;

/**
 * Generic data source interface for handling data operations. This interface defines common
 * operations that can be implemented by both local and remote data sources.
 *
 * @param <T> The type of data this source handles
 * @param <K> The type of key used to identify data items
 */
public interface DataSource<T, K> {

  /**
   * Retrieves a single item by its key.
   *
   * @param key The unique identifier for the item
   * @param callback Callback to receive the result
   */
  void getItem(K key, DataCallback<T> callback);

  /**
   * Retrieves all items of this type.
   *
   * @param callback Callback to receive the result
   */
  void getAllItems(DataCallback<List<T>> callback);

  /**
   * Saves or updates an item.
   *
   * @param key The unique identifier for the item
   * @param item The item to save
   * @param callback Callback to receive the operation result
   */
  void saveItem(K key, T item, OperationCallback callback);

  /**
   * Updates specific fields of an item.
   *
   * @param key The unique identifier for the item
   * @param updates Map of field names to new values
   * @param callback Callback to receive the operation result
   */
  void updateItem(K key, Map<String, Object> updates, OperationCallback callback);

  /**
   * Deletes an item by its key.
   *
   * @param key The unique identifier for the item
   * @param callback Callback to receive the operation result
   */
  void deleteItem(K key, OperationCallback callback);

  /**
   * Returns a LiveData object for observing changes to a specific item. Only supported by local
   * data sources.
   *
   * @param key The unique identifier for the item
   * @return LiveData for the item, or null if not supported
   */
  default LiveData<T> observeItem(K key) {
    return null;
  }

  /**
   * Returns a LiveData object for observing changes to all items. Only supported by local data
   * sources.
   *
   * @return LiveData for all items, or null if not supported
   */
  default LiveData<List<T>> observeAllItems() {
    return null;
  }

  /** Clears all cached data. Only supported by local data sources. */
  default void clearCache() {
    // Default implementation does nothing
  }

  /**
   * Callback interface for data operations.
   *
   * @param <D> The type of data returned
   */
  @FunctionalInterface
  interface DataCallback<D> {
    void onDataLoaded(D data);

    /**
     * Called when an error occurs. Default implementation does nothing.
     *
     * @param error The error message
     */
    default void onError(String error) {
      // Default implementation does nothing
    }
  }

  /** Callback interface for operations that don't return data. */
  @FunctionalInterface
  interface OperationCallback {
    void onComplete();

    /**
     * Called when an error occurs. Default implementation does nothing.
     *
     * @param error The error message
     */
    default void onError(String error) {
      // Default implementation does nothing
    }
  }
}
