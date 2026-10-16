package com.evomanex.AppRH.service;

import com.evomanex.AppRH.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

@Service
public class AuthorizationService
        implements UserDetailsService {

    private final UserRepository userRepository;

    public AuthorizationService(
            UserRepository userRepository
    ) {
        this.userRepository =
                userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(
            String login
    ) throws UsernameNotFoundException {

        String normalizedLogin =
                login
                        .trim()
                        .toLowerCase();

        UserDetails userDetails =
                userRepository.findByLogin(
                        normalizedLogin
                );

        if (userDetails == null) {
            throw new UsernameNotFoundException(
                    "Usuário não encontrado"
            );
        }

        return userDetails;
    }
}