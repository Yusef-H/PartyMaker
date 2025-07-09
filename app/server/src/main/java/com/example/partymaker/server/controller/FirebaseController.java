package com.example.partymaker.server.controller;

import com.example.partymaker.server.service.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/firebase")
public class FirebaseController {

    private final FirebaseService firebaseService;

    @Autowired
    public FirebaseController(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

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
    
    // Specific endpoint for UserGroups
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
    
    // Specific endpoint for saving UserGroups
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
    
    // Specific endpoint for getting a single group by ID
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
    
    // Specific endpoint for getting all groups
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
    
    // Specific endpoint for getting all messages
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
    
    // Specific endpoint for getting messages for a group
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
    
    // Specific endpoint for saving a message
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
    
    // Specific endpoint for updating a group (for MessageKeys updates)
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
    
    // Specific endpoint for getting all users
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
    
    // Specific endpoint for getting a single user by ID
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
    
    // Specific endpoint for saving a user
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
    
    // Specific endpoint for updating a user
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
    
    // Specific endpoint for deleting a user
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