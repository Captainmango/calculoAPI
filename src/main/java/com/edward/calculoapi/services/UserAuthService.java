package com.edward.calculoapi.services;

import com.edward.calculoapi.api.dto.requests.CreateAccountRequest;
import com.edward.calculoapi.api.dto.requests.LogInRequest;
import com.edward.calculoapi.api.dto.requests.TokenRefreshRequest;
import com.edward.calculoapi.api.dto.responses.LogInResponse;
import com.edward.calculoapi.exceptions.EmailInUseException;
import com.edward.calculoapi.exceptions.TokenRefreshException;
import com.edward.calculoapi.api.models.ERole;
import com.edward.calculoapi.api.models.RefreshToken;
import com.edward.calculoapi.api.models.User;
import com.edward.calculoapi.database.repositories.RoleRepository;
import com.edward.calculoapi.database.repositories.UserRepository;
import com.edward.calculoapi.security.jwt.JwtUtils;
import com.edward.calculoapi.security.services.RefreshTokenService;
import com.edward.calculoapi.security.services.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.stream.Collectors;

@Service
public class UserAuthService {
    private static final Logger logger = LoggerFactory.getLogger(UserAuthService.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    public UserAuthService(AuthenticationManager authenticationManager, UserRepository userRepository, RefreshTokenService refreshTokenService, RoleRepository roleRepository, PasswordEncoder encoder, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
    }

    public ResponseEntity<LogInResponse> loginUser(
            @Valid @RequestBody LogInRequest loginRequest
    ) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(authentication);
        String jwtToken = jwtUtils.generateJwtToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(
                new LogInResponse(
                        userDetails.getId(),
                        userDetails.getFirstName(),
                        userDetails.getEmail(),
                        refreshToken.getToken(),
                        jwtToken
                ));
    }

    public LogInRequest createUserAccount(@Valid @RequestBody CreateAccountRequest createAccountRequest){
        if (userRepository.existsByEmail(createAccountRequest.getEmail())) {
            logger.warn("User attempted to use email that was already in use. Request: {}", createAccountRequest);
            throw new EmailInUseException("Email already in use");
        }

        User user = new User(
                createAccountRequest.getFirstName(),
                createAccountRequest.getLastName(),
                createAccountRequest.getEmail(),
                encoder.encode(createAccountRequest.getPassword())
        );

        var roles = createAccountRequest.getRoles().stream()
                .map(ERole::getByName)
                .map(eRole -> roleRepository.findByName(eRole).orElseThrow())
                .collect(Collectors.toSet());

        user.setRoles(roles);

        userRepository.save(user);

        return new LogInRequest(
                createAccountRequest.getEmail(),
                createAccountRequest.getPassword()
        );
    }

    public ResponseEntity<?> loginWithRefresh(@Valid @RequestBody TokenRefreshRequest tokenRefreshRequest){
        String requestRefreshToken = tokenRefreshRequest.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    ResponseCookie token = jwtUtils.generateJwtCookieFromEmail(user.getEmail());
                    String jwt = jwtUtils.generateJwtFromEmail(user.getEmail());
                    return ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, token.toString())
                            .body(new LogInResponse(
                                    user.getId(),
                                    user.getFirstName(),
                                    user.getEmail(),
                                    requestRefreshToken,
                                    jwt
                            ));
                })
                .orElseThrow(() -> {
                    logger.error("[UserAuthService@loginWithRefresh] attempt to use token that does not exist " +
                            "Token: {}", requestRefreshToken);
                    return new TokenRefreshException(requestRefreshToken, "Refresh token is not in database!");
                });
    }
}
