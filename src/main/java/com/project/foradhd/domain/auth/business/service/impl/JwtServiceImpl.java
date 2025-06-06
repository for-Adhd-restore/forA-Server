package com.project.foradhd.domain.auth.business.service.impl;

import static com.project.foradhd.global.enums.CacheKeyType.USER_REFRESH_TOKEN;
import static org.springframework.util.StringUtils.collectionToCommaDelimitedString;

import com.project.foradhd.domain.auth.business.service.JwtService;
import com.project.foradhd.global.service.CacheService;
import com.project.foradhd.global.util.JsonUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtServiceImpl implements JwtService {

    private static final String EMAIL_CLAIM_NAME = "email";
    private static final String AUTHORITIES_CLAIM_NAME = "authorities";
    private static final String JWT_SEPARATOR = "\\.";
    private final CacheService cacheService;
    private final Long accessTokenExpiry;
    private final Long refreshTokenExpiry;
    private final Key key;

    public JwtServiceImpl(CacheService cacheService,
        @Value("${jwt.expiry.access-token}") Long accessTokenExpiry,
        @Value("${jwt.expiry.refresh-token}") Long refreshTokenExpiry,
        @Value("${jwt.secret-key}") String secretKey) {
        this.cacheService = cacheService;
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    @Override
    public String generateAccessToken(String userId, String email,
        Collection<GrantedAuthority> authorities) {
        Date now = new Date();
        return Jwts.builder()
            .setSubject(userId)
            .addClaims(Map.of(EMAIL_CLAIM_NAME, email,
                AUTHORITIES_CLAIM_NAME, collectionToCommaDelimitedString(authorities)))
            .setIssuedAt(now)
            .setExpiration(calculateExpiration(now, accessTokenExpiry))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    @Override
    public String generateRefreshToken(String userId) {
        Date now = new Date();
        return Jwts.builder()
            .setSubject(userId)
            .setIssuedAt(now)
            .setExpiration(calculateExpiration(now, refreshTokenExpiry))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    @Override
    public void validateTokenExpiry(String token) {
        validateTokenExpiry(token, key);
    }

    @Override
    public void validateTokenExpiry(String token, Key key) {
        try {
            parseToken(token, key);
        } catch (UnsupportedJwtException e) {
            log.error("The claimsJws argument does not represent an Claims JWS");
            throw e;
        } catch (MalformedJwtException e) {
            log.error("The claimsJws string is not a valid JWS");
            throw e;
        } catch (SignatureException e) {
            log.error("The claimsJws JWS signature validation fails");
            throw e;
        } catch (ExpiredJwtException e) {
            log.error("The specified JWT is a Claims JWT and the Claims has an expiration time before the time this method is invoked.");
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("The claimsJws string is null or empty or only whitespace");
            throw new JwtException(e.getMessage(), e);
        }
    }

    @Override
    public void validateTokenForm(String token) {
        try {
            parseToken(token, key);
        }  catch (UnsupportedJwtException e) {
            log.error("The claimsJws argument does not represent an Claims JWS");
            throw e;
        } catch (MalformedJwtException e) {
            log.error("The claimsJws string is not a valid JWS");
            throw e;
        } catch (SignatureException e) {
            log.error("The claimsJws JWS signature validation fails");
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("The claimsJws string is null or empty or only whitespace");
            throw new JwtException(e.getMessage(), e);
        } catch (ExpiredJwtException e) { }
    }

    @Override
    public boolean isValidTokenExpiry(String token) {
        try {
            validateTokenExpiry(token);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public boolean isValidTokenForm(String token) {
        try {
            validateTokenForm(token);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public String getSubject(String token) {
        Claims claims = parseExpiredToken(token, key);
        return claims.getSubject();
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities(String token) {
        Claims claims = parseExpiredToken(token, key);
        String authorityString = claims.get(AUTHORITIES_CLAIM_NAME, String.class);
        return AuthorityUtils.commaSeparatedStringToAuthorityList(authorityString);
    }

    @Override
    public Map<String, Object> decodeHeader(String token) {
        String encodedHeader = token.split(JWT_SEPARATOR)[0];
        Base64.Decoder decoder = Base64.getUrlDecoder();

        byte[] decodedHeaderBytes = decoder.decode(encodedHeader.getBytes(StandardCharsets.UTF_8));
        String decodedHeader = new String(decodedHeaderBytes, StandardCharsets.UTF_8);
        return JsonUtil.readValue(decodedHeader, Map.class);
    }

    @Override
    public Map<String, Object> decodePayload(String token) {
        String encodedPayload = token.split(JWT_SEPARATOR)[1];
        Base64.Decoder decoder = Base64.getUrlDecoder();

        byte[] decodedPayloadBytes = decoder.decode(encodedPayload.getBytes(StandardCharsets.UTF_8));
        String decodedPayload = new String(decodedPayloadBytes, StandardCharsets.UTF_8);
        return JsonUtil.readValue(decodedPayload, Map.class);
    }

    @Override
    public Claims parseToken(String token, Key key) {
        return Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @Override
    public Claims parseExpiredToken(String token, Key key) {
        try {
            return parseToken(token, key);
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    @Override
    public void saveRefreshToken(String userId, String refreshToken) {
        cacheService.setValue(USER_REFRESH_TOKEN, userId, refreshToken, refreshTokenExpiry, TimeUnit.MILLISECONDS);
    }

    @Override
    public void deleteRefreshToken(String userId) {
        cacheService.deleteValue(USER_REFRESH_TOKEN, userId);
    }

    @Override
    public boolean existsSavedRefreshToken(String userId, String refreshToken) {
        return cacheService.getValue(USER_REFRESH_TOKEN, userId)
                .filter(savedRefreshToken -> savedRefreshToken.equals(refreshToken))
                .isPresent();
    }

    private Date calculateExpiration(Date now, Long expiry) {
        Instant expirationInstant = now.toInstant().plusMillis(expiry);
        return Date.from(expirationInstant);
    }
}
