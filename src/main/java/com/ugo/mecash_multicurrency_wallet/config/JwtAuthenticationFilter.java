package com.ugo.mecash_multicurrency_wallet.config;

import com.ugo.mecash_multicurrency_wallet.service.UserDetailsImp;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        log.info("=================================== request received by the second filter");
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String email;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            email = jwtService.extractAccessTokenEmail(jwt);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.info("Extracted email" + email);
                UserDetailsImp userDetails = (UserDetailsImp) this.userDetailsService.loadUserByUsername(email);

                if (jwtService.isAccessTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    log.error("Token is invalid or expired");
                    throw new SecurityException("Token is invalid or expired");
                }
            }
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired", e);
            handleException(response, "Token expired. Please refresh your token.");
            return;
        } catch (SecurityException e) {
            log.error("Security exception: {}", e.getMessage());
            handleException(response, e.getMessage());
            return;
        } catch (Exception e) {
            log.error("JWT token processing error: {}", e.getMessage());
            handleException(response, "Token processing error. Please try again.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void handleException(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        String jsonResponse = String.format(
                "{\"status\": %d, \"error\": \"Unauthorized\", \"message\": \"%s\"}",
                HttpServletResponse.SC_UNAUTHORIZED,
                message
        );

        response.getWriter().write(jsonResponse);
    }
}
