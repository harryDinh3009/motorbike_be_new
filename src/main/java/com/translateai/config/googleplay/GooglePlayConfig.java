package com.translateai.config.googleplay;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.Collections;

/**
 * Configuration for Google Play Developer API
 */
@Configuration
@Slf4j
public class GooglePlayConfig {

    @Value("${google.play.package.name}")
    private String packageName;

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "AI Translate App";
    private static final String SERVICE_ACCOUNT_KEY_PATH = "files/service-account-key.json";

    @Bean
    public AndroidPublisher androidPublisher() {
        log.info("Initializing Google Play Developer API client");
        
        try {
            // Load service account credentials from classpath
            ClassPathResource resource = new ClassPathResource(SERVICE_ACCOUNT_KEY_PATH);
            
            if (!resource.exists()) {
                log.warn("Service account key file not found at classpath: {}. Google Play API will not be available.", SERVICE_ACCOUNT_KEY_PATH);
                log.warn("Creating dummy AndroidPublisher bean. Google Play features will not work.");
                return createDummyPublisher();
            }
            
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            
            // Load credentials from classpath resource
            try (InputStream inputStream = resource.getInputStream()) {
                GoogleCredential credential = GoogleCredential
                        .fromStream(inputStream, httpTransport, JSON_FACTORY)
                        .createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));
                
                log.info("Successfully initialized Google Play Developer API client for package: {}", packageName);
                
                return new AndroidPublisher.Builder(httpTransport, JSON_FACTORY, credential)
                        .setApplicationName(APPLICATION_NAME)
                        .build();
            }
                    
        } catch (Exception e) {
            log.error("Failed to initialize Google Play Developer API: {}", e.getMessage(), e);
            log.warn("Creating dummy AndroidPublisher bean. Google Play features will not work.");
            return createDummyPublisher();
        }
    }
    
    private AndroidPublisher createDummyPublisher() {
        try {
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            // Create a builder without credentials (will fail on actual API calls but bean exists)
            return new AndroidPublisher.Builder(httpTransport, JSON_FACTORY, null)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create dummy AndroidPublisher", e);
        }
    }

    @Bean
    public String googlePlayPackageName() {
        return packageName;
    }
}
