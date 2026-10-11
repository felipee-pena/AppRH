package com.evomanex.AppRH.model;
import java.io.Serializable;
import java.util.List;

import jakarta.persistence.*;

import jakarta.validation.constraints.NotEmpty;

@Entity
public class Job implements Serializable{

    private static final long serialVersionUUID = '1';
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long code;
    @NotEmpty
    private String name;
    @NotEmpty
    private String description;
    @NotEmpty
    private String date;
    @NotEmpty
    private String salary;
    @OneToMany(mappedBy = "job", cascade = CascadeType.REMOVE)
    private List<Candidate> Candidate;

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public List<com.evomanex.AppRH.model.Candidate> getCandidate() {
        return Candidate;
    }

    public void setCandidate(List<com.evomanex.AppRH.model.Candidate> candidate) {
        Candidate = candidate;
    }
}
