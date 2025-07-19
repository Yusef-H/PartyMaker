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
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Firebase server is running");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{path}")
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

    @PostMapping("/{path}")
    public ResponseEntity<Void> saveData(@PathVariable String path, @RequestBody Object data) {
        try {
            firebaseService.saveData(path, data).get();
            return ResponseEntity.ok().build();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{path}")
    public ResponseEntity<Void> updateData(@PathVariable String path, @RequestBody Map<String, Object> updates) {
        try {
            firebaseService.updateData(path, updates).get();
            return ResponseEntity.ok().build();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{path}")
    public ResponseEntity<Void> deleteData(@PathVariable String path) {
        try {
            firebaseService.deleteData(path).get();
            return ResponseEntity.ok().build();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
} 