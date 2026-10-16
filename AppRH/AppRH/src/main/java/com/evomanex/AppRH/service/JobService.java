package com.evomanex.AppRH.service;

import com.evomanex.AppRH.domain.user.JobRequestDTO;
import com.evomanex.AppRH.domain.user.JobResponseDTO;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.domain.user.UserRole;
import com.evomanex.AppRH.model.Company;
import com.evomanex.AppRH.model.Job;
import com.evomanex.AppRH.repository.JobRepository;
import com.evomanex.AppRH.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final CompanyService companyService;

    public JobService(
            JobRepository jobRepository,
            UserRepository userRepository,
            CompanyService companyService
    ) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.companyService = companyService;
    }

    @Transactional(readOnly = true)
    public List<JobResponseDTO> findCatalog() {
        return jobRepository
                .findAllByOrderByCodeDesc()
                .stream()
                .map(JobResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<JobResponseDTO> findManagedJobs(
            User authenticatedPrincipal
    ) {
        User authenticatedUser =
                findAuthenticatedUser(
                        authenticatedPrincipal
                );

        if (
                authenticatedUser.getRole()
                        == UserRole.ADMIN
        ) {
            return jobRepository
                    .findAllByOrderByCodeDesc()
                    .stream()
                    .map(JobResponseDTO::from)
                    .toList();
        }

        if (
                authenticatedUser.getRole()
                        == UserRole.USERRH
        ) {
            return jobRepository
                    .findAllByCreatedByOrderByCodeDesc(
                            authenticatedUser
                    )
                    .stream()
                    .map(JobResponseDTO::from)
                    .toList();
        }

        throw new AccessDeniedException(
                "Usuário não possui permissão "
                        + "para gerenciar vagas"
        );
    }

    @Transactional(readOnly = true)
    public JobResponseDTO findByCode(
            long code
    ) {
        Job job =
                findJobOrThrow(code);

        return JobResponseDTO.from(
                job
        );
    }

    @Transactional
    public JobResponseDTO create(
            JobRequestDTO request,
            User authenticatedPrincipal
    ) {
        User authenticatedUser =
                findAuthenticatedUser(
                        authenticatedPrincipal
                );

        validateManagementRole(
                authenticatedUser
        );

        Company company =
                resolveCompany(
                        request,
                        authenticatedUser
                );

        Job job = new Job();

        applyRequest(
                job,
                request,
                company
        );

        job.setCreatedBy(
                authenticatedUser
        );

        Job savedJob =
                jobRepository.saveAndFlush(
                        job
                );

        return JobResponseDTO.from(
                savedJob
        );
    }

    @Transactional
    public JobResponseDTO update(
            long code,
            JobRequestDTO request,
            User authenticatedPrincipal
    ) {
        User authenticatedUser =
                findAuthenticatedUser(
                        authenticatedPrincipal
                );

        Job job =
                findJobOrThrow(code);

        validateOwnership(
                job,
                authenticatedUser
        );

        Company company =
                resolveCompany(
                        request,
                        authenticatedUser
                );

        applyRequest(
                job,
                request,
                company
        );

        Job updatedJob =
                jobRepository.saveAndFlush(
                        job
                );

        return JobResponseDTO.from(
                updatedJob
        );
    }

    @Transactional
    public void delete(
            long code,
            User authenticatedPrincipal
    ) {
        User authenticatedUser =
                findAuthenticatedUser(
                        authenticatedPrincipal
                );

        Job job =
                findJobOrThrow(code);

        validateOwnership(
                job,
                authenticatedUser
        );

        jobRepository.delete(job);
        jobRepository.flush();
    }

    private User findAuthenticatedUser(
            User authenticatedPrincipal
    ) {
        if (authenticatedPrincipal == null) {
            throw new AccessDeniedException(
                    "Usuário não autenticado"
            );
        }

        return userRepository
                .findUserById(
                        authenticatedPrincipal.getId()
                )
                .orElseThrow(
                        () -> new AccessDeniedException(
                                "Usuário autenticado "
                                        + "não foi encontrado"
                        )
                );
    }

    private Company resolveCompany(
            JobRequestDTO request,
            User authenticatedUser
    ) {
        /*
         * Para USERRH, a empresa é obtida
         * automaticamente do perfil autenticado.
         *
         * Um companyId enviado pelo USERRH
         * será ignorado.
         */
        if (
                authenticatedUser.getRole()
                        == UserRole.USERRH
        ) {
            Company linkedCompany =
                    authenticatedUser.getCompany();

            if (linkedCompany == null) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "O usuário de RH precisa estar "
                                + "vinculado a uma empresa"
                );
            }

            /*
             * Recarrega a empresa na transação atual.
             */
            return companyService
                    .findEntityById(
                            linkedCompany.getId()
                    );
        }

        /*
         * O ADMIN não precisa ter uma empresa
         * vinculada no perfil.
         *
         * Por isso, deve informar companyId ao
         * criar ou atualizar uma vaga.
         */
        if (
                authenticatedUser.getRole()
                        == UserRole.ADMIN
        ) {
            if (request.companyId() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "O administrador deve informar "
                                + "a empresa da vaga"
                );
            }

            return companyService
                    .findEntityById(
                            request.companyId()
                    );
        }

        throw new AccessDeniedException(
                "Usuário não possui permissão "
                        + "para definir a empresa da vaga"
        );
    }

    private void applyRequest(
            Job job,
            JobRequestDTO request,
            Company company
    ) {
        job.setName(
                request.name().trim()
        );

        job.setDescription(
                request.description().trim()
        );

        job.setExpirationDate(
                request.expirationDate()
        );

        job.setSalary(
                request.salary()
        );

        job.setCompany(
                company
        );

        job.setContactEmail(
                normalizeOptionalText(
                        request.contactEmail()
                )
        );
    }

    private String normalizeOptionalText(
            String value
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            return null;
        }

        return value
                .trim()
                .toLowerCase();
    }

    private Job findJobOrThrow(
            long code
    ) {
        Job job =
                jobRepository.findByCode(
                        code
                );

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
        boolean canManage =
                authenticatedUser.getRole()
                        == UserRole.ADMIN
                        || authenticatedUser.getRole()
                        == UserRole.USERRH;

        if (!canManage) {
            throw new AccessDeniedException(
                    "Usuário não possui permissão "
                            + "para criar vagas"
            );
        }
    }

    private void validateOwnership(
            Job job,
            User authenticatedUser
    ) {
        /*
         * ADMIN pode atualizar e excluir
         * qualquer vaga.
         */
        if (
                authenticatedUser.getRole()
                        == UserRole.ADMIN
        ) {
            return;
        }

        /*
         * Perfis diferentes de ADMIN e USERRH
         * não podem gerenciar vagas.
         */
        if (
                authenticatedUser.getRole()
                        != UserRole.USERRH
        ) {
            throw new AccessDeniedException(
                    "Usuário não possui permissão "
                            + "para alterar vagas"
            );
        }

        /*
         * USERRH pode atualizar ou excluir somente
         * vagas criadas pela própria conta.
         */
        boolean isOwner =
                Objects.equals(
                        job.getCreatedBy().getId(),
                        authenticatedUser.getId()
                );

        if (!isOwner) {
            throw new AccessDeniedException(
                    "A vaga pertence a outro "
                            + "usuário de RH"
            );
        }
    }
}