package com.evomanex.AppRH.domain.user;

import com.evomanex.AppRH.model.Company;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Table(
        name = "users",
        schema = "public"
)
@Entity(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(
            nullable = false,
            unique = true
    )
    private String login;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 10
    )
    private UserRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 40
    )
    private UserStatus status;

    @Column(name = "email_verified_at")
    private OffsetDateTime emailVerifiedAt;

    protected User() {
    }

    public User(
            String login,
            String password,
            UserRole role
    ) {
        this.login = login;
        this.password = password;
        this.role = role;
        this.status = UserStatus.PENDING_EMAIL_CONFIRMATION;
    }

    public User(
            String id,
            String login,
            String password,
            UserRole role,
            Company company,
            UserStatus status,
            OffsetDateTime emailVerifiedAt
    ) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.role = role;
        this.company = company;
        this.status = status;
        this.emailVerifiedAt = emailVerifiedAt;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.role == UserRole.ADMIN) {
            return List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_USERRH"),
                    new SimpleGrantedAuthority("ROLE_USER")
            );
        }

        if (this.role == UserRole.USERRH) {
            return List.of(
                    new SimpleGrantedAuthority("ROLE_USERRH"),
                    new SimpleGrantedAuthority("ROLE_USER")
            );
        }

        return List.of(
                new SimpleGrantedAuthority("ROLE_USER")
        );
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.BLOCKED
                && status != UserStatus.REJECTED
                && status != UserStatus.DELETED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }

    public String getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public UserRole getRole() {
        return role;
    }

    public Company getCompany() {
        return company;
    }

    public UserStatus getStatus() {
        return status;
    }

    public OffsetDateTime getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void setCompany(
            Company company
    ) {
        this.company = company;
    }

    public void confirmEmail() {
        this.emailVerifiedAt = OffsetDateTime.now();

        if (this.role == UserRole.USERRH) {
            this.status = UserStatus.PENDING_APPROVAL;
            return;
        }

        this.status = UserStatus.ACTIVE;
    }

    public boolean isEmailVerified() {
        return emailVerifiedAt != null;
    }

    public boolean isPendingEmailConfirmation() {
        return status
                == UserStatus.PENDING_EMAIL_CONFIRMATION;
    }

    @Override
    public boolean equals(
            Object object
    ) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof User user)) {
            return false;
        }

        return id != null
                && Objects.equals(
                id,
                user.id
        );
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}