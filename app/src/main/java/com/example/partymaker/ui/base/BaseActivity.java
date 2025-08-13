package com.example.partymaker.ui.base;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelStore;
import com.example.partymaker.utils.infrastructure.system.MemoryManager;

/**
 * Base activity with memory management integration.
 * All major activities should extend this class for proper memory handling.
 */
public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    private String activityKey;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityKey = getClass().getSimpleName() + "_" + System.currentTimeMillis();
        MemoryManager.getInstance().registerObject(activityKey, this);
        MemoryManager.getInstance().logMemoryStats();
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        MemoryManager.getInstance().clearUnusedResources();
    }
    
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        switch (level) {
            case TRIM_MEMORY_RUNNING_MODERATE:
            case TRIM_MEMORY_RUNNING_LOW:
                MemoryManager.getInstance().clearImageCache();
                break;
            case TRIM_MEMORY_RUNNING_CRITICAL:
                MemoryManager.getInstance().emergencyCleanup();
                break;
            case android.content.ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
                break;
            case android.content.ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
                break;
            case android.content.ComponentCallbacks2.TRIM_MEMORY_MODERATE:
                break;
            case android.content.ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
                break;
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        MemoryManager.getInstance().unregisterObject(activityKey);
        
        // Clear ViewModels
        getViewModelStore().clear();
        
        // Clear any remaining references
        clearActivityReferences();
    }
    
    /**
     * Override this method to clear activity-specific references
     * to prevent memory leaks
     */
    protected abstract void clearActivityReferences();
}