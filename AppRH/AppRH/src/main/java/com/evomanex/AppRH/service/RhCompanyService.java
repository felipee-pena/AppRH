package com.evomanex.AppRH.service;

import com.evomanex.AppRH.domain.user.CompanyAssociationRequestDTO;
import com.evomanex.AppRH.domain.user.RhCompanyResponseDTO;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.domain.user.UserRole;
import com.evomanex.AppRH.model.Company;
import com.evomanex.AppRH.repository.CompanyRepository;
import com.evomanex.AppRH.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class RhCompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public RhCompanyService(
            CompanyRepository companyRepository,
            UserRepository userRepository
    ) {
        this.companyRepository =
                companyRepository;

        this.userRepository =
                userRepository;
    }

    @Transactional(readOnly = true)
    public RhCompanyResponseDTO findCurrentCompany(
            User authenticatedUser
    ) {
        validateRhUser(
                authenticatedUser
        );

        Company company =
                authenticatedUser.getCompany();

        if (company == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "O usuário de RH ainda não possui empresa vinculada"
            );
        }

        return RhCompanyResponseDTO.from(
                company,
                false
        );
    }

    @Transactional
    public RhCompanyResponseDTO associateCompany(
            CompanyAssociationRequestDTO request,
            User authenticatedUser
    ) {
        validateRhUser(
                authenticatedUser
        );

        if (
                authenticatedUser.getCompany()
                        != null
        ) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O usuário de RH já possui uma empresa vinculada"
            );
        }

        String displayName =
                normalizeDisplayName(
                        request.name()
                );

        String normalizedName =
                normalizeName(
                        request.name()
                );

        Company company =
                companyRepository
                        .findByNormalizedName(
                                normalizedName
                        )
                        .orElse(null);

        boolean created = false;

        if (company == null) {
            company = new Company();

            company.setName(
                    displayName
            );

            company.setNormalizedName(
                    normalizedName
            );

            company =
                    companyRepository.save(
                            company
                    );

            created = true;
        }

        authenticatedUser.setCompany(
                company
        );

        userRepository.save(
                authenticatedUser
        );

        return RhCompanyResponseDTO.from(
                company,
                created
        );
    }

    private void validateRhUser(
            User authenticatedUser
    ) {
        if (authenticatedUser == null) {
            throw new AccessDeniedException(
                    "Usuário não autenticado"
            );
        }

        if (
                authenticatedUser.getRole()
                        != UserRole.USERRH
        ) {
            throw new AccessDeniedException(
                    "A vinculação de empresa é exclusiva para usuários de RH"
            );
        }
    }

    private String normalizeName(
            String name
    ) {
        return name
                .trim()
                .replaceAll(
                        "\\s+",
                        " "
                )
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
}