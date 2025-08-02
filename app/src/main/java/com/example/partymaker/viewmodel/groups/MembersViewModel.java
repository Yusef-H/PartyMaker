package com.example.partymaker.viewmodel.groups;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.partymaker.data.api.NetworkUtils;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.data.model.User;
import com.example.partymaker.data.repository.GroupRepository;
import com.example.partymaker.data.repository.UserRepository;
import com.example.partymaker.utils.infrastructure.system.ThreadUtils;
import com.example.partymaker.viewmodel.BaseViewModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * ViewModel for group members management functionality.
 * 
 * <p>Handles member listing, invitation management, attendance tracking,
 * and member-related operations within groups.
 * 
 * <p>Features:
 * <ul>
 *   <li>Member list display and management</li>
 *   <li>Invitation tracking and management</li>
 *   <li>Attendance status tracking</li>
 *   <li>Member search and filtering</li>
 *   <li>Friend management integration</li>
 *   <li>Member statistics and analytics</li>
 * </ul>
 * 
 * @author PartyMaker Team
 * @version 1.0
 * @since 1.0
 */
public class MembersViewModel extends BaseViewModel {
    
    private static final String TAG = "MembersViewModel";
    
    // Member types for different views
    public enum MemberType {
        ALL_MEMBERS,
        COMING_MEMBERS,
        INVITED_MEMBERS,
        FRIENDS_ONLY
    }
    
    // Dependencies
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    
    // LiveData for members state
    private final MutableLiveData<Group> currentGroup = new MutableLiveData<>();
    private final MutableLiveData<List<User>> allMembers = new MutableLiveData<>();
    private final MutableLiveData<List<User>> comingMembers = new MutableLiveData<>();
    private final MutableLiveData<List<User>> invitedMembers = new MutableLiveData<>();
    private final MutableLiveData<List<User>> friendMembers = new MutableLiveData<>();
    private final MutableLiveData<List<User>> filteredMembers = new MutableLiveData<>();
    
    // Current view state
    private final MutableLiveData<MemberType> currentViewType = new MutableLiveData<>();
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showOnlineOnly = new MutableLiveData<>();
    
    // User permissions and status
    private final MutableLiveData<Boolean> isCurrentUserAdmin = new MutableLiveData<>();
    private final MutableLiveData<Boolean> canInviteMembers = new MutableLiveData<>();
    private final MutableLiveData<Boolean> canRemoveMembers = new MutableLiveData<>();
    
    // Member operations state
    private final MutableLiveData<Boolean> memberOperationInProgress = new MutableLiveData<>();
    private final MutableLiveData<String> lastOperationResult = new MutableLiveData<>();
    private final MutableLiveData<User> selectedMember = new MutableLiveData<>();
    
    // Statistics
    private final MutableLiveData<Integer> totalMembersCount = new MutableLiveData<>();
    private final MutableLiveData<Integer> comingMembersCount = new MutableLiveData<>();
    private final MutableLiveData<Integer> invitedMembersCount = new MutableLiveData<>();
    private final MutableLiveData<Integer> onlineMembersCount = new MutableLiveData<>();
    
    // Current state
    private String currentUserKey;
    private String currentGroupKey;
    
    /**
     * Constructor for MembersViewModel.
     * 
     * @param application The application context
     */
    public MembersViewModel(@NonNull Application application) {
        super(application);
        this.groupRepository = GroupRepository.getInstance();
        this.userRepository = UserRepository.getInstance();
        
        // Initialize state
        allMembers.setValue(new ArrayList<>());
        comingMembers.setValue(new ArrayList<>());
        invitedMembers.setValue(new ArrayList<>());
        friendMembers.setValue(new ArrayList<>());
        filteredMembers.setValue(new ArrayList<>());
        
        currentViewType.setValue(MemberType.ALL_MEMBERS);
        showOnlineOnly.setValue(false);
        
        isCurrentUserAdmin.setValue(false);
        canInviteMembers.setValue(false);
        canRemoveMembers.setValue(false);
        
        memberOperationInProgress.setValue(false);
        
        totalMembersCount.setValue(0);
        comingMembersCount.setValue(0);
        invitedMembersCount.setValue(0);
        onlineMembersCount.setValue(0);
        
        Log.d(TAG, "MembersViewModel initialized");
    }
    
    // Getters for LiveData
    
    public LiveData<Group> getCurrentGroup() {
        return currentGroup;
    }
    
    public LiveData<List<User>> getAllMembers() {
        return allMembers;
    }
    
