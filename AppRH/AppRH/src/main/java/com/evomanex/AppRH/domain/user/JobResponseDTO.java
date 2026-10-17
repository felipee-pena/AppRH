package com.evomanex.AppRH.domain.user;

import com.evomanex.AppRH.model.Job;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Schema(description = "Dados retornados de uma vaga")
public record JobResponseDTO(
        @Schema(description = "Código da vaga", example = "29") long code,

        @Schema(description = "Nome da vaga", example = "Analista de Sistemas Pleno") String name,

        @Schema(description = "Descrição da vaga", example = "Desenvolvimento de aplicações corporativas")
        String description,

        @Schema(description = "Data de expiração", example = "2027-12-31") LocalDate expirationDate,

        @Schema(description = "Data e hora de criação da vaga", example = "2026-10-15T09:00:00-03:00")
        OffsetDateTime createdAt,

        @Schema(description = "Salário", example = "12990.00") BigDecimal salary,

        @Schema(description = "E-mail opcional para contato", example = "vagas@empresa.com.br", nullable = true)
        String contactEmail,

        @Schema(description = "Empresa responsável pela oportunidade") CompanyResponseDTO company
) {
    public static JobResponseDTO from(Job job) {
        return new JobResponseDTO(
                job.getCode(),
                job.getName(),
                job.getDescription(),
                job.getExpirationDate(),
                job.getCreatedAt(),
                job.getSalary(),
                job.getContactEmail(),
                CompanyResponseDTO.from(job.getCompany())
        );
    }
}
