package com.evomanex.AppRH.controllers;

import com.evomanex.AppRH.domain.user.JobResponseDTO;
import com.evomanex.AppRH.service.JobService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@Tag(
        name = "Vagas",
        description = "Consulta do catálogo de vagas"
)
public class JobRestController {

    private final JobService jobService;

    public JobRestController(
            JobService jobService
    ) {
        this.jobService = jobService;
    }

    @GetMapping
    @Operation(
            summary = "Listar vagas",
            description = "Retorna todas as vagas disponíveis"
    )
    public List<JobResponseDTO> findAll() {
        return jobService.findCatalog();
    }

    @GetMapping("/{code}")
    @Operation(
            summary = "Consultar vaga",
            description = "Retorna uma vaga pelo código"
    )
    public JobResponseDTO findByCode(
            @PathVariable long code
    ) {
        return jobService.findByCode(code);
    }
}