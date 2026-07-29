package com.optracker.api.repository;

import com.optracker.api.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    // 🆕 Carrega a carta E as suas variantes numa só query para evitar LazyInitializationException
    @Query("SELECT DISTINCT c FROM Card c LEFT JOIN FETCH c.variants WHERE c.code = :code")
    Optional<Card> findByCodeWithVariants(@Param("code") String code);

    // Método que já devias ter para carregar todas as cartas com variantes
    @Query("SELECT DISTINCT c FROM Card c LEFT JOIN FETCH c.variants")
    List<Card> findAllWithVariants();

    Optional<Card> findByCode(String code);

    // Verifica se a carta já existe e se tem os dados fundamentais preenchidos
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Card c " +
            "WHERE c.code = :code AND c.name IS NOT NULL AND c.effect IS NOT NULL")
    boolean isCardFullyScraped(@Param("code") String code);
}