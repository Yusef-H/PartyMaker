package com.example.partymaker.viewmodel.groups;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.partymaker.data.api.NetworkUtils;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.data.repository.GroupRepository;
import com.example.partymaker.utils.infrastructure.system.ThreadUtils;
import com.example.partymaker.viewmodel.BaseViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ViewModel for group discovery functionality.
 * 
 * <p>Handles public group browsing, searching, filtering, and joining operations
 * with comprehensive state management and user feedback.
 * 
 * <p>Features:
 * <ul>
 *   <li>Public group browsing and listing</li>
 *   <li>Group search and filtering</li>
 *   <li>Join/leave group operations</li>
 *   <li>Group details preview</li>
 *   <li>Real-time updates</li>
 *   <li>Pagination support</li>
 * </ul>
 * 
 * @author PartyMaker Team
 * @version 1.0
 * @since 1.0
 */
public class GroupDiscoveryViewModel extends BaseViewModel {
    
    private static final String TAG = "GroupDiscoveryViewModel";
    private static final int GROUPS_PER_PAGE = 20;
    
    // Dependencies
    private final GroupRepository groupRepository;
    
    // LiveData for discovery state
    private final MutableLiveData<List<Group>> publicGroups = new MutableLiveData<>();
    private final MutableLiveData<List<Group>> filteredGroups = new MutableLiveData<>();
    private final MutableLiveData<Group> selectedGroup = new MutableLiveData<>();
    private final MutableLiveData<Boolean> joinInProgress = new MutableLiveData<>();
    private final MutableLiveData<String> joinedGroupKey = new MutableLiveData<>();
    private final MutableLiveData<String> leftGroupKey = new MutableLiveData<>();
    
    // Search and filter state
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>();
    private final MutableLiveData<String> locationFilter = new MutableLiveData<>();
    private final MutableLiveData<String> dateFilter = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showOnlyJoinable = new MutableLiveData<>();
    
    // Pagination state
    private final MutableLiveData<Boolean> hasMoreGroups = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingMore = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentPage = new MutableLiveData<>();
    
    // Current state
    private String currentUserKey;
    private List<Group> allPublicGroups = new ArrayList<>();
    
    /**
     * Constructor for GroupDiscoveryViewModel.
     * 
     * @param application The application context
     */
    public GroupDiscoveryViewModel(@NonNull Application application) {
        super(application);
        this.groupRepository = GroupRepository.getInstance();
        
        // Initialize state
        publicGroups.setValue(new ArrayList<>());
        filteredGroups.setValue(new ArrayList<>());
        joinInProgress.setValue(false);
        showOnlyJoinable.setValue(true);
        hasMoreGroups.setValue(false);
        isLoadingMore.setValue(false);
        currentPage.setValue(0);
        
        Log.d(TAG, "GroupDiscoveryViewModel initialized");
    }
    
    // Getters for LiveData
    
    public LiveData<List<Group>> getPublicGroups() {
        return publicGroups;
    }
    
    public LiveData<List<Group>> getFilteredGroups() {
        return filteredGroups;
    }
    
    public LiveData<Group> getSelectedGroup() {
        return selectedGroup;
    }
    
    public LiveData<Boolean> getJoinInProgress() {
        return joinInProgress;
    }
    
    public LiveData<String> getJoinedGroupKey() {
        return joinedGroupKey;
    }
    
