package com.evomanex.AppRH.domain.user;

import com.evomanex.AppRH.model.Company;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Empresa associada ao usuário de RH"
)
public record RhCompanyResponseDTO(

        @Schema(
                description = "Identificador da empresa",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Nome da empresa",
                example = "Claro"
        )
        String name,

        @Schema(
                description = "Indica se a empresa foi criada nesta operação",
                example = "true"
        )
        boolean created

) {

    public static RhCompanyResponseDTO from(
            Company company,
            boolean created
    ) {
        return new RhCompanyResponseDTO(
                company.getId(),
                company.getName(),
                created
        );
    }
}