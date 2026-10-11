package com.evomanex.AppRH.repository;

import com.evomanex.AppRH.model.Job;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;


public interface JobRepository extends CrudRepository<Job, String> {
    Job findByCode(long code);
    List<Job> findByName(String name);
}
