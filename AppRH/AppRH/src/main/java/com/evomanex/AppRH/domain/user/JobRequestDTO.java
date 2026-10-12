package com.evomanex.AppRH.domain.user;

import jakarta.validation.constraints.NotBlank;

public record JobRequestDTO(

        @NotBlank(message = "Informe o nome da vaga")
        String name,

        @NotBlank(message = "Informe a descrição")
        String description,

        @NotBlank(message = "Informe a data")
        String date,

        @NotBlank(message = "Informe o salário")
        String salary

)
{}