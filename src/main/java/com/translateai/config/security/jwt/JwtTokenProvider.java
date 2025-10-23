package com.translateai.config.security.jwt;

import com.translateai.common.ApiStatus;
import com.translateai.config.exception.RestApiException;
import com.translateai.entity.domain.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    @Value("${app.secretKey}")
    private String secretKey;

    @Value("${app.jwtExpirationMs}")
    private Integer jwtExpirationMs;

    public String generateTokenUser(UserEntity userEntity) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.SECOND, jwtExpirationMs);
        Date expiryDate = calendar.getTime();

        List<String> roles = userEntity.getRoles().stream()
                .map(role -> String.valueOf(role.getRlId()))
                .collect(Collectors.toList());

        String avatarDefault = userEntity.getAvatar();

        String token = Jwts.builder()
                .setSubject(userEntity.getEmail())
                .claim("avatar", avatarDefault)
                .claim("fullName", userEntity.getFullName())
                .claim("userName", userEntity.getUserName())
                .claim("id", userEntity.getId())
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, key())
                .compact();

        return token;
    }

    public Authentication getAuthentication(String token, UserDetails userDetails, HttpServletRequest httpServletRequest) {
        Claims claims = parseTokenClaims(token);
        List<String> roles = claims.get("roles", List.class);
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(role))
                .collect(Collectors.toList());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
        return authentication;
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token);

            Date expirationDate = claims.getBody().getExpiration();
            if (expirationDate.before(new Date())) {
                return false;
            }

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new RestApiException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    private Claims parseTokenClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getEmailFromToken(String token) {
        return parseTokenClaims(token).getSubject();
    }

}
