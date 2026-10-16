package com.evomanex.AppRH.repository;

import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.model.Job;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository
        extends JpaRepository<Job, Long> {

    Job findByCode(
            long code
    );

    List<Job> findAllByOrderByCodeDesc();

    List<Job> findAllByCreatedByOrderByCodeDesc(
            User createdBy
    );
}