package com.ugo.mecash_multicurrency_wallet.controller;

import com.ugo.mecash_multicurrency_wallet.dto.request.RefToken;
import com.ugo.mecash_multicurrency_wallet.dto.request.UserRequest;
import com.ugo.mecash_multicurrency_wallet.dto.response.LoginResponse;
import com.ugo.mecash_multicurrency_wallet.dto.response.UserResponse;
import com.ugo.mecash_multicurrency_wallet.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody UserRequest request, HttpServletResponse httpServletResponse) {
        return ResponseEntity.ok(userService.loginUser(request, httpServletResponse));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> getAccessToken(@RequestBody RefToken refToken) {
        return ResponseEntity.ok(userService.getAccessTokenUsingRefreshToken(refToken));
    }

    @GetMapping("/logout")
    public ResponseEntity<LoginResponse> logout(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok(
                    LoginResponse.builder()
                            .message("Logged out successfully")
                            .build()
            );
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(LoginResponse.builder()
                            .message("Logout failed: No token found")
                            .build()
                    );
        }
    }

}

