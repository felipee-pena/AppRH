package com.evomanex.AppRH.service;

import com.evomanex.AppRH.domain.user.CompanyResponseDTO;
import com.evomanex.AppRH.model.Company;
import com.evomanex.AppRH.repository.CompanyRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(
            CompanyRepository companyRepository
    ) {
        this.companyRepository =
                companyRepository;
    }

    public List<CompanyResponseDTO> findAll() {
        return companyRepository
                .findAllByOrderByNameAsc()
                .stream()
                .map(CompanyResponseDTO::from)
                .toList();
    }

    public Company findEntityById(
            Long id
    ) {
        return companyRepository
                .findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Empresa não encontrada"
                        )
                );
    }
}