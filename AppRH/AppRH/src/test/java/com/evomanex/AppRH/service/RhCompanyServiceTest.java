package com.evomanex.AppRH.service;

import com.evomanex.AppRH.domain.user.CompanyAssociationRequestDTO;
import com.evomanex.AppRH.domain.user.RhCompanyResponseDTO;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.domain.user.UserRole;

import com.evomanex.AppRH.model.Company;

import com.evomanex.AppRH.repository.CompanyRepository;
import com.evomanex.AppRH.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RhCompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RhCompanyService service;

    @Test
    void shouldReturnCurrentCompanyForRhUser() {
        User rhUser =
                new User(
                        "rh@example.com",
                        "$2a$10$encoded",
                        UserRole.USERRH
                );

        Company company =
                new Company();

        company.setId(
                10L
        );

        company.setName(
                "Empresa Teste"
        );

        rhUser.setCompany(
                company
        );

        RhCompanyResponseDTO response =
                service.findCurrentCompany(
                        rhUser
                );

        assertThat(
                response.id()
        ).isEqualTo(
                10L
        );

        assertThat(
                response.name()
        ).isEqualTo(
                "Empresa Teste"
        );

        assertThat(
                response.created()
        ).isFalse();
    }

    @Test
    void shouldReturnNotFoundWhenRhUserHasNoCompany() {
        User rhUser =
                new User(
                        "rh@example.com",
                        "$2a$10$encoded",
                        UserRole.USERRH
                );

        assertThatThrownBy(
                () ->
                        service.findCurrentCompany(
                                rhUser
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
                                    "O usuário de RH ainda não possui empresa vinculada"
                            );
                        }
                );
    }

    @Test
    void shouldRejectUnauthenticatedUser() {
        assertThatThrownBy(
                () ->
                        service.findCurrentCompany(
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
    void shouldRejectCandidateUser() {
        User candidate =
                new User(
                        "candidate@example.com",
                        "$2a$10$encoded",
                        UserRole.USER
                );

        assertThatThrownBy(
                () ->
                        service.findCurrentCompany(
                                candidate
                        )
        )
                .isInstanceOf(
                        AccessDeniedException.class
                )
                .hasMessage(
                        "A vinculação de empresa é exclusiva para usuários de RH"
                );
    }

    @Test
    void shouldAssociateExistingCompany() {
        User rhUser =
                new User(
                        "rh@example.com",
                        "$2a$10$encoded",
                        UserRole.USERRH
                );

        CompanyAssociationRequestDTO request =
                new CompanyAssociationRequestDTO(
                        "  Empresa   Existente  "
                );

        Company existingCompany =
                new Company();

        existingCompany.setId(
                20L
        );

        existingCompany.setName(
                "Empresa Existente"
        );

        existingCompany.setNormalizedName(
                "empresa existente"
        );

        when(
                companyRepository.findByNormalizedName(
                        "empresa existente"
                )
        ).thenReturn(
                Optional.of(
                        existingCompany
                )
        );

        RhCompanyResponseDTO response =
                service.associateCompany(
                        request,
                        rhUser
                );

        assertThat(
                response.id()
        ).isEqualTo(
                20L
        );

        assertThat(
                response.name()
        ).isEqualTo(
                "Empresa Existente"
        );

        assertThat(
                response.created()
        ).isFalse();

        assertThat(
                rhUser.getCompany()
        ).isSameAs(
                existingCompany
        );

        verify(
                companyRepository,
                never()
        ).save(
                any(Company.class)
        );

        verify(
                userRepository
        ).save(
                rhUser
        );
    }

    @Test
    void shouldCreateAndAssociateNewCompany() {
        User rhUser =
                new User(
                        "rh@example.com",
                        "$2a$10$encoded",
                        UserRole.USERRH
                );

        CompanyAssociationRequestDTO request =
                new CompanyAssociationRequestDTO(
                        "  Nova   Empresa  "
                );

        when(
                companyRepository.findByNormalizedName(
                        "nova empresa"
                )
        ).thenReturn(
                Optional.empty()
        );

        when(
                companyRepository.save(
                        any(Company.class)
                )
        ).thenAnswer(
                invocation -> {
                    Company company =
                            invocation.getArgument(0);

                    company.setId(
                            30L
                    );

                    return company;
                }
        );

        RhCompanyResponseDTO response =
                service.associateCompany(
                        request,
                        rhUser
                );

        ArgumentCaptor<Company> companyCaptor =
                ArgumentCaptor.forClass(
                        Company.class
                );

        verify(
                companyRepository
        ).save(
                companyCaptor.capture()
        );

        Company createdCompany =
                companyCaptor.getValue();

        assertThat(
                createdCompany.getName()
        ).isEqualTo(
                "Nova Empresa"
        );

        assertThat(
                createdCompany.getNormalizedName()
        ).isEqualTo(
                "nova empresa"
        );

        assertThat(
                response.id()
        ).isEqualTo(
                30L
        );

        assertThat(
                response.created()
        ).isTrue();

        assertThat(
                rhUser.getCompany()
        ).isSameAs(
                createdCompany
        );

        verify(
                userRepository
        ).save(
                rhUser
        );
    }

    @Test
    void shouldRejectAssociationWhenRhAlreadyHasCompany() {
        User rhUser =
                new User(
                        "rh@example.com",
                        "$2a$10$encoded",
                        UserRole.USERRH
                );

        Company company =
                new Company();

        company.setId(
                10L
        );

        company.setName(
                "Empresa Existente"
        );

        rhUser.setCompany(
                company
        );

        CompanyAssociationRequestDTO request =
                new CompanyAssociationRequestDTO(
                        "Outra Empresa"
                );

        assertThatThrownBy(
                () ->
                        service.associateCompany(
                                request,
                                rhUser
                        )
        )
                .isInstanceOf(
                        ResponseStatusException.class
                )
                .hasMessageContaining(
                        "já possui uma empresa vinculada"
                );

        verify(
                companyRepository,
                never()
        ).save(
                any(Company.class)
        );

        verify(
                userRepository,
                never()
        ).save(
                any(User.class)
        );
    }
}