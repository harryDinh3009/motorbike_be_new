package com.translateai.business.common.authenticate.impl.facebook;

import com.translateai.dto.common.authenticate.FacebookUserInfoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class FacebookAuthService {

    private final FacebookGraphClient graphClient;

    /**
     * Verify access token and return user info.
     * Throws RuntimeException on invalid token (you can replace with custom exceptions).
     */
    public FacebookUserInfoDTO verifyAndGetUser(String accessToken) {
        // 1. debug token (returns object with "data": { is_valid, user_id, app_id, ... })
        Map<String, Object> debugResp = graphClient.debugToken(accessToken);
        if (debugResp == null || !debugResp.containsKey("data")) {
            throw new RuntimeException("Invalid debug token response from Facebook");
        }

        Map<String, Object> data = (Map<String, Object>) debugResp.get("data");

        Boolean isValid = (Boolean) data.get("is_valid");
        if (isValid == null || !isValid) {
            throw new RuntimeException("Facebook access token is not valid");
        }

        // Optionally check audience: ensure token was issued for our app id
        String tokenAppId = (String) data.get("app_id");
        // if (!expectedAppId.equals(tokenAppId)) throw ...

        String userId = (String) data.get("user_id");
        if (userId == null) throw new RuntimeException("No user_id in debug_token response");

        // 2. Get user info (with appsecret_proof)
        Map<String, Object> userInfoMap = graphClient.getUserInfo(userId, accessToken);
        if (userInfoMap == null || !userInfoMap.containsKey("id")) {
            throw new RuntimeException("Unable to fetch user info from Facebook");
        }

        String id = (String) userInfoMap.get("id");
        String name = (String) userInfoMap.get("name");
        String email = (String) userInfoMap.get("email");
        String pictureUrl = null;
        if (userInfoMap.get("picture") instanceof Map) {
            Map<?,?> pic = (Map<?,?>) userInfoMap.get("picture");
            if (pic.get("data") instanceof Map) {
                Object url = ((Map<?,?>) pic.get("data")).get("url");
                if (url != null) pictureUrl = url.toString();
            }
        }

        return new FacebookUserInfoDTO(id, name, email, pictureUrl);
    }

}
