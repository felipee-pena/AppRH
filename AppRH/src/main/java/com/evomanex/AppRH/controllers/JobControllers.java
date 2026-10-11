package com.evomanex.AppRH.controllers;

import com.evomanex.AppRH.repository.CandidateRepository;
import com.evomanex.AppRH.repository.JobRepository;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Controller;
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

    //endpoint cadastro vaga
    @RequestMapping(value = "/create-Job", method = RequestMethod.GET)
    public String form(){
        return "job/formJob";
    }
    @RequestMapping(value = "/create-Job", method = RequestMethod.POST)
    public String form(@Valid Job job, BindingResult result, RedirectAttributes atributes){

        if(result.hasErrors()){
            atributes.addFlashAttribute("menssage", "Verfique os campos...");
            return "redirect:/create-Job";
        }
        vr.save(job);
        atributes.addFlashAttribute("menssage", "Vaga cadastrada com sucesso!");
        return "redirect:/create-Job";
    }
}
