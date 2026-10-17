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

import java.text.Normalizer;

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

        validateDuplicatedLogin(
                normalizedLogin
        );

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

    private void validateDuplicatedLogin(
            String normalizedLogin
    ) {
        boolean loginAlreadyExists =
                userRepository
                        .existsByLoginIgnoreCase(
                                normalizedLogin
                        );

        if (loginAlreadyExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe um usuário com este login"
            );
        }
    }

    private UserRole resolveUserRole(
            PublicRegistrationRole role
    ) {
        return switch (role) {
            case USER ->
                    UserRole.USER;

            case USERRH ->
                    UserRole.USERRH;
        };
    }

    private Company resolveCompany(
            RegisterDTO request
    ) {
        return switch (request.role()) {
            case USER ->
                    resolveCandidateCompany(
                            request
                    );

            case USERRH ->
                    resolveRhCompany(
                            request
                    );
        };
    }

    private Company resolveCandidateCompany(
            RegisterDTO request
    ) {
        boolean hasCompanyId =
                request.companyId() != null;

        boolean hasNewCompanyName =
                hasText(
                        request.newCompanyName()
                );

        if (
                hasCompanyId
                        || hasNewCompanyName
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Candidatos não devem informar empresa"
            );
        }

        return null;
    }

    private Company resolveRhCompany(
            RegisterDTO request
    ) {
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

        if (hasCompanyId) {
            return companyService.findEntityById(
                    request.companyId()
            );
        }

        return resolveNewCompany(
                request.newCompanyName()
        );
    }

    private Company resolveNewCompany(
            String requestedName
    ) {
        String displayName =
                normalizeDisplayName(
                        requestedName
                );

        String normalizedName =
                normalizeCompanyName(
                        displayName
                );

        return companyRepository
                .findByNormalizedName(
                        normalizedName
                )
                .orElseGet(
                        () ->
                                createCompany(
                                        displayName,
                                        normalizedName
                                )
                );
    }

    private Company createCompany(
            String displayName,
            String normalizedName
    ) {
        Company company =
                new Company();

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
        String displayName =
                normalizeDisplayName(
                        name
                );

        String withoutAccents =
                Normalizer
                        .normalize(
                                displayName,
                                Normalizer.Form.NFD
                        )
                        .replaceAll(
                                "\\p{M}+",
                                ""
                        );

        return withoutAccents
                .toLowerCase(
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