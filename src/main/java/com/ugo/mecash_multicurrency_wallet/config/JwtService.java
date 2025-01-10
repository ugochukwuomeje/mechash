package com.ugo.mecash_multicurrency_wallet.config;

import com.ugo.mecash_multicurrency_wallet.service.UserDetailsImp;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
@Service
@Slf4j
public class JwtService {

    @Value("${grafana.jwt.access_token_secret_key}")
    private String SECRET_KEY; // = "51655468576D5A7134743777217A25432A462D4A614E635266556A586E327235";
    @Value("${grafana.jwt.refresh_token_secret_key}")
    private String REFRESH_TOKEN_SECRET_KEY; // =

    public String extractAccessTokenUsername(String token) {
        return extractAccessTokenClaim(token, Claims::getSubject);
    }

    public String extractRefreshTokenUsername(String token) {
        try {
            log.info("Attempting to extract username from refresh token: " + token);

            String username = extractRefreshTokenClaim(token, Claims::getSubject);

            if (username != null) {
                log.info("Extracted Refresh token username: " + username);
            } else {
                log.warn("No username found in the token claims.");
            }

            return username;
        } catch (Exception e) {
            log.error("Exception encountered at extractRefreshTokenUsername method: " + e.getMessage(), e);
        }

        return null;
    }

    private <T> T extractAccessTokenClaim(String token, Function<Claims, T> ClaimsResolver){
        final Claims claims = extractAllClaimsFromAccessToken(token);
        return ClaimsResolver.apply(claims);
    }

    private <T> T extractRefreshTokenClaim(String token, Function<Claims, T> ClaimsResolver){
        final Claims claims = extractAllClaimsFromRefereshToken(token);
        log.info("Logging Claims subject" + claims.getSubject());
        return ClaimsResolver.apply(claims);
    }

    public String generateAccessToken(UserDetails userDetails){
        UserDetailsImp userDetailsImp = (UserDetailsImp) userDetails;
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userDetailsImp.getUser().getId());
        claims.put("email", userDetailsImp.getUser().getEmail());
        claims.put("username", userDetailsImp.getUser().getUserName());
        claims.put("authorities", userDetailsImp.getAuthorities());
        claims.put("firstName", userDetailsImp.getUser().getFirstName());
        claims.put("lastName", userDetailsImp.getUser().getLastName());
        claims.put("role", userDetailsImp.getUser().getRole().getRoleName());
        return generateAccessToken(claims, userDetailsImp);
    }

    public String generateAccessToken(
            Map<String,Object> extractClaims,
            UserDetailsImp userDetails
    ){
        return Jwts
                .builder()
                .setIssuer("etz")
                .setClaims(extractClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean isAccessTokenValid(String token, UserDetails userDetails){
        final String username  = extractAccessTokenUsername(token);
        return (username.equals((userDetails.getUsername()))) && !isAccessTokenExpired(token);
    }

    public Boolean isRefreshTokenValid(String token, UserDetails userDetails){
        final String username  = extractAccessTokenUsername(token);
        return (username.equals((userDetails.getUsername()))) && !isAccessTokenExpired(token);
    }

    private boolean isAccessTokenExpired(String token) {
        return extractAccessTokenExpiration(token).before(new Date(System.currentTimeMillis()));
    }

    private boolean isRefreshTokenExpired(String token) {
        return extractRefreshTokenExpiration(token).before(new Date(System.currentTimeMillis()));
    }

    public Date extractAccessTokenExpiration(String token) {
        return extractAccessTokenClaim(token, Claims::getExpiration);
    }

    public Date extractRefreshTokenExpiration(String token) {
        return extractRefreshTokenClaim(token, Claims::getExpiration);
    }

    public String extractAccessTokenUserName(String token){
        return extractAccessTokenClaim(token, Claims::getSubject);
    }



    public Date extractAccessTokenIssuedAt(String token) {
        return extractAccessTokenClaim(token, Claims::getIssuedAt);
    }

    public Date extractRefreshTokenIssuedAt(String token) {
        return extractRefreshTokenClaim(token, Claims::getIssuedAt);
    }

    private Claims extractAllClaimsFromAccessToken(String token){
        return Jwts.parser()
                .setSigningKey(getSignInKey())
                .parseClaimsJws(token)
                .getBody();
    }

    private Claims extractAllClaimsFromRefereshToken(String token){
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(getSignInKeyForRefreshToken())
                    .parseClaimsJws(token)
                    .getBody();
            log.info("SignInKey for validation" + getSignInKeyForRefreshToken());
            log.info("Extracted Claims: " + claims);
            return claims;

        } catch (Exception e) {
            log.error("Failed to extract claims from the refresh token. Error: " + e.getMessage(), e);
            return null;
        }
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Key getSignInKeyForRefreshToken() {
        log.info("Refresh token for SignIn" + REFRESH_TOKEN_SECRET_KEY);
        byte[] keyBytes = Decoders.BASE64.decode(REFRESH_TOKEN_SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateRefreshToken(Map<String, Object> claims, UserDetailsImp userDetails) {
        log.info("[JwtTokenGenerator:generateRefreshToken] Refresh Token Creation Started for: {}", userDetails.getUsername());

        return Jwts
                .builder()
                .setIssuer("etz")
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(getSignInKeyForRefreshToken(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UserDetails userDetails) {
        UserDetailsImp userDetailsImp = (UserDetailsImp) userDetails;
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userDetailsImp.getUser().getId());
        claims.put("email", userDetailsImp.getUser().getEmail());
        claims.put("username", userDetailsImp.getUser().getUserName());
        claims.put("authorities", userDetailsImp.getAuthorities());
        claims.put("firstName", userDetailsImp.getUser().getFirstName());
        claims.put("lastName", userDetailsImp.getUser().getLastName());
        claims.put("role", userDetailsImp.getUser().getRole().getRoleName());

        return generateRefreshToken(claims, userDetailsImp);
    }
}
