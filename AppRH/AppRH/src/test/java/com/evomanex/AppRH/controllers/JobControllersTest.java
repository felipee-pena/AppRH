package com.evomanex.AppRH.controllers;

import com.evomanex.AppRH.model.Candidate;
import com.evomanex.AppRH.model.Job;
import com.evomanex.AppRH.repository.CandidateRepository;
import com.evomanex.AppRH.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobControllersTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    private JobControllers controller;

    @BeforeEach
    void setUp() {
        controller =
                new JobControllers();

        ReflectionTestUtils.setField(
                controller,
                "vr",
                jobRepository
        );

        ReflectionTestUtils.setField(
                controller,
                "cr",
                candidateRepository
        );
    }

    @Test
    void shouldReturnJobForm() {
        ModelAndView response =
                controller.form();

        assertThat(
                response.getViewName()
        ).isEqualTo(
                "job/formJob"
        );

        assertThat(
                response.getModel()
        ).containsKey(
                "job"
        );

        assertThat(
                response.getModel().get(
                        "job"
                )
        ).isInstanceOf(
                Job.class
        );
    }

    @Test
    void shouldRejectInvalidJobCreation() {
        Job job =
                new Job();

        when(
                bindingResult.hasErrors()
        ).thenReturn(
                true
        );

        String response =
                controller.createJob(
                        job,
                        bindingResult,
                        redirectAttributes
                );

        assertThat(
                response
        ).isEqualTo(
                "redirect:/create-Job"
        );

        verify(
                jobRepository,
                never()
        ).save(
                job
        );

        verify(
                redirectAttributes
        ).addFlashAttribute(
                "errorMessagem",
                "Verifique os campos."
        );
    }

    @Test
    void shouldCreateValidJob() {
        Job job =
                new Job();

        when(
                bindingResult.hasErrors()
        ).thenReturn(
                false
        );

        String response =
                controller.createJob(
                        job,
                        bindingResult,
                        redirectAttributes
                );

        assertThat(
                response
        ).isEqualTo(
                "redirect:/create-Job"
        );

        verify(
                jobRepository
        ).save(
                job
        );

        verify(
                redirectAttributes
        ).addFlashAttribute(
                "messagem",
                "Vaga cadastrada com sucesso!"
        );
    }

    @Test
    void shouldListJobs() {
        Job firstJob =
                new Job();

        Job secondJob =
                new Job();

        when(
                jobRepository.findAll()
        ).thenReturn(
                List.of(
                        firstJob,
                        secondJob
                )
        );

        ModelAndView response =
                controller.listaVagas();

        assertThat(
                response.getViewName()
        ).isEqualTo(
                "job/list-Job"
        );

        assertThat(
                response.getModel().get(
                        "jobs"
                )
        ).isEqualTo(
                List.of(
                        firstJob,
                        secondJob
                )
        );
    }

    @Test
    void shouldReturnJobDetails() {
        Job job =
                new Job();

        job.setCode(
                10L
        );

        when(
                jobRepository.findByCode(
                        10L
                )
        ).thenReturn(
                job
        );

        when(
                candidateRepository.findByJob(
                        job
                )
        ).thenReturn(
                List.of()
        );

        ModelAndView response =
                controller.detailsJob(
                        10L
                );

        assertThat(
                response.getViewName()
        ).isEqualTo(
                "job/details-Job"
        );

        assertThat(
                response.getModel().get(
                        "job"
                )
        ).isSameAs(
                job
        );

        verify(
                candidateRepository
        ).findByJob(
                job
        );
    }

    @Test
    void shouldRejectCandidateWithValidationErrors() {
        Candidate candidate =
                new Candidate();

        when(
                bindingResult.hasErrors()
        ).thenReturn(
                true
        );

        String response =
                controller.detailsJobPost(
                        10L,
                        candidate,
                        bindingResult,
                        redirectAttributes
                );

        assertThat(
                response
        ).isEqualTo(
                "redirect:/{code}"
        );

        verify(
                candidateRepository,
                never()
        ).save(
                candidate
        );
    }

    @Test
    void shouldRejectDuplicatedCandidateRg() {
        Candidate candidate =
                new Candidate();

        candidate.setRg(
                "123"
        );

        when(
                bindingResult.hasErrors()
        ).thenReturn(
                false
        );

        when(
                candidateRepository.findByRg(
                        "123"
                )
        ).thenReturn(
                new Candidate()
        );

        String response =
                controller.detailsJobPost(
                        10L,
                        candidate,
                        bindingResult,
                        redirectAttributes
                );

        assertThat(
                response
        ).isEqualTo(
                "redirect:/{code}"
        );

        verify(
                candidateRepository,
                never()
        ).save(
                candidate
        );
    }

    @Test
    void shouldCreateCandidateForJob() {
        Job job =
                new Job();

        job.setCode(
                10L
        );

        Candidate candidate =
                new Candidate();

        candidate.setRg(
                "123"
        );

        when(
                bindingResult.hasErrors()
        ).thenReturn(
                false
        );

        when(
                candidateRepository.findByRg(
                        "123"
                )
        ).thenReturn(
                null
        );

        when(
                jobRepository.findByCode(
                        10L
                )
        ).thenReturn(
                job
        );

        String response =
                controller.detailsJobPost(
                        10L,
                        candidate,
                        bindingResult,
                        redirectAttributes
                );

        assertThat(
                response
        ).isEqualTo(
                "redirect:/{code}"
        );

        assertThat(
                candidate.getJob()
        ).isSameAs(
                job
        );

        verify(
                candidateRepository
        ).save(
                candidate
        );
    }

    @Test
    void shouldDeleteCandidate() {
        Job job =
                new Job();

        job.setCode(
                10L
        );

        Candidate candidate =
                new Candidate();

        candidate.setJob(
                job
        );

        when(
                candidateRepository.findByRg(
                        "123"
                )
        ).thenReturn(
                candidate
        );

        String response =
                controller.deleteCandidate(
                        "123"
                );

        assertThat(
                response
        ).isEqualTo(
                "redirect:/10"
        );

        verify(
                candidateRepository
        ).delete(
                candidate
        );
    }

    @Test
    void shouldReturnEditJobForm() {
        Job job =
                new Job();

        job.setCode(
                10L
        );

        when(
                jobRepository.findByCode(
                        10L
                )
        ).thenReturn(
                job
        );

        ModelAndView response =
                controller.editJob(
                        10L
                );

        assertThat(
                response.getViewName()
        ).isEqualTo(
                "job/update-Job"
        );

        assertThat(
                response.getModel().get(
                        "job"
                )
        ).isSameAs(
                job
        );
    }

    @Test
    void shouldUpdateJob() {
        Job job =
                new Job();

        job.setCode(
                10L
        );

        String response =
                controller.updateJob(
                        job,
                        bindingResult,
                        redirectAttributes
                );

        assertThat(
                response
        ).isEqualTo(
                "redirect:/job/10"
        );

        verify(
                jobRepository
        ).save(
                job
        );

        verify(
                redirectAttributes
        ).addFlashAttribute(
                "sucesso",
                "Vaga alterada com sucesso!"
        );
    }

    @Test
    void shouldDeleteJob() {
        Job job =
                new Job();

        when(
                jobRepository.findByCode(
                        10L
                )
        ).thenReturn(
                job
        );

        String response =
                controller.deleteJob(
                        10L
                );

        assertThat(
                response
        ).isEqualTo(
                "redirect:/jobs"
        );

        verify(
                jobRepository
        ).delete(
                job
        );
    }
}