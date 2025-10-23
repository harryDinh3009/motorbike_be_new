package com.translateai.business.common.authenticate.impl.facebook;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FacebookGraphClient {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://graph.facebook.com")
            .build();

    @Value("${oauth2.facebook.app-id}")
    private String appId;

    @Value("${oauth2.facebook.app-secret}")
    private String appSecret;

    private DebugTokenCache debugTokenCache;

    @Value("${oauth2.facebook.debug-cache-ttl-ms:300000}")
    public void setDebugCacheTtl(long ttlMs) {
        this.debugTokenCache = new DebugTokenCache(ttlMs);
    }

    /**
     * Verify token through /debug_token (uses app access token appId|appSecret).
     * Cache result to avoid rate limiting.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> debugToken(String userAccessToken) {
        Map<String, Object> cached = debugTokenCache.get(userAccessToken);
        if (cached != null) return cached;

        String appAccessToken = appId + "|" + appSecret;
        Map<String, Object> resp = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/debug_token")
                        .queryParam("input_token", userAccessToken)
                        .queryParam("access_token", appAccessToken)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (resp != null) {
            debugTokenCache.put(userAccessToken, resp);
        }
        return resp;
    }

    /**
     * Get user info by calling /{userId}?fields=...
     * Uses appsecret_proof for extra security.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getUserInfo(String userId, String userAccessToken) {
        String appsecretProof = generateAppSecretProof(userAccessToken);

        Map<String, Object> resp = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/" + userId)
                        .queryParam("fields", "id,name,email,picture.width(200).height(200)")
                        .queryParam("access_token", userAccessToken)
                        .queryParam("appsecret_proof", appsecretProof)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return resp;
    }

    private String generateAppSecretProof(String accessToken) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] hash = sha256_HMAC.doFinal(accessToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Unable to generate appsecret_proof", e);
        }
    }
}
