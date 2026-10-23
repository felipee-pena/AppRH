package com.evomanex.AppRH.domain.user;

import com.evomanex.AppRH.model.Company;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Empresa disponível para associação com vagas"
)
public record CompanyResponseDTO(

        @Schema(
                description = "Identificador da empresa",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Nome da empresa",
                example = "Nexa Sistemas"
        )
        String name

) {

    public static CompanyResponseDTO from(
            Company company
    ) {
        return new CompanyResponseDTO(
                company.getId(),
                company.getName()
        );
    }
}