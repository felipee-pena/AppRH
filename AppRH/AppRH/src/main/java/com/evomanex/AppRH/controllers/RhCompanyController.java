package com.evomanex.AppRH.controllers;

import com.evomanex.AppRH.config.OpenApiConfig;
import com.evomanex.AppRH.domain.user.CompanyAssociationRequestDTO;
import com.evomanex.AppRH.domain.user.RhCompanyResponseDTO;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.service.RhCompanyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rh/company")
@Tag(
        name = "Empresa do RH",
        description = "Consulta e vinculação da empresa do usuário de RH"
)
@SecurityRequirement(
        name = OpenApiConfig.SECURITY_SCHEME_NAME
)
public class RhCompanyController {

    private final RhCompanyService rhCompanyService;

    public RhCompanyController(
            RhCompanyService rhCompanyService
    ) {
        this.rhCompanyService =
                rhCompanyService;
    }

    @GetMapping
    @Operation(
            summary = "Consultar empresa do RH",
            description = "Retorna a empresa associada ao usuário de RH autenticado"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Empresa retornada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation =
                                            RhCompanyResponseDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autenticação necessária",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Perfil sem permissão",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário de RH sem empresa",
                    content = @Content
            )
    })
    public ResponseEntity<RhCompanyResponseDTO>
    findCurrentCompany(
            @AuthenticationPrincipal
            User authenticatedUser
    ) {
        RhCompanyResponseDTO response =
                rhCompanyService.findCurrentCompany(
                        authenticatedUser
                );

        return ResponseEntity.ok(
                response
        );
    }

    @PostMapping
    @Operation(
            summary = "Vincular empresa ao RH",
            description = "Localiza uma empresa pelo nome normalizado. "
                    + "Se a empresa existir, vincula ao usuário de RH. "
                    + "Se não existir, cria a empresa e realiza o vínculo."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Empresa existente vinculada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation =
                                            RhCompanyResponseDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "201",
                    description = "Empresa criada e vinculada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation =
                                            RhCompanyResponseDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autenticação necessária",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Perfil sem permissão",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Usuário já possui empresa",
                    content = @Content
            )
    })
    public ResponseEntity<RhCompanyResponseDTO>
    associateCompany(
            @Valid
            @RequestBody
            CompanyAssociationRequestDTO request,

            @AuthenticationPrincipal
            User authenticatedUser
    ) {
        RhCompanyResponseDTO response =
                rhCompanyService.associateCompany(
                        request,
                        authenticatedUser
                );

        HttpStatus status =
                response.created()
                        ? HttpStatus.CREATED
                        : HttpStatus.OK;

        return ResponseEntity
                .status(status)
                .body(response);
    }
}