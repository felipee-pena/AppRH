package com.evomanex.AppRH.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CandidateTest {

    @Test
    void shouldStoreCandidateProperties() {
        Candidate candidate =
                new Candidate();

        Job job =
                new Job();

        job.setCode(
                10L
        );

        candidate.setRg(
                "123456789"
        );

        candidate.setNameCandidate(
                "Candidato Teste"
        );

        candidate.setEmail(
                "candidato@example.com"
        );

        candidate.setJob(
                job
        );

        assertThat(
                candidate.getRg()
        ).isEqualTo(
                "123456789"
        );

        assertThat(
                candidate.getNameCandidate()
        ).isEqualTo(
                "Candidato Teste"
        );

        assertThat(
                candidate.getEmail()
        ).isEqualTo(
                "candidato@example.com"
        );

        assertThat(
                candidate.getJob()
        ).isSameAs(
                job
        );
    }

    @Test
    void shouldAllowChangingAssociatedJob() {
        Candidate candidate =
                new Candidate();

        Job firstJob =
                new Job();

        firstJob.setCode(
                1L
        );

        Job secondJob =
                new Job();

        secondJob.setCode(
                2L
        );

        candidate.setJob(
                firstJob
        );

        candidate.setJob(
                secondJob
        );

        assertThat(
                candidate.getJob()
        ).isSameAs(
                secondJob
        );
    }
}