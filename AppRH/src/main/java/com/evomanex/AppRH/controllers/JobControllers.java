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

    private JobRepository vr;
    private CandidateRepository cr;

    //endpoint cadastro vagas
    @RequestMapping(value = "/create-Job", method = RequestMethod.GET)
    public String form() {
        return "job/formJob";
    }

    @RequestMapping(value = "/create-Job", method = RequestMethod.POST)
    public String form(@Valid Job job, BindingResult result, RedirectAttributes attributes) {

        if (result.hasErrors()) {
            attributes.addFlashAttribute("menssage", "Verfique os campos...");
            return "redirect:/create-Job";
        }
        vr.save(job);
        attributes.addFlashAttribute("menssage", "Vaga cadastrada com sucesso!");
        return "redirect:/create-Job";
    }

    //endpoint listar vagas
    @RequestMapping("/jobs")
    public ModelAndView listaVagas() {
        ModelAndView mv = new ModelAndView("job/listJob");
        Iterable<Job> jobs = vr.findAll();
        mv.addObject("jobs", jobs);
        return mv;
    }

    //endpoint detalhar vaga GET
    @RequestMapping(value = "/{code}", method = RequestMethod.GET)
    public ModelAndView detailsJob(@PathVariable("code") long code) {
        Job job = vr.findByCode(code);
        ModelAndView mv = new ModelAndView("job/detailsJob");
        mv.addObject("job", job);

        Iterable<Candidate> candidates = cr.findByJob(job);

        return mv;
    }

    //endpoint detalhar vaga
    public String detailsJobPost(@PathVariable("code") long code, @Valid Candidate candidate,
                                 BindingResult result, RedirectAttributes attributes) {
        if (result.hasErrors()) {
            attributes.addFlashAttribute("message", "Verifique os campos");
            return "redirect:/{code}";
        }

        //regra rg duplicado
        if (cr.findByRg(candidate.getRg()) != null) {
            attributes.addFlashAttribute("message error", "RG duplicado");
            return "redirect:/{code}";
        }

        //regra mensagem candidato adicionado
        Job job = vr.findByCode(code);
        candidate.setJob(job);
        cr.save(candidate);
        attributes.addFlashAttribute("message", "Candidato adicionado com sucesso!");
        return "redirect:/{code}";

    }

    //Deletar candidato pelo RG
    @RequestMapping("/deletCandidate")
    public String deletCandidate(String rg){
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
        ModelAndView mv = new ModelAndView("job/update-job");
        mv.addObject("job", job);
        return mv;
    }

    //Regra atualização vaga
    @RequestMapping(value = "edit-job", method = RequestMethod.POST)
    public String updateJob(@Valid Job job, BindingResult result, RedirectAttributes attributes){
        vr.save(job);
        attributes.addFlashAttribute("sucess", "Vaga alterada com sucesso!");
        long codeLong = job.getCode();
        String code = "" + codeLong;
        return "redirect:/" + code;
    }

    //endpoint deletar vaga
    @RequestMapping("/deletJob")
    public String deletJob(long code){
        Job job = vr.findByCode(code);
        vr.delete(job);
        return "redirect:/jobs";
    }

}
