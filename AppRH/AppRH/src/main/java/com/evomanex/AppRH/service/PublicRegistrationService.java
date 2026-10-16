package com.evomanex.AppRH.service;

import com.evomanex.AppRH.domain.user.PublicRegistrationRole;
import com.evomanex.AppRH.domain.user.RegisterDTO;
import com.evomanex.AppRH.domain.user.RegisterResponseDTO;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.domain.user.UserRole;
import com.evomanex.AppRH.model.Company;
import com.evomanex.AppRH.repository.CompanyRepository;
import com.evomanex.AppRH.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class PublicRegistrationService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CompanyService companyService;
    private final PasswordEncoder passwordEncoder;

    public PublicRegistrationService(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            CompanyService companyService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository =
                userRepository;

        this.companyRepository =
                companyRepository;

        this.companyService =
                companyService;

        this.passwordEncoder =
                passwordEncoder;
    }

    @Transactional
    public RegisterResponseDTO register(
            RegisterDTO request
    ) {
        String normalizedLogin =
                normalizeLogin(
                        request.login()
                );

        if (
                userRepository.existsByLogin(
                        normalizedLogin
                )
        ) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe um usuário com este login"
            );
        }

        UserRole userRole =
                resolveUserRole(
                        request.role()
                );

        Company company =
                resolveCompany(
                        request
                );

        String encodedPassword =
                passwordEncoder.encode(
                        request.password()
                );

        User user =
                new User(
                        normalizedLogin,
                        encodedPassword,
                        userRole
                );

        user.setCompany(
                company
        );

        User savedUser =
                userRepository.saveAndFlush(
                        user
                );

        return RegisterResponseDTO.from(
                savedUser
        );
    }

    private UserRole resolveUserRole(
            PublicRegistrationRole role
    ) {
        if (
                role == PublicRegistrationRole.USER
        ) {
            return UserRole.USER;
        }

        if (
                role == PublicRegistrationRole.USERRH
        ) {
            return UserRole.USERRH;
        }

        /*
         * O enum público possui somente USER
         * e USERRH. Esta proteção mantém a regra
         * explícita no domínio.
         */
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Perfil público inválido"
        );
    }

    private Company resolveCompany(
            RegisterDTO request
    ) {
        /*
         * Candidatos não pertencem a empresas.
         */
        if (
                request.role()
                        == PublicRegistrationRole.USER
        ) {
            if (
                    request.companyId() != null
                            || hasText(
                            request.newCompanyName()
                    )
            ) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Candidatos não devem informar empresa"
                );
            }

            return null;
        }

        /*
         * A partir deste ponto, o perfil é USERRH.
         *
         * O cadastro precisa indicar:
         *
         * 1. uma empresa existente por companyId; ou
         * 2. uma empresa nova por newCompanyName.
         *
         * As duas opções simultaneamente não são aceitas.
         */
        boolean hasCompanyId =
                request.companyId() != null;

        boolean hasNewCompanyName =
                hasText(
                        request.newCompanyName()
                );

        if (
                hasCompanyId
                        && hasNewCompanyName
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe uma empresa existente "
                            + "ou uma nova empresa, não ambas"
            );
        }

        if (
                !hasCompanyId
                        && !hasNewCompanyName
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O profissional de RH deve informar "
                            + "uma empresa existente ou uma nova empresa"
            );
        }

        /*
         * Empresa existente selecionada no frontend.
         */
        if (hasCompanyId) {
            return companyService
                    .findEntityById(
                            request.companyId()
                    );
        }

        /*
         * Nova empresa informada no cadastro.
         *
         * Antes de criar, o backend procura pelo nome
         * normalizado para evitar duplicidade por diferença
         * de letras maiúsculas, minúsculas ou espaços.
         */
        String displayName =
                normalizeDisplayName(
                        request.newCompanyName()
                );

        String normalizedName =
                normalizeCompanyName(
                        request.newCompanyName()
                );

        return companyRepository
                .findByNormalizedName(
                        normalizedName
                )
                .orElseGet(
                        () -> createCompany(
                                displayName,
                                normalizedName
                        )
                );
    }

    private Company createCompany(
            String displayName,
            String normalizedName
    ) {
        Company company = new Company();

        company.setName(
                displayName
        );

        company.setNormalizedName(
                normalizedName
        );

        return companyRepository.save(
                company
        );
    }

    private String normalizeLogin(
            String login
    ) {
        return login
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }

    private String normalizeDisplayName(
            String name
    ) {
        return name
                .trim()
                .replaceAll(
                        "\\s+",
                        " "
                );
    }

    private String normalizeCompanyName(
            String name
    ) {
        return normalizeDisplayName(
                name
        ).toLowerCase(
                Locale.ROOT
        );
    }

    private boolean hasText(
            String value
    ) {
        return value != null
                && !value.isBlank();
    }
}