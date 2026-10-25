package com.evomanex.AppRH.model;

import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.domain.user.UserRole;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JobTest {

    @Test
    void shouldStartWithEmptyCandidateList() {
        Job job =
                new Job();

        assertThat(
                job.getCandidates()
        ).isNotNull();

        assertThat(
                job.getCandidates()
        ).isEmpty();
    }

    @Test
    void shouldStoreJobProperties() {
        Job job =
                new Job();

        Company company =
                new Company();

        company.setId(
                10L
        );

        company.setName(
                "Empresa Teste"
        );

        User createdBy =
                new User(
                        "rh@example.com",
                        "$2a$10$encoded",
                        UserRole.USERRH
                );

        LocalDate expirationDate =
                LocalDate
                        .now()
                        .plusDays(
                                30
                        );

        OffsetDateTime createdAt =
                OffsetDateTime.now();

        BigDecimal salary =
                new BigDecimal(
                        "9500.00"
                );

        job.setCode(
                100L
        );

        job.setName(
                "Analista de Sistemas"
        );

        job.setDescription(
                "Desenvolvimento de aplicações"
        );

        job.setExpirationDate(
                expirationDate
        );

        job.setSalary(
                salary
        );

        job.setContactEmail(
                "vagas@example.com"
        );

        job.setCompany(
                company
        );

        job.setCreatedAt(
                createdAt
        );

        job.setCreatedBy(
                createdBy
        );

        assertThat(
                job.getCode()
        ).isEqualTo(
                100L
        );

        assertThat(
                job.getName()
        ).isEqualTo(
                "Analista de Sistemas"
        );

        assertThat(
                job.getDescription()
        ).isEqualTo(
                "Desenvolvimento de aplicações"
        );

        assertThat(
                job.getExpirationDate()
        ).isEqualTo(
                expirationDate
        );

        assertThat(
                job.getSalary()
        ).isEqualByComparingTo(
                salary
        );

        assertThat(
                job.getContactEmail()
        ).isEqualTo(
                "vagas@example.com"
        );

        assertThat(
                job.getCompany()
        ).isSameAs(
                company
        );

        assertThat(
                job.getCreatedAt()
        ).isEqualTo(
                createdAt
        );

        assertThat(
                job.getCreatedBy()
        ).isSameAs(
                createdBy
        );
    }

    @Test
    void shouldStoreCandidates() {
        Job job =
                new Job();

        Candidate firstCandidate =
                new Candidate();

        firstCandidate.setRg(
                "111111111"
        );

        Candidate secondCandidate =
                new Candidate();

        secondCandidate.setRg(
                "222222222"
        );

        job.setCandidates(
                List.of(
                        firstCandidate,
                        secondCandidate
                )
        );

        assertThat(
                job.getCandidates()
        ).containsExactly(
                firstCandidate,
                secondCandidate
        );
    }
}