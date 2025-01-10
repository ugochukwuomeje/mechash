package com.ugo.mecash_multicurrency_wallet.service.serviceImpl;

import com.ugo.mecash_multicurrency_wallet.config.JwtService;
import com.ugo.mecash_multicurrency_wallet.dto.request.RefToken;
import com.ugo.mecash_multicurrency_wallet.dto.request.UserRequest;
import com.ugo.mecash_multicurrency_wallet.dto.response.LoginResponse;
import com.ugo.mecash_multicurrency_wallet.dto.response.UserResponse;
import com.ugo.mecash_multicurrency_wallet.entity.User;
import com.ugo.mecash_multicurrency_wallet.enums.ResponseMessage;
import com.ugo.mecash_multicurrency_wallet.repository.UserRepository;
import com.ugo.mecash_multicurrency_wallet.service.UserDetailsImp;
import com.ugo.mecash_multicurrency_wallet.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    private final AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private LoginResponse loginResponse;


    public UserServiceImpl(AuthenticationManager authenticationManager, LoginResponse loginResponse) {
        this.authenticationManager = authenticationManager;
        this.loginResponse = loginResponse;
    }

    @Override
    public UserResponse registerUser(UserRequest request) {
        return null;
    }

    @Override
    public LoginResponse loginUser(UserRequest request, HttpServletResponse httpServletResponse) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            log.error("Invalid username or password for user: {}", request.getEmail());
            return LoginResponse.builder()
                    .message(ResponseMessage.INCORRECT_USERNAME_OR_PASSWORD.getStatusCode())
                    .build();
        } catch (InternalAuthenticationServiceException e) {
            log.error("Failed to load userDetails, Internal server error during authentication for user: {}", request.getEmail(), e);
            return LoginResponse.builder()
                    .message(ResponseMessage.INTERNAL_SERVER_ERROR.getStatusCode())
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error during authentication for user: {}", request.getEmail(), e);
            return LoginResponse.builder()
                    .message(ResponseMessage.INTERNAL_SERVER_ERROR.getStatusCode())
                    .build();
        }

        UserDetailsImp userDetails = (UserDetailsImp) authentication.getPrincipal();
        User user = userDetails.getUser();

        if (passwordEncoder.matches("Password", user.getPassword())) {
            log.info("User {} is logging in for the first time and needs to change their password.", user.getEmail());
            return LoginResponse.builder()
                    .message("Please change your password.")
                    .firstTimeLogin(true)
                    .build();
        }

        log.info("############# Log before generating token ##################");

        String roleName = userDetails.getUser().getRole().getRoleName();
        String jwtToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        log.info("Generated Access token: {}", jwtToken);
        log.info("Generated Refresh Token: {}", refreshToken);

        Date issuedAt = jwtService.extractAccessTokenIssuedAt(jwtToken);
        Date expirationDate = jwtService.extractAccessTokenExpiration(jwtToken);

        createRefreshTokenCookie(httpServletResponse, refreshToken);

        return LoginResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .role(roleName)
                .issuedAt(issuedAt.toString())
                .expirationDate(expirationDate.toString())
                .message("Login successful")
                .build();
    }


    private Cookie createRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setMaxAge(15 * 24 * 60 * 60);
        response.addCookie(refreshTokenCookie);
        return refreshTokenCookie;
    }

    public Object getAccessTokenUsingRefreshToken(RefToken refToken) {
        try {
            String refreshToken = refToken.getRefreshToken();

            log.info("Refresh Token: service(1) {}", refreshToken);
            if (refreshToken == null || refreshToken.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token is missing or empty");
            }

            log.info("Refresh token not null/empty: {}", refreshToken);
            String userName = jwtService.extractRefreshTokenUsername(refreshToken);
            log.info("Extracted Refresh token username: {}", userName);

            User users = userRepository.findByEmail(userName)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            UserDetailsImp userDetails = new UserDetailsImp(users);
            String newAccessToken = jwtService.generateAccessToken(userDetails);
            Date issuedAt = jwtService.extractAccessTokenIssuedAt(newAccessToken);
            Date expirationDate = jwtService.extractAccessTokenExpiration(newAccessToken);

            log.info("Log after generating new access token");

            return LoginResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .role(users.getRole().getRoleName())
                    .issuedAt(issuedAt.toString())
                    .expirationDate(expirationDate.toString())
                    .message("New Access Token Generated")
                    .build();

        } catch (ResponseStatusException e) {
            log.error("Error during access token generation: {}", e.getMessage(), e);
            loginResponse.setMessage(ResponseMessage.ERROR_ACCESSING_TOKEN.getStatusCode());

        } catch (Exception e) {
            log.error("Unexpected error during access token generation", e);
            loginResponse.setMessage(ResponseMessage.INTERNAL_SERVER_ERROR.getStatusCode());
        }
        return null;
    }

}
