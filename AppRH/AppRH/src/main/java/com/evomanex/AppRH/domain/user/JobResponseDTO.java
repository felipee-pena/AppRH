package com.evomanex.AppRH.domain.user;

import com.evomanex.AppRH.model.Job;

public record JobResponseDTO(
        long code,
        String name,
        String description,
        String date,
        String salary
) {

    public static JobResponseDTO from(Job job) {
        return new JobResponseDTO(
                job.getCode(),
                job.getName(),
                job.getDescription(),
                job.getDate(),
                job.getSalary()
        );
    }
}