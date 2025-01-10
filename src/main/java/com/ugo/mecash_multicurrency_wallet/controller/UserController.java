package com.ugo.mecash_multicurrency_wallet.controller;

import com.ugo.mecash_multicurrency_wallet.dto.request.RefToken;
import com.ugo.mecash_multicurrency_wallet.dto.request.UserRequest;
import com.ugo.mecash_multicurrency_wallet.dto.response.LoginResponse;
import com.ugo.mecash_multicurrency_wallet.dto.response.UserResponse;
import com.ugo.mecash_multicurrency_wallet.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
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
    public ResponseEntity<LoginResponse> getAccessToken(@RequestBody RefToken refToken) {
        return null;
    }

}

