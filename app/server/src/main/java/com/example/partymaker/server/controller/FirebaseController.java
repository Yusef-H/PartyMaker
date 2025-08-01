package com.example.partymaker.server.controller;

import com.example.partymaker.server.service.FirebaseService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * REST Controller for handling Firebase-related API endpoints.
 * Provides CRUD operations for data, users, groups, and messages in Firebase.
 * All endpoints are under /api/firebase.
 * 
 * <p>Features:
 * <ul>
 *   <li>Comprehensive input validation
 *   <li>Structured error handling and logging
 *   <li>Consistent response formats
 *   <li>Security best practices
 * </ul>
 */
@RestController
@RequestMapping("/api/firebase")
@Validated
public class FirebaseController {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseController.class);
    
    private final FirebaseService firebaseService;

    @Autowired
    public FirebaseController(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    /**
     * Creates a standardized error response.
     *
     * @param message The error message
     * @return A map containing the error details
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("message", message);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }

    /**
     * Retrieves data from Firebase at the specified path.
     *
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
     *
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
     *
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
     *
     * @param path    The path in Firebase.
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
     *
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
     *
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
     *
     * @param userId The user ID.
     * @param data   The data to save.
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
     *
     * @param groupId The group ID (must not be blank)
     * @return ResponseEntity with group data, 404 if not found, 400 for invalid input, 500 on server error
     */
    @GetMapping("/Groups/{groupId}")
    public ResponseEntity<Object> getGroup(@PathVariable @NotBlank(message = "Group ID cannot be blank") String groupId) {
        logger.info("Retrieving group with ID: {}", groupId);
        
        try {
            // Validate input
            if (groupId.trim().isEmpty()) {
                logger.warn("Invalid group ID provided: empty or whitespace only");
                return ResponseEntity.badRequest().body(createErrorResponse("Group ID cannot be empty"));
            }
            
            // Get all groups from Firebase
            Map<String, Object> allGroups = firebaseService.getData("Groups").get();
            logger.debug("Retrieved {} groups from Firebase", allGroups.size());

            // Check if the group exists
            if (allGroups.containsKey(groupId)) {
                logger.info("Group found successfully: {}", groupId);
                return ResponseEntity.ok(allGroups.get(groupId));
            } else {
                logger.warn("Group not found: {}. Available groups: {}", groupId, allGroups.keySet());
                return ResponseEntity.notFound().build();
            }
        } catch (InterruptedException e) {
            logger.error("Thread interrupted while retrieving group: {}", groupId, e);
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Request was interrupted"));
        } catch (ExecutionException e) {
            logger.error("Execution error while retrieving group: {}", groupId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to retrieve group data"));
        } catch (Exception e) {
            logger.error("Unexpected error while retrieving group: {}", groupId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("An unexpected error occurred"));
        }
    }

    /**
     * Retrieves all groups from Firebase.
     *
     * @return ResponseEntity with map of all groups, or 500 on server error
     */
    @GetMapping("/Groups")
    public ResponseEntity<Object> getAllGroups() {
        logger.info("Retrieving all groups");
        
        try {
            Map<String, Object> allGroups = firebaseService.getData("Groups").get();
            logger.info("Successfully retrieved {} groups from Firebase", allGroups.size());
            return ResponseEntity.ok(allGroups);
        } catch (InterruptedException e) {
            logger.error("Thread interrupted while retrieving all groups", e);
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Request was interrupted"));
        } catch (ExecutionException e) {
            logger.error("Execution error while retrieving all groups", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to retrieve groups data"));
        } catch (Exception e) {
            logger.error("Unexpected error while retrieving all groups", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("An unexpected error occurred"));
        }
    }

    /**
     * Retrieves all messages from Firebase.
     *
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
     *
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
     *
     * @param messageKey The message key (must not be blank)
     * @param data       The message data (must not be null)
     * @return ResponseEntity with 201 Created on success, 400 for invalid input, 500 on server error
     */
    @PostMapping("/GroupsMessages/{messageKey}")
    public ResponseEntity<Object> saveMessage(
            @PathVariable @NotBlank(message = "Message key cannot be blank") String messageKey,
            @RequestBody @Valid Object data) {
        
        logger.info("Saving message with key: {}", messageKey);
        logger.debug("Message data: {}", data);
        
        try {
            // Validate input
            if (messageKey.trim().isEmpty()) {
                logger.warn("Invalid message key provided: empty or whitespace only");
                return ResponseEntity.badRequest().body(createErrorResponse("Message key cannot be empty"));
            }
            
            if (data == null) {
                logger.warn("Null message data provided for key: {}", messageKey);
                return ResponseEntity.badRequest().body(createErrorResponse("Message data cannot be null"));
            }
            
            firebaseService.saveData("GroupsMessages/" + messageKey, data).get();
            logger.info("Message saved successfully with key: {}", messageKey);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (InterruptedException e) {
            logger.error("Thread interrupted while saving message: {}", messageKey, e);
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Request was interrupted"));
        } catch (ExecutionException e) {
            logger.error("Execution error while saving message: {}", messageKey, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to save message"));
        } catch (Exception e) {
            logger.error("Unexpected error while saving message: {}", messageKey, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("An unexpected error occurred"));
        }
    }

    /**
     * Updates a group in Firebase (e.g., for updating MessageKeys).
     *
     * @param groupId The group ID (must not be blank)
     * @param updates The updates to apply (must not be empty)
     * @return ResponseEntity with 200 OK on success, 400 for invalid input, 500 on server error
     */
    @PutMapping("/Groups/{groupId}")
    public ResponseEntity<Object> updateGroup(
            @PathVariable @NotBlank(message = "Group ID cannot be blank") String groupId,
            @RequestBody @NotEmpty(message = "Updates cannot be empty") Map<String, Object> updates) {
        
        logger.info("Updating group {} with {} fields", groupId, updates.size());
        logger.debug("Update data: {}", updates);
        
        try {
            // Validate input
            if (groupId.trim().isEmpty()) {
                logger.warn("Invalid group ID provided for update: empty or whitespace only");
                return ResponseEntity.badRequest().body(createErrorResponse("Group ID cannot be empty"));
            }
            
            if (updates.isEmpty()) {
                logger.warn("Empty updates provided for group: {}", groupId);
                return ResponseEntity.badRequest().body(createErrorResponse("Updates cannot be empty"));
            }
            
            firebaseService.updateData("Groups/" + groupId, updates).get();
            logger.info("Group {} updated successfully", groupId);
            return ResponseEntity.ok().build();
        } catch (InterruptedException e) {
            logger.error("Thread interrupted while updating group: {}", groupId, e);
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Request was interrupted"));
        } catch (ExecutionException e) {
            logger.error("Execution error while updating group: {}", groupId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to update group"));
        } catch (Exception e) {
            logger.error("Unexpected error while updating group: {}", groupId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("An unexpected error occurred"));
        }
    }

    /**
     * Deletes a group from Firebase.
     *
     * @param groupId The group ID.
     * @return 200 OK on success, 404 if not found, 500 on error.
     */
    @DeleteMapping("/Groups/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable String groupId) {
        try {
            System.out.println("DEBUG: Deleting group with ID: " + groupId);
            firebaseService.deleteData("Groups/" + groupId).get();
            System.out.println("DEBUG: Group " + groupId + " deleted successfully");
            return ResponseEntity.ok().build();
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("DEBUG: Failed to delete group " + groupId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retrieves all users from Firebase.
     *
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
     *
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
     *
     * @param userId The user ID.
     * @param data   The user data.
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
     *
     * @param userId  The user ID.
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
     *
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