package com.example.partymaker.server.controller;

import com.example.partymaker.server.service.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * REST Controller for handling Firebase-related API endpoints.
 * Provides CRUD operations for data, users, groups, and messages in Firebase.
 * All endpoints are under /api/firebase.
 */
@RestController
@RequestMapping("/api/firebase")
public class FirebaseController {

    private final FirebaseService firebaseService;

    @Autowired
    public FirebaseController(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    /**
     * Retrieves data from Firebase at the specified path.
     * @param path The path in Firebase.
     * @return Map of data at the path, or 500 on error.
     */
    @GetMapping("/data/{path}")
    public ResponseEntity<Map<String, Object>> getData(@PathVariable String path) {
        try {
            Map<String, Object> data = firebaseService.getData(path).get();
            return ResponseEntity.ok(data);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retrieves a list of data objects from Firebase at the specified path.
     * @param path The path in Firebase.
     * @return List of maps representing the data, or 500 on error.
     */
    @GetMapping("/list/{path}")
    public ResponseEntity<List<Map<String, Object>>> getDataAsList(@PathVariable String path) {
        try {
            List<Map<String, Object>> data = firebaseService.getDataAsList(path).get();
            return ResponseEntity.ok(data);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Saves data to Firebase at the specified path.
     * @param path The path in Firebase.
     * @param data The data to save.
     * @return 200 OK on success, 500 on error.
     */
    @PostMapping("/data/{path}")
    public ResponseEntity<Void> saveData(@PathVariable String path, @RequestBody Object data) {
        try {
            firebaseService.saveData(path, data).get();
            return ResponseEntity.ok().build();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Updates data at the specified path in Firebase.
     * @param path The path in Firebase.
     * @param updates The updates to apply.
     * @return 200 OK on success, 500 on error.
     */
    @PutMapping("/data/{path}")
    public ResponseEntity<Void> updateData(@PathVariable String path, @RequestBody Map<String, Object> updates) {
        try {
            firebaseService.updateData(path, updates).get();
            return ResponseEntity.ok().build();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Deletes data at the specified path in Firebase.
     * @param path The path in Firebase.
     * @return 200 OK on success, 500 on error.
     */
    @DeleteMapping("/data/{path}")
    public ResponseEntity<Void> deleteData(@PathVariable String path) {
        try {
            firebaseService.deleteData(path).get();
            return ResponseEntity.ok().build();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Retrieves user groups for a specific user.
     * @param userId The user ID.
     * @return Map of user groups, or 500 on error.
     */
    @GetMapping("/UserGroups/{userId}")
    public ResponseEntity<Map<String, Object>> getUserGroups(@PathVariable String userId) {
        try {
            // First try to get user groups
            Map<String, Object> userGroups = firebaseService.getData("UserGroups/" + userId).get();
            return ResponseEntity.ok(userGroups);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Saves user groups for a specific user.
     * @param userId The user ID.
     * @param data The data to save.
     * @return 200 OK on success, 500 on error.
     */
    @PostMapping("/UserGroups/{userId}")
    public ResponseEntity<Void> saveUserGroups(@PathVariable String userId, @RequestBody Object data) {
        try {
            firebaseService.saveData("UserGroups/" + userId, data).get();
            return ResponseEntity.ok().build();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Retrieves a single group by its ID.
     * @param groupId The group ID.
     * @return The group object, 404 if not found, 500 on error.
     */
    @GetMapping("/Groups/{groupId}")
    public ResponseEntity<Object> getGroup(@PathVariable String groupId) {
        try {
            System.out.println("DEBUG: Requesting group with ID: " + groupId);
            // First get all groups
            Map<String, Object> allGroups = firebaseService.getData("Groups").get();
            System.out.println("DEBUG: Retrieved " + allGroups.size() + " groups from Firebase");
            System.out.println("DEBUG: Available group keys: " + allGroups.keySet());
            
            // Check if the group exists
            if (allGroups.containsKey(groupId)) {
                System.out.println("DEBUG: Group found! Returning group data");
                return ResponseEntity.ok(allGroups.get(groupId));
            } else {
                System.out.println("DEBUG: Group not found in keys: " + allGroups.keySet());
                return ResponseEntity.notFound().build();
            }
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("DEBUG: Exception occurred: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Retrieves all groups from Firebase.
     * @return Map of all groups, or 500 on error.
     */
    @GetMapping("/Groups")
    public ResponseEntity<Map<String, Object>> getAllGroups() {
        try {
            System.out.println("DEBUG: Requesting all groups");
            Map<String, Object> allGroups = firebaseService.getData("Groups").get();
            System.out.println("DEBUG: Retrieved " + allGroups.size() + " groups from Firebase");
            return ResponseEntity.ok(allGroups);
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("DEBUG: Exception occurred while getting all groups: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Retrieves all messages from Firebase.
     * @return Map of all messages, or 500 on error.
     */
    @GetMapping("/GroupsMessages")
    public ResponseEntity<Map<String, Object>> getAllMessages() {
        try {
            System.out.println("DEBUG: Requesting all messages");
            Map<String, Object> allMessages = firebaseService.getData("GroupsMessages").get();
            System.out.println("DEBUG: Retrieved " + allMessages.size() + " messages from Firebase");
            return ResponseEntity.ok(allMessages);
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("DEBUG: Exception occurred while getting all messages: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Retrieves all messages for a specific group.
     * @param groupId The group ID.
     * @return Map of messages for the group, or 500 on error.
     */
    @GetMapping("/GroupsMessages/{groupId}")
    public ResponseEntity<Map<String, Object>> getGroupMessages(@PathVariable String groupId) {
        try {
            System.out.println("DEBUG: Getting messages for group: " + groupId);
            // Get all messages
            Map<String, Object> allMessages = firebaseService.getData("GroupsMessages").get();
            System.out.println("DEBUG: Retrieved " + allMessages.size() + " messages from Firebase");
            System.out.println("DEBUG: Message keys: " + allMessages.keySet());
            
            // Filter messages for this group
            Map<String, Object> groupMessages = new HashMap<>();
            for (Map.Entry<String, Object> entry : allMessages.entrySet()) {
                System.out.println("DEBUG: Processing message key: " + entry.getKey());
                if (entry.getValue() instanceof Map) {
                    Map<String, Object> message = (Map<String, Object>) entry.getValue();
                    System.out.println("DEBUG: Message data: " + message);
                    if (message.containsKey("groupId") && groupId.equals(message.get("groupId"))) {
                        System.out.println("DEBUG: Message matches group ID, adding to result");
                        groupMessages.put(entry.getKey(), message);
                    } else {
                        System.out.println("DEBUG: Message does not match group ID: " + message.get("groupId"));
                    }
                } else {
                    System.out.println("DEBUG: Entry value is not a Map: " + entry.getValue());
                }
            }
            
            System.out.println("DEBUG: Returning " + groupMessages.size() + " messages for group");
            return ResponseEntity.ok(groupMessages);
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("DEBUG: Exception in getGroupMessages: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Saves a message to Firebase under the specified message key.
     * @param messageKey The message key.
     * @param data The message data.
     * @return 200 OK on success, 500 on error.
     */
    @PostMapping("/GroupsMessages/{messageKey}")
    public ResponseEntity<Void> saveMessage(@PathVariable String messageKey, @RequestBody Object data) {
        try {
            firebaseService.saveData("GroupsMessages/" + messageKey, data).get();
            return ResponseEntity.ok().build();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Updates a group in Firebase (e.g., for updating MessageKeys).
     * @param groupId The group ID.
     * @param updates The updates to apply.
     * @return 200 OK on success, 500 on error.
     */
    @PutMapping("/Groups/{groupId}")
    public ResponseEntity<Void> updateGroup(@PathVariable String groupId, @RequestBody Map<String, Object> updates) {
        try {
            System.out.println("DEBUG: Updating group " + groupId + " with data: " + updates);
            firebaseService.updateData("Groups/" + groupId, updates).get();
            System.out.println("DEBUG: Group " + groupId + " updated successfully");
            return ResponseEntity.ok().build();
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("DEBUG: Failed to update group " + groupId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Retrieves all users from Firebase.
     * @return Map of all users, or 500 on error.
     */
    @GetMapping("/Users")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        try {
            System.out.println("DEBUG: Requesting all users");
            Map<String, Object> allUsers = firebaseService.getData("Users").get();
            System.out.println("DEBUG: Retrieved " + allUsers.size() + " users from Firebase");
            return ResponseEntity.ok(allUsers);
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("DEBUG: Exception occurred while getting all users: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Retrieves a single user by ID.
     * @param userId The user ID.
     * @return The user object, 404 if not found, 500 on error.
     */
    @GetMapping("/Users/{userId}")
    public ResponseEntity<Object> getUser(@PathVariable String userId) {
        try {
            System.out.println("DEBUG: Requesting user with ID: " + userId);
            // First get all users
            Map<String, Object> allUsers = firebaseService.getData("Users").get();
            System.out.println("DEBUG: Retrieved " + allUsers.size() + " users from Firebase");
            System.out.println("DEBUG: Available user keys: " + allUsers.keySet());
            
            // Check if the user exists
            if (allUsers.containsKey(userId)) {
                System.out.println("DEBUG: User found! Returning user data");
                return ResponseEntity.ok(allUsers.get(userId));
            } else {
                System.out.println("DEBUG: User not found in keys: " + allUsers.keySet());
                return ResponseEntity.notFound().build();
            }
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("DEBUG: Exception occurred: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Saves a user to Firebase.
     * @param userId The user ID.
     * @param data The user data.
     * @return 200 OK on success, 500 on error.
     */
    @PostMapping("/Users/{userId}")
    public ResponseEntity<Void> saveUser(@PathVariable String userId, @RequestBody Object data) {
        try {
            System.out.println("DEBUG: Saving user with ID: " + userId);
            firebaseService.saveData("Users/" + userId, data).get();
            System.out.println("DEBUG: User " + userId + " saved successfully");
            return ResponseEntity.ok().build();
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("DEBUG: Failed to save user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Updates a user in Firebase.
     * @param userId The user ID.
     * @param updates The updates to apply.
     * @return 200 OK on success, 500 on error.
     */
    @PutMapping("/Users/{userId}")
    public ResponseEntity<Void> updateUser(@PathVariable String userId, @RequestBody Map<String, Object> updates) {
        try {
            System.out.println("DEBUG: Updating user " + userId + " with data: " + updates);
            firebaseService.updateData("Users/" + userId, updates).get();
            System.out.println("DEBUG: User " + userId + " updated successfully");
            return ResponseEntity.ok().build();
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("DEBUG: Failed to update user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Deletes a user from Firebase.
     * @param userId The user ID.
     * @return 200 OK on success, 500 on error.
     */
    @DeleteMapping("/Users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        try {
            System.out.println("DEBUG: Deleting user with ID: " + userId);
            firebaseService.deleteData("Users/" + userId).get();
            System.out.println("DEBUG: User " + userId + " deleted successfully");
            return ResponseEntity.ok().build();
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("DEBUG: Failed to delete user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
} 