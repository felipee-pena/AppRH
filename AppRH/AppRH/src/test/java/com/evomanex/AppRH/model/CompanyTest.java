package com.evomanex.AppRH.model;

import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.domain.user.UserRole;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CompanyTest {

    @Test
    void shouldStoreCompanyProperties() {
        Company company =
                new Company();

        OffsetDateTime createdAt =
                OffsetDateTime.now();

        OffsetDateTime updatedAt =
                createdAt.plusMinutes(1);

        company.setId(
                1L
        );

        company.setName(
                "São João Tecnologia"
        );

        company.setNormalizedName(
                "sao joao tecnologia"
        );

        company.setCreatedAt(
                createdAt
        );

        company.setUpdatedAt(
                updatedAt
        );

        assertThat(
                company.getId()
        ).isEqualTo(
                1L
        );

        assertThat(
                company.getName()
        ).isEqualTo(
                "São João Tecnologia"
        );

        assertThat(
                company.getNormalizedName()
        ).isEqualTo(
                "sao joao tecnologia"
        );

        assertThat(
                company.getCreatedAt()
        ).isEqualTo(
                createdAt
        );

        assertThat(
                company.getUpdatedAt()
        ).isEqualTo(
                updatedAt
        );
    }

    @Test
    void shouldStoreUsers() {
        Company company =
                new Company();

        User user =
                new User(
                        "rh@example.com",
                        "$2a$10$encoded",
                        UserRole.USERRH
                );

        List<User> users =
                new ArrayList<>();

        users.add(
                user
        );

        company.setUsers(
                users
        );

        assertThat(
                company.getUsers()
        )
                .containsExactly(
                        user
                );
    }

    @Test
    void shouldStartWithEmptyCollections() {
        Company company =
                new Company();

        assertThat(
                company.getJobs()
        ).isEmpty();

        assertThat(
                company.getUsers()
        ).isEmpty();
    }
}