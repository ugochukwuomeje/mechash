package com.ugo.mecash_multicurrency_wallet.service.serviceImpl;

import com.ugo.mecash_multicurrency_wallet.config.JwtService;
import com.ugo.mecash_multicurrency_wallet.dto.request.RefToken;
import com.ugo.mecash_multicurrency_wallet.dto.request.UserRequest;
import com.ugo.mecash_multicurrency_wallet.dto.response.LoginResponse;
import com.ugo.mecash_multicurrency_wallet.dto.response.UserResponse;
import com.ugo.mecash_multicurrency_wallet.entity.Role;
import com.ugo.mecash_multicurrency_wallet.entity.User;
import com.ugo.mecash_multicurrency_wallet.enums.ResponseMessage;
import com.ugo.mecash_multicurrency_wallet.repository.RoleRepository;
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
    private final LoginResponse loginResponse;
    @Autowired
    private RoleRepository roleRepository;


    public UserServiceImpl(AuthenticationManager authenticationManager, LoginResponse loginResponse) {
        this.authenticationManager = authenticationManager;
        this.loginResponse = loginResponse;
    }

    @Override
    public UserResponse registerUser(UserRequest userRequest) {
        UserResponse response = new UserResponse();

        try {
            ////////////////////////////////// Check if a user with the provided email already exists
            if (userRepository.findUserByEmail(userRequest.getEmail())) {
                response.setMessage(ResponseMessage.EMAIL_ALREADY_EXIST.getStatusCode());
                return response;
            }

            ////////////////////////////////// Check if a user with the provided username already exists
            if (userRepository.findByUserName(userRequest.getUserName())) {
                response.setMessage(ResponseMessage.USERNAME_ALREADY_EXIST.getStatusCode());
                return response;
            }

            ///////////////////////////////////// Create a new User object
            User user = new User();
            user.setFirstName(userRequest.getFirstName());
            user.setLastName(userRequest.getLastName());
            user.setUserName(userRequest.getUserName());
            user.setEmail(userRequest.getEmail());
            user.setPassword(userRequest.getPassword());

            String roleName = userRequest.getRole();
            Role role = roleRepository.findByRoleName(roleName);
            if (role == null) {
                response.setMessage("Role with name \"" + roleName + "\" does not exist.");
                return response;
            }
            user.setRole(role);

            ///////////////////////////////////// Generate a default password for the user
          //  String defaultPassword = generateRandomPassword(8);
            String defaultPassword = userRequest.getPassword();
            user.setPassword(passwordEncoder.encode(defaultPassword));

            ///////////////////////////////// Save the user and flush
            User savedUser = userRepository.saveAndFlush(user);

            //////////////////////////////////////////// Convert the saved user to a response DTO
         //   UserResponse response = new UserResponse();
         //   response = convertToDTO(savedUser);

            response.setId(savedUser.getId());
            response.setEmail(savedUser.getEmail());
            response.setUserName(savedUser.getUserName());
            response.setFirstName(savedUser.getFirstName());
            response.setLastName(savedUser.getLastName());
            response.setRole(savedUser.getRole().getRoleName());
            response.setMessage("User registered successfully");

        } catch (Exception e) {
            log.error("Error during user registration: {}", e.getMessage(), e);
            response.setMessage(ResponseMessage.REGISTRATION_ERROR.getStatusCode());
        }

        return response;
    }


    private UserResponse convertToDTO(User user) {
        UserResponse userResponse = new UserResponse();

        userResponse.setId(user.getId());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setUserName(user.getUserName());
        log.info("username picked from DB:" + user.getUserName());
        userResponse.setEmail(user.getEmail());

        if (user.getRole() != null) {
            userResponse.setRole(user.getRole().getRoleName());
        } else {
            log.warn("User with ID " + user.getId() + " has no role assigned");
        }
        return userResponse;
    }

    private String generateRandomPassword(int i) {
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
                    .message(ResponseMessage.INVALID_CREDENTIALS.getStatusCode())
                    .build();
        } catch (InternalAuthenticationServiceException e) {
            log.error("Failed to load userDetails, Internal server error during authentication for user: {}", request.getEmail(), e);
            return LoginResponse.builder()
                    .message(ResponseMessage.INTERNAL_SERVER_ERROR_DUE_TO_AUTHENTICATION.getStatusCode())
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error during authentication for user: {}", request.getEmail(), e);
            return LoginResponse.builder()
                    .message(ResponseMessage.INTERNAL_SERVER_ERROR.getStatusCode())
                    .build();
        }

        UserDetailsImp userDetails = (UserDetailsImp) authentication.getPrincipal();
        User user = userDetails.getUser();

//        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
//            log.info("User {} is logging in for the first time and needs to change their password.", user.getEmail());
//            return LoginResponse.builder()
//                    .message("Please change your password.")
//                    .firstTimeLogin(true)
//                    .build();
//        }

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

    @Override
    public Object getAccessTokenUsingRefreshToken(RefToken refToken) {
        try {
            String refreshToken = refToken.getRefreshToken();

            log.info("Refresh Token: service(1) {}", refreshToken);
            if (refreshToken == null || refreshToken.isEmpty()) {
                log.error("Refresh token is missing or empty");
                LoginResponse.builder().message(ResponseMessage.BAD_REQUEST.getStatusCode());
            }

            log.info("Refresh token not null/empty: {}", refreshToken);
            String email = jwtService.extractRefreshTokenEmail(refreshToken);
            log.info("Extracted Refresh token username: {}", email);

            User users = userRepository.findByEmail(email)
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
