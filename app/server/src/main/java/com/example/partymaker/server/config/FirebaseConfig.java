package com.example.partymaker.server.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;

import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.getApplicationDefault())
                    .setDatabaseUrl("https://partymaker-9c966-default-rtdb.firebaseio.com")
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Bean
    public FirebaseDatabase firebaseDatabase() {
        return FirebaseDatabase.getInstance();
    }
} 