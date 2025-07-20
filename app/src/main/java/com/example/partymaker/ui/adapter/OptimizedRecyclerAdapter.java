package com.example.partymaker.ui.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * Base adapter class for RecyclerView that uses DiffUtil for efficient updates.
 * This improves performance by only updating items that have changed.
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
     * Gets an item at the specified position.
     *
     * @param position The position
     * @return The item
     */
    public T getItem(int position) {
        if (position >= 0 && position < items.size()) {
            return items.get(position);
        }
        return null;
    }
    
    /**
     * Gets all items.
     *
     * @return The list of items
     */
    public List<T> getItems() {
        return new ArrayList<>(items);
    }
    
    /**
     * Updates the adapter with a new list of items.
     * Uses DiffUtil to calculate the difference and update efficiently.
     *
     * @param newItems The new list of items
     */
    public void updateItems(@NonNull List<T> newItems) {
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new DiffUtil.Callback() {
                    @Override
                    public int getOldListSize() {
                        return items.size();
                    }
                    
                    @Override
                    public int getNewListSize() {
                        return newItems.size();
                    }
                    
                    @Override
                    public boolean areItemsTheSame(int oldPosition, int newPosition) {
                        return OptimizedRecyclerAdapter.this.areItemsTheSame(
                                items.get(oldPosition), newItems.get(newPosition));
                    }
                    
                    @Override
                    public boolean areContentsTheSame(int oldPosition, int newPosition) {
                        return OptimizedRecyclerAdapter.this.areContentsTheSame(
                                items.get(oldPosition), newItems.get(newPosition));
                    }
                });
        
        this.items = new ArrayList<>(newItems);
        diffResult.dispatchUpdatesTo(this);
    }
    
    /**
     * Adds an item to the adapter.
     *
     * @param item The item to add
     */
    public void addItem(T item) {
        if (item != null) {
            items.add(item);
            notifyItemInserted(items.size() - 1);
        }
    }
    
    /**
     * Removes an item from the adapter.
     *
     * @param position The position of the item to remove
     */
    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }
    
    /**
     * Clears all items from the adapter.
     */
    public void clearItems() {
        int size = items.size();
        items.clear();
        notifyItemRangeRemoved(0, size);
    }
    
    /**
     * Determines if two items represent the same object.
     * This is used by DiffUtil to determine if an item was added, removed, or moved.
     *
     * @param oldItem The old item
     * @param newItem The new item
     * @return true if the items represent the same object, false otherwise
     */
    protected abstract boolean areItemsTheSame(T oldItem, T newItem);
    
    /**
     * Determines if the contents of two items are the same.
     * This is used by DiffUtil to determine if an item was changed.
     *
     * @param oldItem The old item
     * @param newItem The new item
     * @return true if the contents are the same, false otherwise
     */
    protected abstract boolean areContentsTheSame(T oldItem, T newItem);
} 