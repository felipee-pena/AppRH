package com.evomanex.AppRH.controllers;

import com.evomanex.AppRH.domain.user.JobRequestDTO;
import com.evomanex.AppRH.domain.user.JobResponseDTO;
import com.evomanex.AppRH.model.Job;
import com.evomanex.AppRH.repository.JobRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "http://localhost:8081")
public class JobRestController {

    private final JobRepository jobRepository;

    public JobRestController(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @GetMapping
    public List<JobResponseDTO> findAll() {
        return StreamSupport
                .stream(jobRepository.findAll().spliterator(), false)
                .map(JobResponseDTO::from)
                .toList();
    }

    @GetMapping("/{code}")
    public ResponseEntity<JobResponseDTO> findByCode(
            @PathVariable long code) {

        Job job = jobRepository.findByCode(code);

        if (job == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                JobResponseDTO.from(job)
        );
    }

    @PostMapping
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

    @PutMapping("/{code}")
    public ResponseEntity<JobResponseDTO> update(
            @PathVariable long code,
            @Valid @RequestBody JobRequestDTO request) {

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
    public ResponseEntity<Void> delete(
            @PathVariable long code) {

        Job job = jobRepository.findByCode(code);

        if (job == null) {
            return ResponseEntity.notFound().build();
        }

        jobRepository.delete(job);

        return ResponseEntity.noContent().build();
    }
}