package com.optracker.api.repository;

import com.optracker.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 1. Busca básica para Login (Username ou Email)
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    // 2. Busca para o Sistema de Trocas (Encontrar vizinhos por cidade/país)
    // Filtra utilizadores que NÃO estão de férias e têm perfil público
    @Query("SELECT u FROM User u JOIN u.address a JOIN u.profile p " +
            "WHERE a.city = :city AND p.isOnVacation = false AND p.isPublicProfile = true")
    List<User> findActiveTradersByCity(@Param("city") String city);

    // 3. Busca por Reputação (Encontrar os "Piratas de Honra" - PowerSellers)
    @Query("SELECT u FROM User u JOIN u.stats s WHERE s.positiveEvaluations > :minPositives")
    List<User> findTopRatedUsers(@Param("minPositives") Integer minPositives);

    // 4. Verificação de existência (Útil para o formulário de Registo)
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}