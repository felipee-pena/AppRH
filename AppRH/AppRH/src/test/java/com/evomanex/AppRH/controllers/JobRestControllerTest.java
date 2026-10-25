package com.evomanex.AppRH.controllers;

import com.evomanex.AppRH.domain.user.JobResponseDTO;
import com.evomanex.AppRH.service.JobService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobRestControllerTest {

    @Mock
    private JobService jobService;

    private JobRestController controller;

    @BeforeEach
    void setUp() {
        controller =
                new JobRestController(
                        jobService
                );
    }

    @Test
    void shouldReturnJobCatalog() {
        JobResponseDTO firstJob =
                mock(
                        JobResponseDTO.class
                );

        JobResponseDTO secondJob =
                mock(
                        JobResponseDTO.class
                );

        when(
                jobService.findCatalog()
        ).thenReturn(
                List.of(
                        firstJob,
                        secondJob
                )
        );

        List<JobResponseDTO> response =
                controller.findAll();

        assertThat(
                response
        ).containsExactly(
                firstJob,
                secondJob
        );

        verify(
                jobService
        ).findCatalog();
    }

    @Test
    void shouldReturnEmptyCatalog() {
        when(
                jobService.findCatalog()
        ).thenReturn(
                List.of()
        );

        List<JobResponseDTO> response =
                controller.findAll();

        assertThat(
                response
        ).isEmpty();

        verify(
                jobService
        ).findCatalog();
    }

    @Test
    void shouldReturnJobByCode() {
        JobResponseDTO serviceResponse =
                mock(
                        JobResponseDTO.class
                );

        when(
                jobService.findByCode(
                        10L
                )
        ).thenReturn(
                serviceResponse
        );

        JobResponseDTO response =
                controller.findByCode(
                        10L
                );

        assertThat(
                response
        ).isSameAs(
                serviceResponse
        );

        verify(
                jobService
        ).findByCode(
                10L
        );
    }
}