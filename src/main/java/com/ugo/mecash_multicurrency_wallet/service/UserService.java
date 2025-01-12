package com.ugo.mecash_multicurrency_wallet.service;

import com.ugo.mecash_multicurrency_wallet.dto.request.RefToken;
import com.ugo.mecash_multicurrency_wallet.dto.request.UserRequest;
import com.ugo.mecash_multicurrency_wallet.dto.response.LoginResponse;
import com.ugo.mecash_multicurrency_wallet.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    UserResponse registerUser(UserRequest request);
    LoginResponse loginUser(UserRequest request, HttpServletResponse httpServletResponse);

    Object getAccessTokenUsingRefreshToken(RefToken refToken);
}

