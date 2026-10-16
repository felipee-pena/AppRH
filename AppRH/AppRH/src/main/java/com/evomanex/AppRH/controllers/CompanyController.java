package com.evomanex.AppRH.controllers;

import com.evomanex.AppRH.config.OpenApiConfig;
import com.evomanex.AppRH.domain.user.CompanyResponseDTO;
import com.evomanex.AppRH.service.CompanyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@Tag(
        name = "Empresas",
        description = "Consulta das empresas disponíveis para associação com vagas"
)
@SecurityRequirement(
        name = OpenApiConfig.SECURITY_SCHEME_NAME
)
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(
            CompanyService companyService
    ) {
        this.companyService =
                companyService;
    }

    @GetMapping
    @Operation(
            summary = "Listar empresas",
            description = "Retorna as empresas disponíveis para criação e atualização de vagas"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Empresas retornadas com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autenticação necessária"
            )
    })
    public List<CompanyResponseDTO> findAll() {
        return companyService.findAll();
    }
}