package com.evomanex.AppRH.service;

import com.evomanex.AppRH.domain.user.JobRequestDTO;
import com.evomanex.AppRH.domain.user.JobResponseDTO;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.domain.user.UserRole;
import com.evomanex.AppRH.model.Job;
import com.evomanex.AppRH.repository.JobRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(
            JobRepository jobRepository
    ) {
        this.jobRepository = jobRepository;
    }

    public List<JobResponseDTO> findCatalog() {
        return StreamSupport
                .stream(
                        jobRepository
                                .findAll()
                                .spliterator(),
                        false
                )
                .map(JobResponseDTO::from)
                .toList();
    }

    public List<JobResponseDTO> findManagedJobs(
            User authenticatedUser
    ) {
        if (authenticatedUser == null) {
            throw new AccessDeniedException(
                    "Usuário não autenticado"
            );
        }

        if (authenticatedUser.getRole() == UserRole.ADMIN) {
            return StreamSupport
                    .stream(
                            jobRepository
                                    .findAll()
                                    .spliterator(),
                            false
                    )
                    .map(JobResponseDTO::from)
                    .toList();
        }

        if (authenticatedUser.getRole() == UserRole.USERRH) {
            return jobRepository
                    .findAllByCreatedBy(authenticatedUser)
                    .stream()
                    .map(JobResponseDTO::from)
                    .toList();
        }

        throw new AccessDeniedException(
                "Usuário não possui permissão para gerenciar vagas"
        );
    }

    public JobResponseDTO findByCode(long code) {
        Job job = findJobOrThrow(code);

        return JobResponseDTO.from(job);
    }

    public JobResponseDTO create(
            JobRequestDTO request,
            User authenticatedUser
    ) {
        validateManagementRole(authenticatedUser);

        Job job = new Job();

        job.setName(request.name());
        job.setDescription(request.description());
        job.setDate(request.date());
        job.setSalary(request.salary());
        job.setCreatedBy(authenticatedUser);

        Job savedJob = jobRepository.save(job);

        return JobResponseDTO.from(savedJob);
    }

    public JobResponseDTO update(
            long code,
            JobRequestDTO request,
            User authenticatedUser
    ) {
        Job job = findJobOrThrow(code);

        validateOwnership(
                job,
                authenticatedUser
        );

        job.setName(request.name());
        job.setDescription(request.description());
        job.setDate(request.date());
        job.setSalary(request.salary());

        Job updatedJob = jobRepository.save(job);

        return JobResponseDTO.from(updatedJob);
    }

    public void delete(
            long code,
            User authenticatedUser
    ) {
        Job job = findJobOrThrow(code);

        validateOwnership(
                job,
                authenticatedUser
        );

        jobRepository.delete(job);
    }

    private Job findJobOrThrow(long code) {
        Job job = jobRepository.findByCode(code);

        if (job == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Vaga não encontrada"
            );
        }

        return job;
    }

    private void validateManagementRole(
            User authenticatedUser
    ) {
        if (authenticatedUser == null) {
            throw new AccessDeniedException(
                    "Usuário não autenticado"
            );
        }

        boolean canManage =
                authenticatedUser.getRole() == UserRole.ADMIN
                        || authenticatedUser.getRole() == UserRole.USERRH;

        if (!canManage) {
            throw new AccessDeniedException(
                    "Usuário não possui permissão para criar vagas"
            );
        }
    }

    private void validateOwnership(
            Job job,
            User authenticatedUser
    ) {
        if (authenticatedUser == null) {
            throw new AccessDeniedException(
                    "Usuário não autenticado"
            );
        }

        if (authenticatedUser.getRole() == UserRole.ADMIN) {
            return;
        }

        if (authenticatedUser.getRole() != UserRole.USERRH) {
            throw new AccessDeniedException(
                    "Usuário não possui permissão para alterar vagas"
            );
        }

        boolean isOwner = Objects.equals(
                job.getCreatedBy().getId(),
                authenticatedUser.getId()
        );

        if (!isOwner) {
            throw new AccessDeniedException(
                    "A vaga pertence a outro usuário de RH"
            );
        }
    }
}