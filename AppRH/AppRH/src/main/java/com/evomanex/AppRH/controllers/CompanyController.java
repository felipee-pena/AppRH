package com.evomanex.AppRH.controllers;

import com.evomanex.AppRH.domain.user.CompanyResponseDTO;
import com.evomanex.AppRH.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies")
@Tag(name = "Empresas", description = "Consulta pública das empresas disponíveis no AppRH")
@SecurityRequirements
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    @Operation(
            summary = "Listar empresas",
            description = """
        Retorna as empresas disponíveis para:

        - cadastro público de profissionais de RH;
        - criação de vagas por administradores;
        - consultas da plataforma.
        """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresas retornadas com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro interno ao consultar empresas", content = @Content)
    })
    public ResponseEntity<List<CompanyResponseDTO>> findAll() {
        List<CompanyResponseDTO> response = companyService.findAll();

        return ResponseEntity.ok(response);
    }
}
