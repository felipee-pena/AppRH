package com.evomanex.AppRH.controllers;

import com.evomanex.AppRH.domain.user.JobRequestDTO;
import com.evomanex.AppRH.domain.user.JobResponseDTO;
import com.evomanex.AppRH.model.Job;
import com.evomanex.AppRH.repository.JobRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.MediaType;
import io.swagger.v3.oas.annotations.media.Content;

import java.util.List;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping( value = "/api/jobs", produces = MediaType.APPLICATION_JSON_VALUE )
@Tag(
        name = "Vagas",
        description = "Operações para gerenciamento de vagas"
)
@CrossOrigin(origins = "http://localhost:5173")
public class JobRestController {

    private final JobRepository jobRepository;

    public JobRestController(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @GetMapping
    @Operation(
            summary = "Listar vagas",
            description = "Retorna todas as vagas cadastradas"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Vagas retornadas com sucesso"
    )
    public List<JobResponseDTO> findAll() {
        return StreamSupport
                .stream(jobRepository.findAll().spliterator(), false)
                .map(JobResponseDTO::from)
                .toList();
    }

    @GetMapping("/{code}")
    @Operation(
            summary = "Buscar vaga por código",
            description = "Retorna uma vaga pelo código informado"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Vaga encontrada"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Vaga não encontrada",
                    content = @Content
            )
    })
    public ResponseEntity<JobResponseDTO> findByCode(
            @Parameter(
                    description = "Código identificador da vaga",
                    example = "1",
                    required = true
            )
            @PathVariable long code
    ) {
        Job job = jobRepository.findByCode(code);

        if (job == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                JobResponseDTO.from(job)
        );
    }

    @PostMapping( consumes = MediaType.APPLICATION_JSON_VALUE )
    @Operation(
            summary = "Cadastrar vaga",
            description = "Cadastra uma nova vaga"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Vaga cadastrada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados da vaga inválidos",
                    content = @Content
            )
    })
    public ResponseEntity<JobResponseDTO> create(
            @Valid @RequestBody JobRequestDTO request) {

        Job job = new Job();

        job.setName(request.name());
        job.setDescription(request.description());
        job.setDate(request.date());
        job.setSalary(request.salary());

        Job savedJob = jobRepository.save(job);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(JobResponseDTO.from(savedJob));
    }

    @PutMapping( value = "/{code}", consumes = MediaType.APPLICATION_JSON_VALUE )
    @Operation(
            summary = "Atualizar vaga",
            description = "Atualiza os dados de uma vaga existente"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Vaga atualizada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados da vaga inválidos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Vaga não encontrada",
                    content = @Content
            )
    })
    public ResponseEntity<JobResponseDTO> update(
            @Parameter(
                    description = "Código identificador da vaga que será atualizada",
                    example = "1",
                    required = true
            )
            @PathVariable long code,

            @Valid
            @RequestBody JobRequestDTO request
    ) {
        Job job = jobRepository.findByCode(code);

        if (job == null) {
            return ResponseEntity.notFound().build();
        }

        job.setName(request.name());
        job.setDescription(request.description());
        job.setDate(request.date());
        job.setSalary(request.salary());

        Job updatedJob = jobRepository.save(job);

        return ResponseEntity.ok(
                JobResponseDTO.from(updatedJob)
        );
    }

    @DeleteMapping("/{code}")
    @Operation(
            summary = "Excluir vaga",
            description = "Exclui uma vaga pelo código informado"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Vaga excluída com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Vaga não encontrada",
                    content = @Content
            )
    })
    public ResponseEntity<Void> delete(
            @Parameter(
                    description = "Código identificador da vaga que será excluída",
                    example = "1",
                    required = true
            )
            @PathVariable long code
    ) {
        Job job = jobRepository.findByCode(code);

        if (job == null) {
            return ResponseEntity.notFound().build();
        }

        jobRepository.delete(job);

        return ResponseEntity.noContent().build();
    }
}