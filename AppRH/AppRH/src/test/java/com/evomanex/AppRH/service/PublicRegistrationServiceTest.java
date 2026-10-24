package com.evomanex.AppRH.service;

import com.evomanex.AppRH.domain.user.PublicRegistrationRole;
import com.evomanex.AppRH.domain.user.RegisterDTO;
import com.evomanex.AppRH.domain.user.RegisterResponseDTO;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.domain.user.UserRole;
import com.evomanex.AppRH.domain.user.UserStatus;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicRegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyService companyService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailVerificationService emailVerificationService;

    @InjectMocks
    private PublicRegistrationService service;

    @Test
    void shouldRegisterCandidateWithNormalizedLoginAndEncodedPassword() {
        RegisterDTO request =
                new RegisterDTO(
                        "  Candidato@Example.COM  ",
                        "Usuario@123",
                        PublicRegistrationRole.USER,
                        null,
                        null
                );

        when(
                userRepository.existsByLoginIgnoreCase(
                        "candidato@example.com"
                )
        ).thenReturn(
                false
        );

        when(
                passwordEncoder.encode(
                        "Usuario@123"
                )
        ).thenReturn(
                "$2a$10$encoded-password"
        );

        when(
                userRepository.saveAndFlush(
                        any(User.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        RegisterResponseDTO response =
                service.register(
                        request
                );

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(
                        User.class
                );

        verify(
                userRepository
        ).saveAndFlush(
                userCaptor.capture()
        );

        User savedUser =
                userCaptor.getValue();

        assertThat(
                savedUser.getLogin()
        ).isEqualTo(
                "candidato@example.com"
        );

        assertThat(
                savedUser.getPassword()
        ).isEqualTo(
                "$2a$10$encoded-password"
        );

        assertThat(
                savedUser.getRole()
        ).isEqualTo(
                UserRole.USER
        );

        assertThat(
                savedUser.getStatus()
        ).isEqualTo(
                UserStatus.PENDING_EMAIL_CONFIRMATION
        );

        assertThat(
                savedUser.getCompany()
        ).isNull();

        assertThat(
                response.login()
        ).isEqualTo(
                "candidato@example.com"
        );

        assertThat(
                response.role()
        ).isEqualTo(
                UserRole.USER
        );

        verify(
                emailVerificationService
        ).createVerificationToken(
                savedUser
        );
    }

    @Test
    void shouldRejectDuplicatedLogin() {
        RegisterDTO request =
                new RegisterDTO(
                        "usuario@example.com",
                        "Usuario@123",
                        PublicRegistrationRole.USER,
                        null,
                        null
                );

        when(
                userRepository.existsByLoginIgnoreCase(
                        "usuario@example.com"
                )
        ).thenReturn(
                true
        );

        assertThatThrownBy(
                () ->
                        service.register(
                                request
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
                                    HttpStatus.CONFLICT
                            );

                            assertThat(
                                    responseException.getReason()
                            ).isEqualTo(
                                    "Já existe um usuário com este login"
                            );
                        }
                );

        verify(
                userRepository,
                never()
        ).saveAndFlush(
                any(User.class)
        );

        verify(
                emailVerificationService,
                never()
        ).createVerificationToken(
                any(User.class)
        );
    }

    @Test
    void shouldRejectCompanyForCandidate() {
        RegisterDTO request =
                new RegisterDTO(
                        "candidato@example.com",
                        "Usuario@123",
                        PublicRegistrationRole.USER,
                        1L,
                        null
                );

        when(
                userRepository.existsByLoginIgnoreCase(
                        "candidato@example.com"
                )
        ).thenReturn(
                false
        );

        assertThatThrownBy(
                () ->
                        service.register(
                                request
                        )
        )
                .isInstanceOf(
                        ResponseStatusException.class
                )
                .hasMessageContaining(
                        "Candidatos não devem informar empresa"
                );
    }

    @Test
    void shouldRequireCompanyForRhUser() {
        RegisterDTO request =
                new RegisterDTO(
                        "rh@example.com",
                        "Usuario@123",
                        PublicRegistrationRole.USERRH,
                        null,
                        null
                );

        when(
                userRepository.existsByLoginIgnoreCase(
                        "rh@example.com"
                )
        ).thenReturn(
                false
        );

        assertThatThrownBy(
                () ->
                        service.register(
                                request
                        )
        )
                .isInstanceOf(
                        ResponseStatusException.class
                )
                .hasMessageContaining(
                        "O profissional de RH deve informar"
                );
    }

    @Test
    void shouldRejectExistingAndNewCompanyAtSameTime() {
        RegisterDTO request =
                new RegisterDTO(
                        "rh@example.com",
                        "Usuario@123",
                        PublicRegistrationRole.USERRH,
                        1L,
                        "Nova Empresa"
                );

        when(
                userRepository.existsByLoginIgnoreCase(
                        "rh@example.com"
                )
        ).thenReturn(
                false
        );

        assertThatThrownBy(
                () ->
                        service.register(
                                request
                        )
        )
                .isInstanceOf(
                        ResponseStatusException.class
                )
                .hasMessageContaining(
                        "não ambas"
                );
    }

    @Test
    void shouldAssociateExistingCompanyWithRhUser() {
        RegisterDTO request =
                new RegisterDTO(
                        "rh@example.com",
                        "Usuario@123",
                        PublicRegistrationRole.USERRH,
                        10L,
                        null
                );

        Company company =
                new Company();

        company.setId(
                10L
        );

        company.setName(
                "Empresa Existente"
        );

        when(
                userRepository.existsByLoginIgnoreCase(
                        "rh@example.com"
                )
        ).thenReturn(
                false
        );

        when(
                companyService.findEntityById(
                        10L
                )
        ).thenReturn(
                company
        );

        when(
                passwordEncoder.encode(
                        "Usuario@123"
                )
        ).thenReturn(
                "$2a$10$encoded-password"
        );

        when(
                userRepository.saveAndFlush(
                        any(User.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        service.register(
                request
        );

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(
                        User.class
                );

        verify(
                userRepository
        ).saveAndFlush(
                userCaptor.capture()
        );

        assertThat(
                userCaptor
                        .getValue()
                        .getCompany()
        ).isSameAs(
                company
        );

        assertThat(
                userCaptor
                        .getValue()
                        .getRole()
        ).isEqualTo(
                UserRole.USERRH
        );
    }

    @Test
    void shouldCreateNormalizedCompanyForRhUser() {
        RegisterDTO request =
                new RegisterDTO(
                        "rh@example.com",
                        "Usuario@123",
                        PublicRegistrationRole.USERRH,
                        null,
                        "  São   João Tecnologia  "
                );

        when(
                userRepository.existsByLoginIgnoreCase(
                        "rh@example.com"
                )
        ).thenReturn(
                false
        );

        when(
                companyRepository.findByNormalizedName(
                        "sao joao tecnologia"
                )
        ).thenReturn(
                Optional.empty()
        );

        when(
                companyRepository.save(
                        any(Company.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        when(
                passwordEncoder.encode(
                        "Usuario@123"
                )
        ).thenReturn(
                "$2a$10$encoded-password"
        );

        when(
                userRepository.saveAndFlush(
                        any(User.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        service.register(
                request
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

        Company savedCompany =
                companyCaptor.getValue();

        assertThat(
                savedCompany.getName()
        ).isEqualTo(
                "São João Tecnologia"
        );

        assertThat(
                savedCompany.getNormalizedName()
        ).isEqualTo(
                "sao joao tecnologia"
        );
    }
}