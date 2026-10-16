package com.evomanex.AppRH.model;

import com.evomanex.AppRH.domain.user.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "job",
        schema = "public"
)
public class Job implements Serializable {

    private static final long serialVersionUID =
            1L;

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private long code;

    @Column(
            nullable = false,
            length = 255
    )
    private String name;

    @Column(
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String description;

    @Column(
            name = "expiration_date",
            nullable = false
    )
    private LocalDate expirationDate;

    @Column(
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal salary;

    @Column(
            name = "contact_email",
            length = 255
    )
    private String contactEmail;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "company_id",
            nullable = false
    )
    private Company company;

    @Column(
            name = "created_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "created_by_user_id",
            nullable = false
    )
    private User createdBy;

    @OneToMany(
            mappedBy = "job",
            cascade = CascadeType.REMOVE
    )
    private List<Candidate> candidates =
            new ArrayList<>();

    public long getCode() {
        return code;
    }

    public void setCode(
            long code
    ) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(
            String name
    ) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description
    ) {
        this.description = description;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(
            LocalDate expirationDate
    ) {
        this.expirationDate =
                expirationDate;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(
            BigDecimal salary
    ) {
        this.salary = salary;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(
            String contactEmail
    ) {
        this.contactEmail =
                contactEmail;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(
            Company company
    ) {
        this.company = company;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            OffsetDateTime createdAt
    ) {
        this.createdAt = createdAt;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(
            User createdBy
    ) {
        this.createdBy = createdBy;
    }

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(
            List<Candidate> candidates
    ) {
        this.candidates = candidates;
    }
}