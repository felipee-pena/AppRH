package com.evomanex.AppRH.service;

import com.evomanex.AppRH.domain.user.CompanyResponseDTO;
import com.evomanex.AppRH.model.Company;
import com.evomanex.AppRH.repository.CompanyRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyService service;

    @Test
    void shouldReturnCompaniesOrderedByName() {
        Company firstCompany =
                new Company();

        firstCompany.setId(
                1L
        );

        firstCompany.setName(
                "Alpha"
        );

        firstCompany.setNormalizedName(
                "alpha"
        );

        Company secondCompany =
                new Company();

        secondCompany.setId(
                2L
        );

        secondCompany.setName(
                "Beta"
        );

        secondCompany.setNormalizedName(
                "beta"
        );

        when(
                companyRepository.findAllByOrderByNameAsc()
        ).thenReturn(
                List.of(
                        firstCompany,
                        secondCompany
                )
        );

        List<CompanyResponseDTO> result =
                service.findAll();

        assertThat(
                result
        ).hasSize(
                2
        );

        assertThat(
                result
        )
                .extracting(
                        CompanyResponseDTO::name
                )
                .containsExactly(
                        "Alpha",
                        "Beta"
                );

        verify(
                companyRepository
        ).findAllByOrderByNameAsc();
    }

    @Test
    void shouldReturnEmptyListWhenNoCompanyExists() {
        when(
                companyRepository.findAllByOrderByNameAsc()
        ).thenReturn(
                List.of()
        );

        List<CompanyResponseDTO> result =
                service.findAll();

        assertThat(
                result
        ).isEmpty();
    }

    @Test
    void shouldFindCompanyEntityById() {
        Company company =
                new Company();

        company.setId(
                10L
        );

        company.setName(
                "Empresa Teste"
        );

        when(
                companyRepository.findById(
                        10L
                )
        ).thenReturn(
                Optional.of(
                        company
                )
        );

        Company result =
                service.findEntityById(
                        10L
                );

        assertThat(
                result
        ).isSameAs(
                company
        );

        verify(
                companyRepository
        ).findById(
                10L
        );
    }

    @Test
    void shouldReturnNotFoundWhenCompanyDoesNotExist() {
        when(
                companyRepository.findById(
                        99L
                )
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(
                () ->
                        service.findEntityById(
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
                                    "Empresa não encontrada"
                            );
                        }
                );
    }
}