package com.evomanex.AppRH.model;

import com.evomanex.AppRH.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "companies",
        schema = "public"
)
public class Company implements Serializable {

    private static final long serialVersionUID =
            1L;

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(
            nullable = false,
            length = 150
    )
    private String name;

    @Column(
            name = "normalized_name",
            nullable = false,
            unique = true,
            length = 150
    )
    private String normalizedName;

    @Column(
            name = "created_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false,
            insertable = false
    )
    private OffsetDateTime updatedAt;

    @OneToMany(
            mappedBy = "company"
    )
    private List<Job> jobs =
            new ArrayList<>();

    @OneToMany(
            mappedBy = "company"
    )
    private List<User> users =
            new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(
            Long id
    ) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(
            String name
    ) {
        this.name = name;
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    public void setNormalizedName(
            String normalizedName
    ) {
        this.normalizedName =
                normalizedName;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            OffsetDateTime createdAt
    ) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(
            OffsetDateTime updatedAt
    ) {
        this.updatedAt = updatedAt;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(
            List<Job> jobs
    ) {
        this.jobs = jobs;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(
            List<User> users
    ) {
        this.users = users;
    }
}