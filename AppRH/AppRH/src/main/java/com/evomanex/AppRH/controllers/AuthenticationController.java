package com.evomanex.AppRH.controllers;

import com.evomanex.AppRH.domain.user.AuthenticationDTO;
import com.evomanex.AppRH.domain.user.LoginResponseDTO;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.infra.security.TokenService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private static final long TOKEN_EXPIRATION_SECONDS = 7200L;

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthenticationController(
            AuthenticationManager authenticationManager,
            TokenService tokenService
    ) {
        this.authenticationManager =
                authenticationManager;

        this.tokenService =
                tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid
            @RequestBody
            AuthenticationDTO request
    ) {
        var usernamePassword =
                new UsernamePasswordAuthenticationToken(
                        request.login(),
                        request.password()
                );

        var authentication =
                authenticationManager.authenticate(
                        usernamePassword
                );

        User authenticatedUser =
                (User) authentication.getPrincipal();

        String accessToken =
                tokenService.generateToken(
                        authenticatedUser
                );

        LoginResponseDTO response =
                new LoginResponseDTO(
                        accessToken,
                        "Bearer",
                        TOKEN_EXPIRATION_SECONDS
                );

        return ResponseEntity.ok(response);
    }
}