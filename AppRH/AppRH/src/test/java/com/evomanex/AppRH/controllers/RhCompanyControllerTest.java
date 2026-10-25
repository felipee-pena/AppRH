package com.evomanex.AppRH.controllers;

import com.evomanex.AppRH.domain.user.CompanyAssociationRequestDTO;
import com.evomanex.AppRH.domain.user.RhCompanyResponseDTO;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.domain.user.UserRole;
import com.evomanex.AppRH.service.RhCompanyService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RhCompanyControllerTest {

    @Mock
    private RhCompanyService rhCompanyService;

    private RhCompanyController controller;

    private User rhUser;

    @BeforeEach
    void setUp() {
        controller =
                new RhCompanyController(
                        rhCompanyService
                );

        rhUser =
                new User(
                        "rh@example.com",
                        "$2a$10$encoded",
                        UserRole.USERRH
                );
    }

    @Test
    void shouldReturnCurrentCompany() {
        RhCompanyResponseDTO serviceResponse =
                new RhCompanyResponseDTO(
                        10L,
                        "Empresa Teste",
                        false
                );

        when(
                rhCompanyService.findCurrentCompany(
                        rhUser
                )
        ).thenReturn(
                serviceResponse
        );

        ResponseEntity<RhCompanyResponseDTO> response =
                controller.findCurrentCompany(
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
                rhCompanyService
        ).findCurrentCompany(
                rhUser
        );
    }

    @Test
    void shouldReturnCreatedWhenNewCompanyIsCreated() {
        CompanyAssociationRequestDTO request =
                new CompanyAssociationRequestDTO(
                        "Nova Empresa"
                );

        RhCompanyResponseDTO serviceResponse =
                new RhCompanyResponseDTO(
                        20L,
                        "Nova Empresa",
                        true
                );

        when(
                rhCompanyService.associateCompany(
                        request,
                        rhUser
                )
        ).thenReturn(
                serviceResponse
        );

        ResponseEntity<RhCompanyResponseDTO> response =
                controller.associateCompany(
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
                rhCompanyService
        ).associateCompany(
                request,
                rhUser
        );
    }

    @Test
    void shouldReturnOkWhenExistingCompanyIsAssociated() {
        CompanyAssociationRequestDTO request =
                new CompanyAssociationRequestDTO(
                        "Empresa Existente"
                );

        RhCompanyResponseDTO serviceResponse =
                new RhCompanyResponseDTO(
                        30L,
                        "Empresa Existente",
                        false
                );

        when(
                rhCompanyService.associateCompany(
                        request,
                        rhUser
                )
        ).thenReturn(
                serviceResponse
        );

        ResponseEntity<RhCompanyResponseDTO> response =
                controller.associateCompany(
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
    }
}