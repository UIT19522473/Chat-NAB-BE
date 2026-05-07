package com.chat.nab.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account.path}")
    private String firebaseServiceAccountPath;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        InputStream serviceAccount;

        if (firebaseServiceAccountPath.startsWith("classpath:")) {
            String classpathFile = firebaseServiceAccountPath.replace("classpath:", "");
            serviceAccount = getClass().getResourceAsStream(classpathFile);

            if (serviceAccount == null) {
                throw new IOException("Firebase service account not found in classpath: " + classpathFile);
            }
        } else {
            serviceAccount = new FileInputStream(firebaseServiceAccountPath);
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp app = FirebaseApp.initializeApp(options);

        log.info("Firebase initialized successfully");

        return app;
    }
}