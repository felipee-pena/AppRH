package com.evomanex.AppRH.controllers;

import com.evomanex.AppRH.domain.user.JobRequestDTO;
import com.evomanex.AppRH.domain.user.JobResponseDTO;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.domain.user.UserRole;
import com.evomanex.AppRH.service.JobService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

import java.time.LocalDate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobManagementControllerTest {

    @Mock
    private JobService jobService;

    private JobManagementController controller;

    private User rhUser;

    @BeforeEach
    void setUp() {
        controller =
                new JobManagementController(
                        jobService
                );

        rhUser =
                new User(
                        "rh@example.com",
                        "$2a$10$encoded",
                        UserRole.USERRH
                );
    }

    @Test
    void shouldReturnManagedJobs() {
        when(
                jobService.findManagedJobs(
                        rhUser
                )
        ).thenReturn(
                List.of()
        );

        List<JobResponseDTO> response =
                controller.findManagedJobs(
                        rhUser
                );

        assertThat(
                response
        ).isEmpty();

        verify(
                jobService
        ).findManagedJobs(
                rhUser
        );
    }

    @Test
    void shouldCreateJob() {
        JobRequestDTO request =
                createRequest();

        JobResponseDTO serviceResponse =
                org.mockito.Mockito.mock(
                        JobResponseDTO.class
                );

        when(
                jobService.create(
                        request,
                        rhUser
                )
        ).thenReturn(
                serviceResponse
        );

        var response =
                controller.create(
                        request,
                        rhUser
                );

        assertThat(
                response.getStatusCode()
        ).isEqualTo(
                HttpStatus.CREATED
        );

        assertThat(
                response.getBody()
        ).isSameAs(
                serviceResponse
        );

        verify(
                jobService
        ).create(
                request,
                rhUser
        );
    }

    @Test
    void shouldUpdateJob() {
        JobRequestDTO request =
                createRequest();

        JobResponseDTO serviceResponse =
                org.mockito.Mockito.mock(
                        JobResponseDTO.class
                );

        when(
                jobService.update(
                        10L,
                        request,
                        rhUser
                )
        ).thenReturn(
                serviceResponse
        );

        var response =
                controller.update(
                        10L,
                        request,
                        rhUser
                );

        assertThat(
                response.getStatusCode()
        ).isEqualTo(
                HttpStatus.OK
        );

        assertThat(
                response.getBody()
        ).isSameAs(
                serviceResponse
        );

        verify(
                jobService
        ).update(
                10L,
                request,
                rhUser
        );
    }

    @Test
    void shouldDeleteJob() {
        doNothing()
                .when(
                        jobService
                )
                .delete(
                        10L,
                        rhUser
                );

        var response =
                controller.delete(
                        10L,
                        rhUser
                );

        assertThat(
                response.getStatusCode()
        ).isEqualTo(
                HttpStatus.NO_CONTENT
        );

        assertThat(
                response.getBody()
        ).isNull();

        verify(
                jobService
        ).delete(
                10L,
                rhUser
        );
    }

    private static JobRequestDTO createRequest() {
        return new JobRequestDTO(
                "Analista de Sistemas",
                "Desenvolvimento de aplicações",
                LocalDate.now().plusDays(30),
                new BigDecimal("9000.00"),
                null,
                "vagas@example.com"
        );
    }
}