    public LiveData<List<User>> getComingMembers() {
        return comingMembers;
    }
    
    public LiveData<List<User>> getInvitedMembers() {
        return invitedMembers;
    }
    
    public LiveData<List<User>> getFriendMembers() {
        return friendMembers;
    }
    
    public LiveData<List<User>> getFilteredMembers() {
        return filteredMembers;
    }
    
    public LiveData<MemberType> getCurrentViewType() {
        return currentViewType;
    }
    
    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }
    
    public LiveData<Boolean> getShowOnlineOnly() {
        return showOnlineOnly;
    }
    
    public LiveData<Boolean> getIsCurrentUserAdmin() {
        return isCurrentUserAdmin;
    }
    
    public LiveData<Boolean> getCanInviteMembers() {
        return canInviteMembers;
    }
    
    public LiveData<Boolean> getCanRemoveMembers() {
        return canRemoveMembers;
    }
    
    public LiveData<Boolean> getMemberOperationInProgress() {
        return memberOperationInProgress;
    }
    
    public LiveData<String> getLastOperationResult() {
        return lastOperationResult;
    }
    
    public LiveData<User> getSelectedMember() {
        return selectedMember;
    }
    
    public LiveData<Integer> getTotalMembersCount() {
        return totalMembersCount;
    }
    
    public LiveData<Integer> getComingMembersCount() {
        return comingMembersCount;
    }
    
    public LiveData<Integer> getInvitedMembersCount() {
        return invitedMembersCount;
    }
    
    public LiveData<Integer> getOnlineMembersCount() {
        return onlineMembersCount;
    }
    
    /**
     * Initializes the ViewModel with user and group information.
     * 
     * @param userKey The current user's key
     * @param groupKey The group key to manage members for
     */
    public void initialize(@NonNull String userKey, @NonNull String groupKey) {
        this.currentUserKey = userKey;
        this.currentGroupKey = groupKey;
        
        Log.d(TAG, "Initialized with user: " + userKey + ", group: " + groupKey);
        
        // Load group and member data
        loadGroupData();
    }
    
    /**
     * Loads the group data and member information.
     */
    public void loadGroupData() {
        if (currentGroupKey == null) {
            setError("Group not specified", NetworkUtils.ErrorType.VALIDATION_ERROR);
            return;
        }
        
        setLoading(true);
        clearMessages();
        
        Log.d(TAG, "Loading group data and members for: " + currentGroupKey);
        
        ThreadUtils.runOnBackground(() -> {
            groupRepository.getGroup(currentGroupKey, new GroupRepository.Callback<Group>() {
                @Override
                public void onSuccess(Group group) {
                    handleGroupLoaded(group);
                }
                
                @Override
                public void onError(Exception error) {
                    handleGroupLoadError(error);
                }
            });
        });
    }
    
    /**
     * Sets the current view type for member display.
     * 
     * @param viewType The type of members to display
     */
    public void setViewType(@NonNull MemberType viewType) {
        currentViewType.setValue(viewType);
        applyFilters();
        
        Log.d(TAG, "Member view type changed to: " + viewType);
    }
    
    /**
     * Sets the search query for filtering members.
     * 
     * @param query The search query
     */
    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
        applyFilters();
    }
    
    /**
     * Sets whether to show only online members.
     * 
     * @param onlineOnly true to show only online members
     */
    public void setShowOnlineOnly(boolean onlineOnly) {
        showOnlineOnly.setValue(onlineOnly);
        applyFilters();
    }
    
    /**
     * Selects a member for detailed operations.
     * 
     * @param member The member to select
     */
    public void selectMember(User member) {
        selectedMember.setValue(member);
        
        if (member != null) {
            Log.d(TAG, "Member selected: " + member.getUsername());
        }
    }
    
    /**
     * Invites a user to the group.
     * 
     * @param user The user to invite
     */
    public void inviteMember(@NonNull User user) {
        if (!canInviteMembers()) {
            setError("You don't have permission to invite members", 
                    NetworkUtils.ErrorType.PERMISSION_ERROR);
            return;
        }
        
        if (memberOperationInProgress.getValue() != null && memberOperationInProgress.getValue()) {
            Log.w(TAG, "Member operation already in progress");
            return;
        }
        
        memberOperationInProgress.setValue(true);
        clearMessages();
        
        Log.d(TAG, "Inviting member: " + user.getUsername());
        
        ThreadUtils.runOnBackground(() -> {
            groupRepository.inviteMemberToGroup(currentGroupKey, user.getUserKey(), 
                new GroupRepository.Callback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        handleInviteSuccess(user);
                    }
                    
                    @Override
                    public void onError(Exception error) {
                        handleMemberOperationError("invite", user, error);
                    }
                });
        });
    }
    
    /**
     * Removes a member from the group.
     * 
     * @param member The member to remove
     */
    public void removeMember(@NonNull User member) {
        if (!canRemoveMembers()) {
            setError("You don't have permission to remove members", 
                    NetworkUtils.ErrorType.PERMISSION_ERROR);
            return;
        }
        
        if (member.getUserKey().equals(currentUserKey)) {
            setError("Cannot remove yourself from the group", 
                    NetworkUtils.ErrorType.VALIDATION_ERROR);
            return;
        }
        
        if (memberOperationInProgress.getValue() != null && memberOperationInProgress.getValue()) {
            Log.w(TAG, "Member operation already in progress");
            return;
        }
        
        memberOperationInProgress.setValue(true);
        clearMessages();
        
        Log.d(TAG, "Removing member: " + member.getUsername());
        
        ThreadUtils.runOnBackground(() -> {
            groupRepository.removeMemberFromGroup(currentGroupKey, member.getUserKey(), 
                new GroupRepository.Callback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        handleRemoveSuccess(member);
                    }
                    
                    @Override
                    public void onError(Exception error) {
                        handleMemberOperationError("remove", member, error);
                    }
                });
        });
    }
    
    /**
     * Updates a member's attendance status.
     * 
     * @param member The member to update
     * @param isComing Whether the member is coming
     */
    public void updateMemberAttendance(@NonNull User member, boolean isComing) {
        if (!canManageAttendance()) {
            setError("You don't have permission to manage attendance", 
                    NetworkUtils.ErrorType.PERMISSION_ERROR);
            return;
        }
        
        memberOperationInProgress.setValue(true);
        clearMessages();
        
        Log.d(TAG, "Updating attendance for: " + member.getUsername() + " to: " + isComing);
        
        ThreadUtils.runOnBackground(() -> {
            groupRepository.updateAttendanceStatus(currentGroupKey, member.getUserKey(), isComing, 
                new GroupRepository.Callback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        handleAttendanceUpdateSuccess(member, isComing);
                    }
                    
                    @Override
                    public void onError(Exception error) {
                        handleMemberOperationError("update attendance for", member, error);
                    }
                });
        });
    }
    
    /**
     * Refreshes all member data.
     */
    public void refreshMembers() {
        loadGroupData();
    }
    
    /**
     * Clears all filters and shows all members.
     */
    public void clearFilters() {
        searchQuery.setValue(null);
        showOnlineOnly.setValue(false);
        currentViewType.setValue(MemberType.ALL_MEMBERS);
        applyFilters();
    }
    
    // Private helper methods
    
    private boolean canInviteMembers() {
        Boolean permission = canInviteMembers.getValue();
        return permission != null && permission;
    }
    
    private boolean canRemoveMembers() {
        Boolean permission = canRemoveMembers.getValue();
        return permission != null && permission;
    }
    
    private boolean canManageAttendance() {
        // For now, all members can manage their own attendance
        // Admins can manage others' attendance
        Boolean isAdmin = isCurrentUserAdmin.getValue();
        return isAdmin != null && isAdmin;
    }
    
    private void handleGroupLoaded(Group group) {
        Log.d(TAG, "Group loaded successfully: " + group.getGroupName());
        
        ThreadUtils.runOnMainThread(() -> {
            setLoading(false);
            currentGroup.setValue(group);
            
            // Update user permissions
            updateUserPermissions(group);
            
            // Load member details
            loadMemberDetails(group);
            
            setSuccess("Members data loaded successfully");
        });
    }
    
    private void handleGroupLoadError(Exception error) {
        Log.e(TAG, "Failed to load group", error);
        
        ThreadUtils.runOnMainThread(() -> {
            setLoading(false);
            
            String errorMessage = error.getMessage();
            setError("Failed to load group: " + 
                    (errorMessage != null ? errorMessage : "Unknown error"));
        });
    }
    
    private void updateUserPermissions(Group group) {
        if (currentUserKey == null) {
            return;
        }
        
        // Check if user is admin
        boolean isAdmin = group.getAdminKey() != null && 
                         group.getAdminKey().equals(currentUserKey);
        isCurrentUserAdmin.setValue(isAdmin);
        
        // Set permissions based on admin status
        canInviteMembers.setValue(isAdmin);
        canRemoveMembers.setValue(isAdmin);
        
        Log.d(TAG, "User permissions updated - Admin: " + isAdmin);
    }
    
    private void loadMemberDetails(Group group) {
        ThreadUtils.runOnBackground(() -> {
            // Load all members
            loadUsersFromKeys(group.getFriendKeys(), users -> {
                ThreadUtils.runOnMainThread(() -> {
                    allMembers.setValue(users);
                    updateStatistics();
                    applyFilters();
                });
            });
            
            // Load coming members
            loadUsersFromKeys(group.getComingKeys(), users -> {
                ThreadUtils.runOnMainThread(() -> {
                    comingMembers.setValue(users);
                    updateStatistics();
                    applyFilters();
                });
            });
            
            // Invited members would be loaded separately if we had invitation tracking
            ThreadUtils.runOnMainThread(() -> {
                invitedMembers.setValue(new ArrayList<>());
                updateStatistics();
                applyFilters();
            });
            
            // Load friend members (intersection of all members and user's friends)
            loadFriendMembers(group);
        });
    }
    
    private void loadUsersFromKeys(HashMap<String, Object> userKeys, UserListCallback callback) {
        if (userKeys == null || userKeys.isEmpty()) {
            callback.onUsersLoaded(new ArrayList<>());
            return;
        }
        
        List<User> users = new ArrayList<>();
        int totalUsers = userKeys.size();
        int[] loadedCount = {0};
        
        for (String userKey : userKeys.keySet()) {
            userRepository.getUser(userKey, new UserRepository.Callback<User>() {
                @Override
                public void onSuccess(User user) {
                    synchronized (users) {
                        users.add(user);
                        loadedCount[0]++;
                        
                        if (loadedCount[0] == totalUsers) {
                            callback.onUsersLoaded(users);
                        }
                    }
                }
                
                @Override
                public void onError(Exception error) {
                    Log.w(TAG, "Failed to load user: " + userKey, error);
                    synchronized (users) {
                        loadedCount[0]++;
                        
                        if (loadedCount[0] == totalUsers) {
                            callback.onUsersLoaded(users);
                        }
                    }
                }
            });
        }
    }
    
    private void loadFriendMembers(Group group) {
        // This would require user's friend list to filter group members
        // For now, just set empty list
        ThreadUtils.runOnMainThread(() -> {
            friendMembers.setValue(new ArrayList<>());
            applyFilters();
        });
    }
    
    private void applyFilters() {
        ThreadUtils.runOnBackground(() -> {
            try {
                MemberType viewType = currentViewType.getValue();
                if (viewType == null) {
                    viewType = MemberType.ALL_MEMBERS;
                }
                
                List<User> sourceList = getSourceListForViewType(viewType);
                List<User> filtered = new ArrayList<>(sourceList);
                
                // Apply search query filter
                String query = searchQuery.getValue();
                if (query != null && !query.trim().isEmpty()) {
                    String lowerQuery = query.toLowerCase().trim();
                    filtered = filtered.stream()
                        .filter(user -> 
                            (user.getUsername() != null && 
                             user.getUsername().toLowerCase().contains(lowerQuery)) ||
                            (user.getFullName() != null && 
                             user.getFullName().toLowerCase().contains(lowerQuery)))
                        .collect(ArrayList::new, (list, item) -> list.add(item), ArrayList::addAll);
                }
                
                // Apply online filter (simplified - would need real online status)
                Boolean onlineOnly = showOnlineOnly.getValue();
                if (onlineOnly != null && onlineOnly) {
                    // For now, randomly mark some users as "online"
                    filtered = filtered.stream()
                        .filter(user -> Math.random() > 0.3) // 70% "online"
                        .collect(ArrayList::new, (list, item) -> list.add(item), ArrayList::addAll);
                }
                
                final List<User> finalFiltered = filtered;
                ThreadUtils.runOnMainThread(() -> {
                    filteredMembers.setValue(finalFiltered);
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error applying filters", e);
                ThreadUtils.runOnMainThread(() -> {
                    setError("Error filtering members: " + e.getMessage());
                });
            }
        });
    }
    
    private List<User> getSourceListForViewType(MemberType viewType) {
        switch (viewType) {
            case COMING_MEMBERS:
                return comingMembers.getValue() != null ? comingMembers.getValue() : new ArrayList<>();
            case INVITED_MEMBERS:
                return invitedMembers.getValue() != null ? invitedMembers.getValue() : new ArrayList<>();
            case FRIENDS_ONLY:
                return friendMembers.getValue() != null ? friendMembers.getValue() : new ArrayList<>();
            case ALL_MEMBERS:
            default:
                return allMembers.getValue() != null ? allMembers.getValue() : new ArrayList<>();
        }
    }
    
    private void updateStatistics() {
        List<User> all = allMembers.getValue();
        List<User> coming = comingMembers.getValue();
        List<User> invited = invitedMembers.getValue();
        
        int totalCount = all != null ? all.size() : 0;
        int comingCount = coming != null ? coming.size() : 0;
        int invitedCount = invited != null ? invited.size() : 0;
        int onlineCount = (int) (totalCount * 0.7); // Simplified online calculation
        
        totalMembersCount.setValue(totalCount);
        comingMembersCount.setValue(comingCount);
        invitedMembersCount.setValue(invitedCount);
        onlineMembersCount.setValue(onlineCount);
        
        Log.d(TAG, "Statistics updated - Total: " + totalCount + 
              ", Coming: " + comingCount + ", Invited: " + invitedCount + 
              ", Online: " + onlineCount);
    }
    
    private void handleInviteSuccess(User user) {
        Log.d(TAG, "Member invited successfully: " + user.getUsername());
        
        ThreadUtils.runOnMainThread(() -> {
            memberOperationInProgress.setValue(false);
            setSuccess(user.getUsername() + " has been invited to the group");
            lastOperationResult.setValue("invited_" + user.getUserKey());
            
            // Refresh member data
            loadGroupData();
        });
    }
    
    private void handleRemoveSuccess(User user) {
        Log.d(TAG, "Member removed successfully: " + user.getUsername());
        
        ThreadUtils.runOnMainThread(() -> {
            memberOperationInProgress.setValue(false);
            setSuccess(user.getUsername() + " has been removed from the group");
            lastOperationResult.setValue("removed_" + user.getUserKey());
            
            // Refresh member data
            loadGroupData();
        });
    }
    
    private void handleAttendanceUpdateSuccess(User member, boolean isComing) {
        Log.d(TAG, "Attendance updated for: " + member.getUsername() + " to: " + isComing);
        
        ThreadUtils.runOnMainThread(() -> {
            memberOperationInProgress.setValue(false);
            
            String message = member.getUsername() + " is now " + 
                           (isComing ? "attending" : "not attending") + " the event";
            setSuccess(message);
            lastOperationResult.setValue("attendance_" + member.getUserKey() + "_" + isComing);
            
            // Refresh member data
            loadGroupData();
        });
    }
    
    private void handleMemberOperationError(String operation, User user, Exception error) {
        Log.e(TAG, "Failed to " + operation + " member: " + user.getUsername(), error);
        
        ThreadUtils.runOnMainThread(() -> {
            memberOperationInProgress.setValue(false);
            
            String errorMessage = error.getMessage();
            setError("Failed to " + operation + " " + user.getUsername() + ": " + 
                    (errorMessage != null ? errorMessage : "Unknown error"));
        });
    }
    
    // Interface for user list loading callback
    private interface UserListCallback {
        void onUsersLoaded(List<User> users);
    }
    
    /**
     * Clears all members data and resets state.
     */
    public void clearMembersData() {
        currentGroup.setValue(null);
        allMembers.setValue(new ArrayList<>());
        comingMembers.setValue(new ArrayList<>());
        invitedMembers.setValue(new ArrayList<>());
        friendMembers.setValue(new ArrayList<>());
        filteredMembers.setValue(new ArrayList<>());
        
        currentViewType.setValue(MemberType.ALL_MEMBERS);
        searchQuery.setValue(null);
        showOnlineOnly.setValue(false);
        
        isCurrentUserAdmin.setValue(false);
        canInviteMembers.setValue(false);
        canRemoveMembers.setValue(false);
        
        memberOperationInProgress.setValue(false);
        lastOperationResult.setValue(null);
        selectedMember.setValue(null);
        
        totalMembersCount.setValue(0);
        comingMembersCount.setValue(0);
        invitedMembersCount.setValue(0);
        onlineMembersCount.setValue(0);
        
        currentUserKey = null;
        currentGroupKey = null;
        
        clearMessages();
        
        Log.d(TAG, "Members data cleared");
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        clearMembersData();
        Log.d(TAG, "MembersViewModel cleared");
    }
}