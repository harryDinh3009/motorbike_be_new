package com.translateai.business.common.authenticate.impl.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.translateai.common.ApiStatus;
import com.translateai.config.exception.RestApiException;
import com.translateai.entity.domain.UserEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Random;

@Service
public class GoogleTokenVerifierService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String CLIENT_ID_GOOGLE;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String CLIENT_SECRET_GOOGLE;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String CLIENT_REDIRECT_URI;

    @Value("${google.oauth2.token.endpoint}")
    private String GOOGLE_OAUTH2_TOKEN_END_POINT;

    public UserEntity verifyGoogleToken(String idTokenString) {
        try {
            HttpTransport transport = new NetHttpTransport();
            JsonFactory jsonFactory = new JacksonFactory();

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                    .setAudience(Collections.singletonList(CLIENT_ID_GOOGLE))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                String email = payload.getEmail();
                String fullName = (String) payload.get("name");
                String avatarUrl = (String) payload.get("picture");

                UserEntity userEntity = new UserEntity();
                userEntity.setFullName(fullName);
                userEntity.setEmail(email);
                userEntity.setUserName(email.split("@")[0]);
                userEntity.setAvatar(avatarUrl);

                return userEntity;
            } else {
                throw new RestApiException(ApiStatus.TOKEN_VERIFICATION_FAILED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RestApiException(ApiStatus.TOKEN_VERIFICATION_FAILED);
        }
    }

//    public UserEntity verifyGoogleToken(String code) {
//        try {
//            HttpTransport httpTransport = new NetHttpTransport();
//            JsonFactory jsonFactory = new JacksonFactory();
//            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
//                    httpTransport,
//                    jsonFactory,
//                    GOOGLE_OAUTH2_TOKEN_END_POINT,
//                    CLIENT_ID_GOOGLE,
//                    CLIENT_SECRET_GOOGLE,
//                    code,
//                    CLIENT_REDIRECT_URI
//            ).execute();
//            String idToken = tokenResponse.getIdToken();
//            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(httpTransport, jsonFactory)
//                    .setAudience(Collections.singletonList(CLIENT_ID_GOOGLE))
//                    .build();
//            GoogleIdToken googleIdToken = verifier.verify(idToken);
//            if (googleIdToken != null) {
//                Payload payload = googleIdToken.getPayload();
//
//                String email = payload.getEmail();
//                String fullName = (String) payload.get("name");
//                String avatarUrl = (String) payload.get("picture");
//
//                String emailName = email.split("@")[0];
//
//                UserEntity userEntity = new UserEntity();
//                userEntity.setFullName(fullName);
//                userEntity.setEmail(email);
//                userEntity.setUserName(generateRandomUserName(emailName));
//                userEntity.setAvatar(avatarUrl);
//
//                return userEntity;
//            } else {
//                throw new RestApiException(ApiStatus.TOKEN_VERIFICATION_FAILED);
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            throw new RestApiException(ApiStatus.TOKEN_VERIFICATION_FAILED);
//        }
//    }

    private static String generateRandomUserName(String emailName) {
        Random random = new Random();
        StringBuilder userName = new StringBuilder();
        StringBuilder currentWord = new StringBuilder();
        boolean isDigitPart = false;
        for (char ch : emailName.toCharArray()) {
            if (Character.isDigit(ch)) {
                if (!isDigitPart) {
                    if (!currentWord.isEmpty()) {
                        userName.append(currentWord.substring(0, currentWord.length() - 1));
                    }
                    currentWord.setLength(0);
                    isDigitPart = true;
                }
                currentWord.append(ch);
            } else {
                if (isDigitPart) {
                    if (!currentWord.isEmpty()) {
                        userName.append(currentWord);
                    }
                    currentWord.setLength(0);
                    isDigitPart = false;
                }
                currentWord.append(ch);
            }
        }
        if (!currentWord.isEmpty() && !isDigitPart) {
            userName.append(currentWord.substring(0, currentWord.length() - 1));
        }
        int randomNumber = 1000 + random.nextInt(9000);
        userName.append(randomNumber);
        return userName.toString();
    }

}