    public LiveData<String> getLeftGroupKey() {
        return leftGroupKey;
    }
    
    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }
    
    public LiveData<String> getLocationFilter() {
        return locationFilter;
    }
    
    public LiveData<String> getDateFilter() {
        return dateFilter;
    }
    
    public LiveData<Boolean> getShowOnlyJoinable() {
        return showOnlyJoinable;
    }
    
    public LiveData<Boolean> getHasMoreGroups() {
        return hasMoreGroups;
    }
    
    public LiveData<Boolean> getIsLoadingMore() {
        return isLoadingMore;
    }
    
    /**
     * Sets the current user key for filtering user-specific data.
     * 
     * @param userKey The current user's key
     */
    public void setCurrentUser(@NonNull String userKey) {
        this.currentUserKey = userKey;
        Log.d(TAG, "Current user set: " + userKey);
    }
    
    /**
     * Loads public groups available for discovery.
     * 
     * @param forceRefresh Whether to force refresh from server
     */
    public void loadPublicGroups(boolean forceRefresh) {
        if (isCurrentlyLoading() && !forceRefresh) {
            Log.w(TAG, "Public groups loading already in progress");
            return;
        }
        
        setLoading(true);
        clearMessages();
        currentPage.setValue(0);
        
        Log.d(TAG, "Loading public groups, forceRefresh: " + forceRefresh);
        
        ThreadUtils.runOnBackground(() -> {
            groupRepository.getPublicGroups(forceRefresh, new GroupRepository.Callback<List<Group>>() {
                @Override
                public void onSuccess(List<Group> groups) {
                    handlePublicGroupsLoaded(groups);
                }
                
                @Override
                public void onError(Exception error) {
                    handlePublicGroupsError(error);
                }
            });
        });
    }
    
    /**
     * Loads more public groups for pagination.
     */
    public void loadMoreGroups() {
        if (isLoadingMore.getValue() != null && isLoadingMore.getValue()) {
            Log.w(TAG, "Load more already in progress");
            return;
        }
        
        Boolean hasMore = hasMoreGroups.getValue();
        if (hasMore == null || !hasMore) {
            Log.d(TAG, "No more groups to load");
            return;
        }
        
        isLoadingMore.setValue(true);
        Integer page = currentPage.getValue();
        int nextPage = (page != null ? page : 0) + 1;
        
        Log.d(TAG, "Loading more groups, page: " + nextPage);
        
        ThreadUtils.runOnBackground(() -> {
            // Simulate pagination (in real implementation, pass page parameter to repository)
            ThreadUtils.runOnMainThreadDelayed(() -> {
                // For now, just mark as no more groups available
                hasMoreGroups.setValue(false);
                isLoadingMore.setValue(false);
                currentPage.setValue(nextPage);
                
                setInfo("All available groups loaded");
            }, 1000);
        });
    }
    
    /**
     * Searches groups by name or description.
     * 
     * @param query The search query
     */
    public void searchGroups(@Nullable String query) {
        searchQuery.setValue(query);
        applyFilters();
    }
    
    /**
     * Filters groups by location.
     * 
     * @param location The location filter
     */
    public void filterByLocation(@Nullable String location) {
        locationFilter.setValue(location);
        applyFilters();
    }
    
    /**
     * Filters groups by date.
     * 
     * @param date The date filter in format "DD/MM/YYYY"
     */
    public void filterByDate(@Nullable String date) {
        dateFilter.setValue(date);
        applyFilters();
    }
    
    /**
     * Sets whether to show only joinable groups.
     * 
     * @param joinableOnly true to show only joinable groups
     */
    public void setShowOnlyJoinable(boolean joinableOnly) {
        showOnlyJoinable.setValue(joinableOnly);
        applyFilters();
    }
    
    /**
     * Clears all filters and shows all public groups.
     */
    public void clearFilters() {
        searchQuery.setValue(null);
        locationFilter.setValue(null);
        dateFilter.setValue(null);
        showOnlyJoinable.setValue(true);
        applyFilters();
    }
    
    /**
     * Selects a group for detailed view.
     * 
     * @param group The group to select
     */
    public void selectGroup(@Nullable Group group) {
        selectedGroup.setValue(group);
        
        if (group != null) {
            Log.d(TAG, "Group selected: " + group.getGroupName());
        }
    }
    
    /**
     * Joins the specified group.
     * 
     * @param group The group to join
     */
    public void joinGroup(@NonNull Group group) {
        if (currentUserKey == null) {
            setError("User not authenticated", NetworkUtils.ErrorType.AUTHENTICATION_ERROR);
            return;
        }
        
        if (joinInProgress.getValue() != null && joinInProgress.getValue()) {
            Log.w(TAG, "Join operation already in progress");
            return;
        }
        
        joinInProgress.setValue(true);
        clearMessages();
        
        Log.d(TAG, "Joining group: " + group.getGroupName());
        
        ThreadUtils.runOnBackground(() -> {
            groupRepository.joinGroup(group.getGroupKey(), currentUserKey, 
                new GroupRepository.Callback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        handleJoinSuccess(group);
                    }
                    
                    @Override
                    public void onError(Exception error) {
                        handleJoinError(group, error);
                    }
                });
        });
    }
    
    /**
     * Leaves the specified group.
     * 
     * @param group The group to leave
     */
    public void leaveGroup(@NonNull Group group) {
        if (currentUserKey == null) {
            setError("User not authenticated", NetworkUtils.ErrorType.AUTHENTICATION_ERROR);
            return;
        }
        
        if (joinInProgress.getValue() != null && joinInProgress.getValue()) {
            Log.w(TAG, "Leave operation already in progress");
            return;
        }
        
        joinInProgress.setValue(true);
        clearMessages();
        
        Log.d(TAG, "Leaving group: " + group.getGroupName());
        
        ThreadUtils.runOnBackground(() -> {
            groupRepository.leaveGroup(group.getGroupKey(), currentUserKey, 
                new GroupRepository.Callback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        handleLeaveSuccess(group);
                    }
                    
                    @Override
                    public void onError(Exception error) {
                        handleLeaveError(group, error);
                    }
                });
        });
    }
    
    /**
     * Checks if the current user is a member of the specified group.
     * 
     * @param group The group to check
     * @return true if user is a member, false otherwise
     */
    public boolean isUserMemberOfGroup(@NonNull Group group) {
        if (currentUserKey == null) {
            return false;
        }
        
        return group.getFriendKeys() != null && 
               group.getFriendKeys().containsKey(currentUserKey);
    }
    
    /**
     * Checks if the current user can join the specified group.
     * 
     * @param group The group to check
     * @return true if user can join, false otherwise
     */
    public boolean canUserJoinGroup(@NonNull Group group) {
        if (currentUserKey == null) {
            return false;
        }
        
        // User is already a member
        if (isUserMemberOfGroup(group)) {
            return false;
        }
        
        // Check if group allows new members
        if (!group.isCanAdd()) {
            return false;
        }
        
        // Check if group is public
        return "public".equals(group.getGroupType());
    }
    
    // Private helper methods
    
    private void applyFilters() {
        ThreadUtils.runOnBackground(() -> {
            try {
                List<Group> filtered = new ArrayList<>(allPublicGroups);
                
                // Apply search query filter
                String query = searchQuery.getValue();
                if (query != null && !query.trim().isEmpty()) {
                    String lowerQuery = query.toLowerCase().trim();
                    filtered = filtered.stream()
                        .filter(group -> 
                            (group.getGroupName() != null && 
                             group.getGroupName().toLowerCase().contains(lowerQuery)) ||
                            (group.getGroupLocation() != null && 
                             group.getGroupLocation().toLowerCase().contains(lowerQuery)))
                        .collect(Collectors.toList());
                }
                
                // Apply location filter
                String location = locationFilter.getValue();
                if (location != null && !location.trim().isEmpty()) {
                    String lowerLocation = location.toLowerCase().trim();
                    filtered = filtered.stream()
                        .filter(group -> group.getGroupLocation() != null && 
                               group.getGroupLocation().toLowerCase().contains(lowerLocation))
                        .collect(Collectors.toList());
                }
                
                // Apply date filter
                String date = dateFilter.getValue();
                if (date != null && !date.trim().isEmpty()) {
                    filtered = filtered.stream()
                        .filter(group -> {
                            String groupDate = group.getGroupDays() + "/" + 
                                             group.getGroupMonths() + "/" + 
                                             group.getGroupYears();
                            return date.equals(groupDate);
                        })
                        .collect(Collectors.toList());
                }
                
                // Apply joinable filter
                Boolean joinableOnly = showOnlyJoinable.getValue();
                if (joinableOnly != null && joinableOnly) {
                    filtered = filtered.stream()
                        .filter(this::canUserJoinGroup)
                        .collect(Collectors.toList());
                }
                
                // Update filtered results
                final List<Group> finalFiltered = filtered;
                ThreadUtils.runOnMainThread(() -> {
                    filteredGroups.setValue(finalFiltered);
                    
                    Log.d(TAG, "Filters applied - " + finalFiltered.size() + 
                          " groups match criteria out of " + allPublicGroups.size() + " total");
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error applying filters", e);
                ThreadUtils.runOnMainThread(() -> {
                    setError("Error filtering groups: " + e.getMessage());
                });
            }
        });
    }
    
    private void handlePublicGroupsLoaded(List<Group> groups) {
        Log.d(TAG, "Public groups loaded successfully: " + groups.size() + " groups");
        
        allPublicGroups = new ArrayList<>(groups);
        
        ThreadUtils.runOnMainThread(() -> {
            setLoading(false);
            publicGroups.setValue(groups);
            
            // Apply current filters
            applyFilters();
            
            // Update pagination state
            hasMoreGroups.setValue(groups.size() >= GROUPS_PER_PAGE);
            currentPage.setValue(0);
            
            if (groups.isEmpty()) {
                setInfo("No public groups available at the moment");
            } else {
                setSuccess("Loaded " + groups.size() + " public groups");
            }
        });
    }
    
    private void handlePublicGroupsError(Exception error) {
        Log.e(TAG, "Failed to load public groups", error);
        
        ThreadUtils.runOnMainThread(() -> {
            setLoading(false);
            
            String errorMessage = error.getMessage();
            NetworkUtils.ErrorType errorType = NetworkUtils.ErrorType.UNKNOWN_ERROR;
            
            if (errorMessage != null) {
                if (errorMessage.toLowerCase().contains("network")) {
                    errorType = NetworkUtils.ErrorType.NETWORK_ERROR;
                } else if (errorMessage.toLowerCase().contains("permission")) {
                    errorType = NetworkUtils.ErrorType.PERMISSION_ERROR;
                }
            }
            
            setError("Failed to load public groups: " + 
                    (errorMessage != null ? errorMessage : "Unknown error"), errorType);
        });
    }
    
    private void handleJoinSuccess(Group group) {
        Log.d(TAG, "Successfully joined group: " + group.getGroupName());
        
        ThreadUtils.runOnMainThread(() -> {
            joinInProgress.setValue(false);
            joinedGroupKey.setValue(group.getGroupKey());
            setSuccess("Successfully joined '" + group.getGroupName() + "'!");
            
            // Refresh groups to update membership status
            loadPublicGroups(true);
        });
    }
    
    private void handleJoinError(Group group, Exception error) {
        Log.e(TAG, "Failed to join group: " + group.getGroupName(), error);
        
        ThreadUtils.runOnMainThread(() -> {
            joinInProgress.setValue(false);
            
            String errorMessage = error.getMessage();
            NetworkUtils.ErrorType errorType = NetworkUtils.ErrorType.UNKNOWN_ERROR;
            
            if (errorMessage != null) {
                if (errorMessage.toLowerCase().contains("full") || 
                    errorMessage.toLowerCase().contains("capacity")) {
                    errorType = NetworkUtils.ErrorType.VALIDATION_ERROR;
                    errorMessage = "Group is full";
                } else if (errorMessage.toLowerCase().contains("permission")) {
                    errorType = NetworkUtils.ErrorType.PERMISSION_ERROR;
                    errorMessage = "Not allowed to join this group";
                }
            }
            
            setError("Failed to join group: " + 
                    (errorMessage != null ? errorMessage : "Unknown error"), errorType);
        });
    }
    
    private void handleLeaveSuccess(Group group) {
        Log.d(TAG, "Successfully left group: " + group.getGroupName());
        
        ThreadUtils.runOnMainThread(() -> {
            joinInProgress.setValue(false);
            leftGroupKey.setValue(group.getGroupKey());
            setSuccess("Successfully left '" + group.getGroupName() + "'");
            
            // Refresh groups to update membership status
            loadPublicGroups(true);
        });
    }
    
    private void handleLeaveError(Group group, Exception error) {
        Log.e(TAG, "Failed to leave group: " + group.getGroupName(), error);
        
        ThreadUtils.runOnMainThread(() -> {
            joinInProgress.setValue(false);
            
            String errorMessage = error.getMessage();
            setError("Failed to leave group: " + 
                    (errorMessage != null ? errorMessage : "Unknown error"));
        });
    }
    
    /**
     * Refreshes the public groups list.
     */
    public void refreshGroups() {
        loadPublicGroups(true);
    }
    
    /**
     * Clears all discovery data and resets state.
     */
    public void clearDiscoveryData() {
        allPublicGroups.clear();
        publicGroups.setValue(new ArrayList<>());
        filteredGroups.setValue(new ArrayList<>());
        selectedGroup.setValue(null);
        joinedGroupKey.setValue(null);
        leftGroupKey.setValue(null);
        
        // Clear filters
        searchQuery.setValue(null);
        locationFilter.setValue(null);
        dateFilter.setValue(null);
        showOnlyJoinable.setValue(true);
        
        // Reset pagination
        currentPage.setValue(0);
        hasMoreGroups.setValue(false);
        isLoadingMore.setValue(false);
        
        // Reset join state
        joinInProgress.setValue(false);
        
        clearMessages();
        
        Log.d(TAG, "Discovery data cleared");
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        clearDiscoveryData();
        Log.d(TAG, "GroupDiscoveryViewModel cleared");
    }
}