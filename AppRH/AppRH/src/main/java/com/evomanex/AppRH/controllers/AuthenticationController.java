package com.evomanex.AppRH.controllers;

import com.evomanex.AppRH.domain.user.AuthenticationDTO;
import com.evomanex.AppRH.domain.user.LoginResponseDTO;
import com.evomanex.AppRH.domain.user.RegisterDTO;
import com.evomanex.AppRH.domain.user.RegisterResponseDTO;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.domain.user.UserRole;

import com.evomanex.AppRH.infra.security.TokenService;
import com.evomanex.AppRH.repository.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(
        name = "Autenticação",
        description = "Cadastro de candidatos e autenticação de usuários"
)
@SecurityRequirements
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationController(
            AuthenticationManager authenticationManager,
            TokenService tokenService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager =
                authenticationManager;

        this.tokenService =
                tokenService;

        this.userRepository =
                userRepository;

        this.passwordEncoder =
                passwordEncoder;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Autenticar usuário",
            description = "Valida login e senha e retorna um token JWT"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticação realizada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation =
                                            LoginResponseDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Login ou senha inválidos",
                    content = @Content
            )
    })
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
                        TokenService.EXPIRATION_SECONDS
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(
            summary = "Cadastrar candidato",
            description =
                    "Cria um usuário candidato com o perfil USER"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Candidato cadastrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation =
                                            RegisterResponseDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Login já cadastrado",
                    content = @Content
            )
    })
    public ResponseEntity<RegisterResponseDTO> register(
            @Valid
            @RequestBody
            RegisterDTO request
    ) {
        String normalizedLogin =
                request.login()
                        .trim()
                        .toLowerCase();

        if (
                userRepository.existsByLogin(
                        normalizedLogin
                )
        ) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .build();
        }

        String encodedPassword =
                passwordEncoder.encode(
                        request.password()
                );

        User user =
                new User(
                        normalizedLogin,
                        encodedPassword,
                        UserRole.USER
                );

        User savedUser =
                userRepository.save(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        RegisterResponseDTO.from(
                                savedUser
                        )
                );
    }
}