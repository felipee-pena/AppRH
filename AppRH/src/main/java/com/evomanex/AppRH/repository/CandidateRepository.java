package com.evomanex.AppRH.repository;

import com.evomanex.AppRH.model.*;

import java.util.List;
import  org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface CandidateRepository extends CrudRepository<Candidate, String> {
    Iterable<Candidate>findByJob(Job job);

    Candidate findByRg(String rg);
    Candidate findById(long id);

    List<Candidate>findByNameCandidate(String nameCandidate);
}
