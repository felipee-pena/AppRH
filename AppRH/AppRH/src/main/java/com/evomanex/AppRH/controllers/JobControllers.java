package com.evomanex.AppRH.controllers;

import com.evomanex.AppRH.repository.CandidateRepository;
import com.evomanex.AppRH.repository.JobRepository;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.Banner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.evomanex.AppRH.model.*;
@Controller
public class JobControllers {
    @Autowired
    private JobRepository vr;
    @Autowired
    private CandidateRepository cr;

    //endpoint cadastro vagas
    @RequestMapping(
            value = "/create-Job",
            method = RequestMethod.GET
    )
    public ModelAndView form() {
        ModelAndView mv = new ModelAndView("job/formJob");
        mv.addObject("job", new Job());
        return mv;
    }

    @RequestMapping(
            value = "/create-Job",
            method = RequestMethod.POST
    )
    public String createJob(
            @Valid Job job,
            BindingResult result,
            RedirectAttributes attributes) {

        if (result.hasErrors()) {
            attributes.addFlashAttribute(
                    "errorMessagem",
                    "Verifique os campos."
            );

            return "redirect:/create-Job";
        }

        vr.save(job);

        attributes.addFlashAttribute(
                "messagem",
                "Vaga cadastrada com sucesso!"
        );

        return "redirect:/create-Job";
    }

    //endpoint listar vagas
    @RequestMapping("/jobs")
    public ModelAndView listaVagas() {
        ModelAndView mv = new ModelAndView("job/list-Job");
        Iterable<Job> jobs = vr.findAll();
        mv.addObject("jobs", jobs);
        return mv;
    }

    //endpoint detalhar vaga GET
    @RequestMapping(value = "/job/{code}", method = RequestMethod.GET)
    public ModelAndView detailsJob(@PathVariable("code") long code) {
        Job job = vr.findByCode(code);
        ModelAndView mv = new ModelAndView("job/details-Job");
        mv.addObject("job", job);

        Iterable<Candidate> candidates = cr.findByJob(job);

        return mv;
    }

    //endpoint detalhar vaga
    @RequestMapping(value = "/job/{code}", method = RequestMethod.POST)
    public String detailsJobPost(@PathVariable("code") long code, @Valid Candidate candidate,
                                 BindingResult result, RedirectAttributes attributes) {
        if (result.hasErrors()) {
            attributes.addFlashAttribute("messagem", "Verifique os campos");
            return "redirect:/{code}";
        }

        //regra rg duplicado
        if (cr.findByRg(candidate.getRg()) != null) {
            attributes.addFlashAttribute("messagem error", "RG duplicado");
            return "redirect:/{code}";
        }

        //regra mensagem candidato adicionado
        Job job = vr.findByCode(code);
        candidate.setJob(job);
        cr.save(candidate);
        attributes.addFlashAttribute("messagem", "Candidato adicionado com sucesso!");
        return "redirect:/{code}";

    }

    //Deletar candidato pelo RG
    @RequestMapping("/deleteCandidate")
    public String deleteCandidate(String rg){
        Candidate candidate = cr.findByRg(rg);
        Job job = candidate.getJob();
        String code = "" + job.getCode();
        cr.delete(candidate);
        return "redirect:/" + code;
    }

    //endpoint atualização vaga
    //formulario de edição de vaga
    @RequestMapping(value = "/edit-job", method = RequestMethod.GET)
    public ModelAndView editJob(long code){
        Job job = vr.findByCode(code);
        ModelAndView mv = new ModelAndView("job/update-Job");
        mv.addObject("job", job);
        return mv;
    }

    //Regra atualização vaga
    @RequestMapping(value = "edit-job", method = RequestMethod.POST)
    public String updateJob(@Valid Job job, BindingResult result, RedirectAttributes attributes){
        vr.save(job);
        attributes.addFlashAttribute("sucesso", "Vaga alterada com sucesso!");
        long codeLong = job.getCode();
        String code = "" + codeLong;
        return "redirect:/jobs" + code;
    }

    //endpoint deletar vaga
    @RequestMapping("/deleteJob")
    public String deleteJob(long code){
        Job job = vr.findByCode(code);
        vr.delete(job);
        return "redirect:/jobs";
    }

}
