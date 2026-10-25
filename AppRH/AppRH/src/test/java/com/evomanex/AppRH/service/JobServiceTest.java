package com.evomanex.AppRH.service;

import com.evomanex.AppRH.domain.user.JobRequestDTO;
import com.evomanex.AppRH.domain.user.JobResponseDTO;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.domain.user.UserRole;

import com.evomanex.AppRH.model.Company;
import com.evomanex.AppRH.model.Job;

import com.evomanex.AppRH.repository.JobRepository;
import com.evomanex.AppRH.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.test.util.ReflectionTestUtils;

import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import java.time.LocalDate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyService companyService;

    @InjectMocks
    private JobService service;

    private Company company;

    @BeforeEach
    void setUp() {
        company =
                new Company();

        company.setId(
                10L
        );

        company.setName(
                "Empresa Teste"
        );

        company.setNormalizedName(
                "empresa teste"
        );
    }

    @Test
    void shouldReturnCatalogOrderedByCode() {
        Job firstJob =
                createJob(
                        20L,
                        "Vaga mais recente"
                );

        Job secondJob =
                createJob(
                        10L,
                        "Vaga anterior"
                );

        when(
                jobRepository.findAllByOrderByCodeDesc()
        ).thenReturn(
                List.of(
                        firstJob,
                        secondJob
                )
        );

        List<JobResponseDTO> result =
                service.findCatalog();

        assertThat(
                result
        ).hasSize(
                2
        );

        assertThat(
                result
        )
                .extracting(
                        JobResponseDTO::code
                )
                .containsExactly(
                        20L,
                        10L
                );
    }

    @Test
    void shouldReturnJobByCode() {
        Job job =
                createJob(
                        10L,
                        "Analista Java"
                );

        when(
                jobRepository.findByCode(
                        10L
                )
        ).thenReturn(
                job
        );

        JobResponseDTO result =
                service.findByCode(
                        10L
                );

        assertThat(
                result.code()
        ).isEqualTo(
                10L
        );

        assertThat(
                result.name()
        ).isEqualTo(
                "Analista Java"
        );
    }

    @Test
    void shouldReturnNotFoundWhenJobDoesNotExist() {
        when(
                jobRepository.findByCode(
                        99L
                )
        ).thenReturn(
                null
        );

        assertThatThrownBy(
                () ->
                        service.findByCode(
                                99L
                        )
        )
                .isInstanceOf(
                        ResponseStatusException.class
                )
                .satisfies(
                        exception -> {
                            ResponseStatusException responseException =
                                    (ResponseStatusException) exception;

                            assertThat(
                                    responseException.getStatusCode()
                            ).isEqualTo(
                                    HttpStatus.NOT_FOUND
                            );

                            assertThat(
                                    responseException.getReason()
                            ).isEqualTo(
                                    "Vaga não encontrada"
                            );
                        }
                );
    }

    @Test
    void shouldReturnAllManagedJobsForAdmin() {
        User admin =
                createUser(
                        "admin-id",
                        "admin@example.com",
                        UserRole.ADMIN
                );

        Job job =
                createJob(
                        10L,
                        "Analista Java"
                );

        when(
                userRepository.findUserById(
                        "admin-id"
                )
        ).thenReturn(
                Optional.of(
                        admin
                )
        );

        when(
                jobRepository.findAllByOrderByCodeDesc()
        ).thenReturn(
                List.of(
                        job
                )
        );

        List<JobResponseDTO> result =
                service.findManagedJobs(
                        admin
                );

        assertThat(
                result
        ).hasSize(
                1
        );

        verify(
                jobRepository
        ).findAllByOrderByCodeDesc();
    }

    @Test
    void shouldReturnOnlyOwnedJobsForRh() {
        User rhUser =
                createUser(
                        "rh-id",
                        "rh@example.com",
                        UserRole.USERRH
                );

        when(
                userRepository.findUserById(
                        "rh-id"
                )
        ).thenReturn(
                Optional.of(
                        rhUser
                )
        );

        when(
                jobRepository.findAllByCreatedByOrderByCodeDesc(
                        rhUser
                )
        ).thenReturn(
                List.of(
                        createJob(
                                10L,
                                "Vaga do RH"
                        )
                )
        );

        List<JobResponseDTO> result =
                service.findManagedJobs(
                        rhUser
                );

        assertThat(
                result
        ).hasSize(
                1
        );

        verify(
                jobRepository
        ).findAllByCreatedByOrderByCodeDesc(
                rhUser
        );
    }

    @Test
    void shouldRejectManagedJobsForCandidate() {
        User candidate =
                createUser(
                        "candidate-id",
                        "candidate@example.com",
                        UserRole.USER
                );

        when(
                userRepository.findUserById(
                        "candidate-id"
                )
        ).thenReturn(
                Optional.of(
                        candidate
                )
        );

        assertThatThrownBy(
                () ->
                        service.findManagedJobs(
                                candidate
                        )
        )
                .isInstanceOf(
                        AccessDeniedException.class
                )
                .hasMessageContaining(
                        "não possui permissão"
                );
    }

    @Test
    void shouldCreateJobForAdmin() {
        User admin =
                createUser(
                        "admin-id",
                        "admin@example.com",
                        UserRole.ADMIN
                );

        JobRequestDTO request =
                createRequest(
                        10L,
                        "  Analista Java  ",
                        "  Desenvolvimento backend  ",
                        "  VAGAS@EXAMPLE.COM  "
                );

        when(
                userRepository.findUserById(
                        "admin-id"
                )
        ).thenReturn(
                Optional.of(
                        admin
                )
        );

        when(
                companyService.findEntityById(
                        10L
                )
        ).thenReturn(
                company
        );

        when(
                jobRepository.saveAndFlush(
                        any(Job.class)
                )
        ).thenAnswer(
                invocation -> {
                    Job job =
                            invocation.getArgument(0);

                    job.setCode(
                            100L
                    );

                    return job;
                }
        );

        JobResponseDTO result =
                service.create(
                        request,
                        admin
                );

        ArgumentCaptor<Job> jobCaptor =
                ArgumentCaptor.forClass(
                        Job.class
                );

        verify(
                jobRepository
        ).saveAndFlush(
                jobCaptor.capture()
        );

        Job savedJob =
                jobCaptor.getValue();

        assertThat(
                savedJob.getName()
        ).isEqualTo(
                "Analista Java"
        );

        assertThat(
                savedJob.getDescription()
        ).isEqualTo(
                "Desenvolvimento backend"
        );

        assertThat(
                savedJob.getContactEmail()
        ).isEqualTo(
                "vagas@example.com"
        );

        assertThat(
                savedJob.getCompany()
        ).isSameAs(
                company
        );

        assertThat(
                savedJob.getCreatedBy()
        ).isSameAs(
                admin
        );

        assertThat(
                result.code()
        ).isEqualTo(
                100L
        );
    }

    @Test
    void shouldRejectAdminCreationWithoutCompanyId() {
        User admin =
                createUser(
                        "admin-id",
                        "admin@example.com",
                        UserRole.ADMIN
                );

        JobRequestDTO request =
                createRequest(
                        null,
                        "Analista",
                        "Descrição",
                        null
                );

        when(
                userRepository.findUserById(
                        "admin-id"
                )
        ).thenReturn(
                Optional.of(
                        admin
                )
        );

        assertThatThrownBy(
                () ->
                        service.create(
                                request,
                                admin
                        )
        )
                .isInstanceOf(
                        ResponseStatusException.class
                )
                .hasMessageContaining(
                        "administrador deve informar"
                );

        verify(
                jobRepository,
                never()
        ).saveAndFlush(
                any(Job.class)
        );
    }

    @Test
    void shouldCreateJobForRhUsingLinkedCompany() {
        User rhUser =
                createUser(
                        "rh-id",
                        "rh@example.com",
                        UserRole.USERRH
                );

        rhUser.setCompany(
                company
        );

        JobRequestDTO request =
                createRequest(
                        999L,
                        "Analista",
                        "Descrição",
                        ""
                );

        when(
                userRepository.findUserById(
                        "rh-id"
                )
        ).thenReturn(
                Optional.of(
                        rhUser
                )
        );

        when(
                companyService.findEntityById(
                        10L
                )
        ).thenReturn(
                company
        );

        when(
                jobRepository.saveAndFlush(
                        any(Job.class)
                )
        ).thenAnswer(
                invocation -> {
                    Job job =
                            invocation.getArgument(0);

                    job.setCode(
                            200L
                    );

                    return job;
                }
        );

        service.create(
                request,
                rhUser
        );

        ArgumentCaptor<Job> jobCaptor =
                ArgumentCaptor.forClass(
                        Job.class
                );

        verify(
                jobRepository
        ).saveAndFlush(
                jobCaptor.capture()
        );

        assertThat(
                jobCaptor
                        .getValue()
                        .getCompany()
        ).isSameAs(
                company
        );

        assertThat(
                jobCaptor
                        .getValue()
                        .getContactEmail()
        ).isNull();

        verify(
                companyService
        ).findEntityById(
                10L
        );

        verify(
                companyService,
                never()
        ).findEntityById(
                999L
        );
    }

    @Test
    void shouldRejectRhCreationWithoutLinkedCompany() {
        User rhUser =
                createUser(
                        "rh-id",
                        "rh@example.com",
                        UserRole.USERRH
                );

        when(
                userRepository.findUserById(
                        "rh-id"
                )
        ).thenReturn(
                Optional.of(
                        rhUser
                )
        );

        assertThatThrownBy(
                () ->
                        service.create(
                                createRequest(
                                        null,
                                        "Analista",
                                        "Descrição",
                                        null
                                ),
                                rhUser
                        )
        )
                .isInstanceOf(
                        ResponseStatusException.class
                )
                .hasMessageContaining(
                        "vinculado a uma empresa"
                );
    }

    @Test
    void shouldDeleteJobAsAdmin() {
        User admin =
                createUser(
                        "admin-id",
                        "admin@example.com",
                        UserRole.ADMIN
                );

        Job job =
                createJob(
                        10L,
                        "Vaga"
                );

        when(
                userRepository.findUserById(
                        "admin-id"
                )
        ).thenReturn(
                Optional.of(
                        admin
                )
        );

        when(
                jobRepository.findByCode(
                        10L
                )
        ).thenReturn(
                job
        );

        service.delete(
                10L,
                admin
        );

        verify(
                jobRepository
        ).delete(
                job
        );

        verify(
                jobRepository
        ).flush();
    }

    @Test
    void shouldRejectRhDeletingAnotherUsersJob() {
        User owner =
                createUser(
                        "owner-id",
                        "owner@example.com",
                        UserRole.USERRH
                );

        User anotherRh =
                createUser(
                        "another-id",
                        "another@example.com",
                        UserRole.USERRH
                );

        Job job =
                createJob(
                        10L,
                        "Vaga"
                );

        job.setCreatedBy(
                owner
        );

        when(
                userRepository.findUserById(
                        "another-id"
                )
        ).thenReturn(
                Optional.of(
                        anotherRh
                )
        );

        when(
                jobRepository.findByCode(
                        10L
                )
        ).thenReturn(
                job
        );

        assertThatThrownBy(
                () ->
                        service.delete(
                                10L,
                                anotherRh
                        )
        )
                .isInstanceOf(
                        AccessDeniedException.class
                )
                .hasMessageContaining(
                        "outro usuário de RH"
                );

        verify(
                jobRepository,
                never()
        ).delete(
                job
        );
    }

    @Test
    void shouldRejectNullAuthenticatedUser() {
        assertThatThrownBy(
                () ->
                        service.findManagedJobs(
                                null
                        )
        )
                .isInstanceOf(
                        AccessDeniedException.class
                )
                .hasMessage(
                        "Usuário não autenticado"
                );
    }

    @Test
    void shouldRejectAuthenticatedUserNotFoundInDatabase() {
        User principal =
                createUser(
                        "missing-id",
                        "missing@example.com",
                        UserRole.USER
                );

        when(
                userRepository.findUserById(
                        "missing-id"
                )
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(
                () ->
                        service.findManagedJobs(
                                principal
                        )
        )
                .isInstanceOf(
                        AccessDeniedException.class
                )
                .hasMessageContaining(
                        "não foi encontrado"
                );
    }

    private Job createJob(
            long code,
            String name
    ) {
        Job job =
                new Job();

        job.setCode(
                code
        );

        job.setName(
                name
        );

        job.setDescription(
                "Descrição da vaga"
        );

        job.setExpirationDate(
                LocalDate
                        .now()
                        .plusDays(
                                30
                        )
        );

        job.setSalary(
                new BigDecimal(
                        "9000.00"
                )
        );

        job.setContactEmail(
                "vagas@example.com"
        );

        job.setCompany(
                company
        );

        return job;
    }

    private static User createUser(
            String id,
            String login,
            UserRole role
    ) {
        User user =
                new User(
                        login,
                        "$2a$10$encoded",
                        role
                );

        ReflectionTestUtils.setField(
                user,
                "id",
                id
        );

        return user;
    }

    private static JobRequestDTO createRequest(
            Long companyId,
            String name,
            String description,
            String contactEmail
    ) {
        return new JobRequestDTO(
                name,
                description,
                LocalDate
                        .now()
                        .plusDays(
                                30
                        ),
                new BigDecimal(
                        "9000.00"
                ),
                companyId,
                contactEmail
        );
    }
}