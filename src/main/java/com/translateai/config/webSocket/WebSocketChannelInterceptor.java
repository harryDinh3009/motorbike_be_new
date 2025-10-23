package com.translateai.config.webSocket;

import com.translateai.common.ApiStatus;
import com.translateai.config.exception.RestApiException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    @Value ("${app.secretKey}")
    private String secretKey;

    @Autowired
    private WebSocketSessionManager webSocketSessionManager;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        try {
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
            System.out.println(accessor.getCommand());
            if (StompCommand.SEND.equals(accessor.getCommand()) || StompCommand.CONNECT.equals(accessor.getCommand())) {
                String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
                if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                    String jwtToken = authorizationHeader.substring(7);
                    if (StringUtils.isNotBlank(jwtToken)) {
                        if (! validateToken(jwtToken, accessor.getSessionId())) {
                            throw new RestApiException(ApiStatus.UNAUTHORIZED);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RestApiException(ApiStatus.UNAUTHORIZED);
        }

        return message;
    }

    public boolean validateToken(String token, String sessionId) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes())).build()
                    .parseClaimsJws(token);

            Date expirationDate = claims.getBody().getExpiration();
            if (expirationDate.before(new Date())) {
                return false;
            }

            String fullName = claims.getBody().get("fullName", String.class);
            String userName = claims.getBody().get("userName", String.class);
            String id = claims.getBody().get("id", String.class);
            String avatar = claims.getBody().get("avatar", String.class);
            String email = claims.getBody().getSubject();

            webSocketSessionManager.storeSessionInfo(sessionId,
                    SessionWebSocketInfo.builder().id(id).avatar(avatar).email(email).fullName(fullName).jwtToken(token)
                            .userName(userName).build());

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new RestApiException(ApiStatus.UNAUTHORIZED);
        }
    }

}