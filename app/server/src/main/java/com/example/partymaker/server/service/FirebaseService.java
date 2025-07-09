package com.example.partymaker.server.service;

import com.google.firebase.database.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class FirebaseService {

    private final FirebaseDatabase firebaseDatabase;

    @Autowired
    public FirebaseService(FirebaseDatabase firebaseDatabase) {
        this.firebaseDatabase = firebaseDatabase;
    }

    public CompletableFuture<Map<String, Object>> getData(String path) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        
        DatabaseReference ref = firebaseDatabase.getReference(path);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> result = new HashMap<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        result.put(child.getKey(), child.getValue());
                    }
                }
                future.complete(result);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });
        
        return future;
    }

    public CompletableFuture<List<Map<String, Object>>> getDataAsList(String path) {
        CompletableFuture<List<Map<String, Object>>> future = new CompletableFuture<>();
        
        DatabaseReference ref = firebaseDatabase.getReference(path);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Map<String, Object>> result = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", child.getKey());
                        for (DataSnapshot field : child.getChildren()) {
                            item.put(field.getKey(), field.getValue());
                        }
                        result.add(item);
                    }
                }
                future.complete(result);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });
        
        return future;
    }

    public CompletableFuture<Void> saveData(String path, Object data) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        DatabaseReference ref = firebaseDatabase.getReference(path);
        ref.setValue(data, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                future.completeExceptionally(databaseError.toException());
            } else {
                future.complete(null);
            }
        });
        
        return future;
    }

    public CompletableFuture<Void> updateData(String path, Map<String, Object> updates) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        DatabaseReference ref = firebaseDatabase.getReference(path);
        ref.updateChildren(updates, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                future.completeExceptionally(databaseError.toException());
            } else {
                future.complete(null);
            }
        });
        
        return future;
    }

    public CompletableFuture<Void> deleteData(String path) {
        return saveData(path, null);
    }
} 