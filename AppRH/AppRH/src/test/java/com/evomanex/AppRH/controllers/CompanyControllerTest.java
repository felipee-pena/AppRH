package com.evomanex.AppRH.controllers;

import com.evomanex.AppRH.domain.user.CompanyResponseDTO;
import com.evomanex.AppRH.service.CompanyService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CompanyControllerTest {

    @Mock
    private CompanyService companyService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        CompanyController controller =
                new CompanyController(
                        companyService
                );

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(
                                controller
                        )
                        .build();
    }

    @Test
    void shouldReturnCompanies() throws Exception {
        CompanyResponseDTO firstCompany =
                new CompanyResponseDTO(
                        1L,
                        "Alpha"
                );

        CompanyResponseDTO secondCompany =
                new CompanyResponseDTO(
                        2L,
                        "Beta"
                );

        when(
                companyService.findAll()
        ).thenReturn(
                List.of(
                        firstCompany,
                        secondCompany
                )
        );

        mockMvc.perform(
                        get(
                                "/api/companies"
                        )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        jsonPath("$")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$.length()")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$[0].id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0].name")
                                .value("Alpha")
                )
                .andExpect(
                        jsonPath("$[1].id")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$[1].name")
                                .value("Beta")
                );

        verify(
                companyService
        ).findAll();
    }

    @Test
    void shouldReturnEmptyArrayWhenNoCompanyExists()
            throws Exception {
        when(
                companyService.findAll()
        ).thenReturn(
                List.of()
        );

        mockMvc.perform(
                        get(
                                "/api/companies"
                        )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        jsonPath("$")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$.length()")
                                .value(0)
                );

        verify(
                companyService
        ).findAll();
    }
}