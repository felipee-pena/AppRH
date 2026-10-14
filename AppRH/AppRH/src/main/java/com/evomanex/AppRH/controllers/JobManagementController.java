package com.evomanex.AppRH.controllers;

import com.evomanex.AppRH.domain.user.JobRequestDTO;
import com.evomanex.AppRH.domain.user.JobResponseDTO;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.service.JobService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/management/jobs")
public class JobManagementController {

    private final JobService jobService;

    public JobManagementController(
            JobService jobService
    ) {
        this.jobService = jobService;
    }

    @GetMapping
    public List<JobResponseDTO> findManagedJobs(
            @AuthenticationPrincipal
            User authenticatedUser
    ) {
        return jobService.findManagedJobs(
                authenticatedUser
        );
    }

    @PostMapping
    public ResponseEntity<JobResponseDTO> create(
            @Valid
            @RequestBody
            JobRequestDTO request,

            @AuthenticationPrincipal
            User authenticatedUser
    ) {
        JobResponseDTO response =
                jobService.create(
                        request,
                        authenticatedUser
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{code}")
    public ResponseEntity<JobResponseDTO> update(
            @PathVariable long code,

            @Valid
            @RequestBody
            JobRequestDTO request,

            @AuthenticationPrincipal
            User authenticatedUser
    ) {
        JobResponseDTO response =
                jobService.update(
                        code,
                        request,
                        authenticatedUser
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> delete(
            @PathVariable long code,

            @AuthenticationPrincipal
            User authenticatedUser
    ) {
        jobService.delete(
                code,
                authenticatedUser
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}