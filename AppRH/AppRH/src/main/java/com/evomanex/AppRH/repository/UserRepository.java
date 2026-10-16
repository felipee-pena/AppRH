package com.evomanex.AppRH.repository;

import com.evomanex.AppRH.domain.user.User;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, String> {

   @EntityGraph(
           attributePaths = {
                   "company"
           }
   )
   UserDetails findByLogin(
           String login
   );

   @EntityGraph(
           attributePaths = {
                   "company"
           }
   )
   Optional<User> findUserById(
           String id
   );

   boolean existsByLogin(
           String login
   );
}