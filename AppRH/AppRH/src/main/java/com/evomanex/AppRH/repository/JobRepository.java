package com.evomanex.AppRH.repository;

import com.evomanex.AppRH.model.Job;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface JobRepository extends CrudRepository<Job, Long> {

    Job findByCode(long code);

    List<Job> findByName(String name);
}