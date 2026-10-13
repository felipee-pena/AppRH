package com.evomanex.AppRH.domain.user;

import com.evomanex.AppRH.model.Job;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados retornados de uma vaga")
public record JobResponseDTO(

        @Schema(
                description = "Código da vaga",
                example = "1"
        )
        long code,

        @Schema(
                description = "Nome da vaga",
                example = "Analista de Sistemas"
        )
        String name,

        @Schema(
                description = "Descrição da vaga",
                example = "Desenvolvimento de aplicações corporativas"
        )
        String description,

        @Schema(
                description = "Data de expiração",
                example = "2026-12-31"
        )
        String date,

        @Schema(
                description = "Salário",
                example = "5500.00"
        )
        String salary
) {

    public static JobResponseDTO from(Job job) {
        return new JobResponseDTO(
                job.getCode(),
                job.getName(),
                job.getDescription(),
                job.getDate(),
                job.getSalary()
        );
    }
}