package com.evomanex.AppRH.events;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserRegisteredEventTest {

    @Test
    void shouldCreateUserRegisteredEvent() {
        UserRegisteredEvent event =
                new UserRegisteredEvent();

        assertThat(
                event
        ).isNotNull();

        assertThat(
                event
                        .getClass()
                        .getSimpleName()
        ).isEqualTo(
                "UserRegisteredEvent"
        );
    }
}