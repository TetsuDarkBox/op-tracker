package com.optracker.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.optracker.api.enums.Role;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role = Role.ROLE_USER;

    private boolean active = true;
    private boolean banned = false;

    // Relacionamentos 1:1 (Perfil, Morada e Estatísticas)
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private UserProfile profile;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private UserAddress address;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "stats_id")
    private UserStats stats;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime lastLogin;

    public User() {}

    // --- MÉTODOS OBRIGATÓRIOS DO USERDETAILS (LOGICA DE SEGURANÇA) ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Converte o Enum Role para a autoridade que o Spring Security entende
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Conta nunca expira por tempo
    }

    @Override
    public boolean isAccountNonLocked() {
        return !banned; // Se está banido, a conta está trancada
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Password nunca expira por tempo
    }

    @Override
    public boolean isEnabled() {
        return active; // Se não está ativo (ex: email não verificado), a conta está desativada
    }

    // --- GETTERS E SETTERS ---


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public UserProfile getProfile() {
        return profile;
    }

    public void setProfile(UserProfile profile) {
        this.profile = profile;
    }

    public UserAddress getAddress() {
        return address;
    }

    public void setAddress(UserAddress address) {
        this.address = address;
    }

    public UserStats getStats() {
        return stats;
    }

    public void setStats(UserStats stats) {
        this.stats = stats;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
}