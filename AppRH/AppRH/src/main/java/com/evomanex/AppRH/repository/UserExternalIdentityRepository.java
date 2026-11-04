package com.evomanex.AppRH.repository;

import com.evomanex.AppRH.domain.identity.UserExternalIdentity;
import com.evomanex.AppRH.infra.security.oauth.ExternalIdentityProvider;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserExternalIdentityRepository
        extends JpaRepository<
        UserExternalIdentity,
        Long
        > {

    @EntityGraph(
            attributePaths = {
                    "user",
                    "user.company"
            }
    )
    Optional<UserExternalIdentity>
    findByProviderAndProviderUserId(
            ExternalIdentityProvider provider,
            String providerUserId
    );

    @EntityGraph(
            attributePaths = {
                    "user",
                    "user.company"
            }
    )
    Optional<UserExternalIdentity>
    findByUser_IdAndProvider(
            String userId,
            ExternalIdentityProvider provider
    );

    @EntityGraph(
            attributePaths = {
                    "user",
                    "user.company"
            }
    )
    List<UserExternalIdentity>
    findAllByUser_Id(
            String userId
    );

    boolean existsByProviderAndProviderUserId(
            ExternalIdentityProvider provider,
            String providerUserId
    );

    boolean existsByUser_IdAndProvider(
            String userId,
            ExternalIdentityProvider provider
    );

    long deleteByUser_IdAndProvider(
            String userId,
            ExternalIdentityProvider provider
    );
}