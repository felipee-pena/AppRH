package com.evomanex.AppRH.controllers;

import com.evomanex.AppRH.domain.user.AuthenticationDTO;
import com.evomanex.AppRH.domain.user.LoginResponseDTO;
import com.evomanex.AppRH.domain.user.RegisterDTO;
import com.evomanex.AppRH.domain.user.RegisterResponseDTO;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.infra.security.TokenService;
import com.evomanex.AppRH.service.PublicRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Cadastro público e autenticação de usuários")
@SecurityRequirements
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final PublicRegistrationService publicRegistrationService;

    public AuthenticationController(
            AuthenticationManager authenticationManager,
            TokenService tokenService,
            PublicRegistrationService publicRegistrationService
    ) {
        this.authenticationManager = authenticationManager;

        this.tokenService = tokenService;

        this.publicRegistrationService = publicRegistrationService;
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Valida login e senha e retorna um token JWT")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticação realizada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponseDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Login ou senha inválidos", content = @Content)
    })
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody AuthenticationDTO request) {
        String normalizedLogin = request.login().trim().toLowerCase(Locale.ROOT);

        var authenticationToken = new UsernamePasswordAuthenticationToken(normalizedLogin, request.password());

        var authentication = authenticationManager.authenticate(authenticationToken);

        User authenticatedUser = (User) authentication.getPrincipal();

        String accessToken = tokenService.generateToken(authenticatedUser);

        LoginResponseDTO response = new LoginResponseDTO(accessToken, "Bearer", TokenService.EXPIRATION_SECONDS);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(
            summary = "Cadastrar usuário",
            description = """
        Cria um candidato USER ou profissional USERRH.

        USER não deve informar empresa.

        USERRH deve selecionar uma empresa existente
        por companyId ou informar newCompanyName.

        O cadastro público nunca cria ADMIN.
        """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuário cadastrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterResponseDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Empresa não encontrada", content = @Content),
            @ApiResponse(responseCode = "409", description = "Login ou empresa já cadastrados", content = @Content)
    })
    public ResponseEntity<RegisterResponseDTO> register(@Valid @RequestBody RegisterDTO request) {
        RegisterResponseDTO response = publicRegistrationService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
