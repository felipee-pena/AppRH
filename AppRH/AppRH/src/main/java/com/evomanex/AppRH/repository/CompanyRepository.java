package com.evomanex.AppRH.repository;

import com.evomanex.AppRH.model.Company;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository
        extends CrudRepository<Company, Long> {

    List<Company> findAllByOrderByNameAsc();

    Optional<Company> findByNormalizedName(
            String normalizedName
    );

    List<Company>
    findTop10ByNormalizedNameContainingOrderByNameAsc(
            String normalizedName
    );
}