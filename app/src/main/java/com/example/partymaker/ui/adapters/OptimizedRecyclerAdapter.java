package com.example.partymaker.ui.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * Base adapter class for RecyclerView that uses DiffUtil for efficient updates. This improves
 * performance by only updating items that have changed.
 *
 * @param <T> The type of data items
 * @param <VH> The ViewHolder type
 */
public abstract class OptimizedRecyclerAdapter<T, VH extends RecyclerView.ViewHolder>
    extends RecyclerView.Adapter<VH> {

  protected List<T> items = new ArrayList<>();

  @Override
  public int getItemCount() {
    return items.size();
  }

  /**
   * Gets an item at the specified position with bounds checking.
   *
   * @param position The position
   * @return The item, or null if position is out of bounds
   */
  public T getItem(int position) {
    if (isValidPosition(position)) {
      return items.get(position);
    }
    return null;
  }

  /**
   * Checks if the position is valid for the current items list.
   *
   * @param position The position to check
   * @return true if position is valid
   */
  protected boolean isValidPosition(int position) {
    return position >= 0 && position < items.size();
  }

  /**
   * Gets a defensive copy of all items.
   *
   * @return A new list containing all items
   */
  @NonNull
  public List<T> getItems() {
    return new ArrayList<>(items);
  }

  /**
   * Updates the adapter with a new list of items. Uses DiffUtil to calculate the difference and
   * update efficiently.
   *
   * @param newItems The new list of items
   */
  public void updateItems(@NonNull List<T> newItems) {
    final DiffUtil.DiffResult diffResult = calculateDiff(newItems);
    this.items = new ArrayList<>(newItems);
    diffResult.dispatchUpdatesTo(this);
  }

  /**
   * Calculates the difference between current items and new items using DiffUtil.
   *
   * @param newItems The new list of items to compare against
   * @return DiffResult containing the calculated differences
   */
  private DiffUtil.DiffResult calculateDiff(@NonNull List<T> newItems) {
    return DiffUtil.calculateDiff(new ItemDiffCallback(items, newItems));
  }

  /** DiffUtil callback for comparing items efficiently. */
  private class ItemDiffCallback extends DiffUtil.Callback {
    private final List<T> oldList;
    private final List<T> newList;

    ItemDiffCallback(List<T> oldList, List<T> newList) {
      this.oldList = oldList;
      this.newList = newList;
    }

    @Override
    public int getOldListSize() {
      return oldList.size();
    }

    @Override
    public int getNewListSize() {
      return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldPosition, int newPosition) {
      return OptimizedRecyclerAdapter.this.areItemsTheSame(
          oldList.get(oldPosition), newList.get(newPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldPosition, int newPosition) {
      return OptimizedRecyclerAdapter.this.areContentsTheSame(
          oldList.get(oldPosition), newList.get(newPosition));
    }
  }

  /**
   * Adds an item to the adapter at the end of the list.
   *
   * @param item The item to add
   */
  public void addItem(T item) {
    addItem(item, items.size());
  }

  /**
   * Adds an item to the adapter at the specified position with validation.
   *
   * @param item The item to add (must not be null)
   * @param position The position to insert the item at
   */
  public void addItem(T item, int position) {
    if (item != null && position >= 0 && position <= items.size()) {
      items.add(position, item);
      notifyItemInserted(position);
    }
  }

  /**
   * Removes an item from the adapter with bounds checking.
   *
   * @param position The position of the item to remove
   */
  public void removeItem(int position) {
    if (isValidPosition(position)) {
      items.remove(position);
      notifyItemRemoved(position);
    }
  }

  /** Clears all items from the adapter. */
  public void clearItems() {
    int size = items.size();
    if (size > 0) {
      items.clear();
      notifyItemRangeRemoved(0, size);
    }
  }

  /**
   * Checks if the adapter has any items.
   *
   * @return true if the adapter is empty, false otherwise
   */
  public boolean isEmpty() {
    return items.isEmpty();
  }

  /**
   * Determines if two items represent the same object. This is used by DiffUtil to determine if an
   * item was added, removed, or moved.
   *
   * @param oldItem The old item
   * @param newItem The new item
   * @return true if the items represent the same object, false otherwise
   */
  protected abstract boolean areItemsTheSame(T oldItem, T newItem);

  /**
   * Determines if the contents of two items are the same. This is used by DiffUtil to determine if
   * an item was changed.
   *
   * @param oldItem The old item
   * @param newItem The new item
   * @return true if the contents are the same, false otherwise
   */
  protected abstract boolean areContentsTheSame(T oldItem, T newItem);
}
