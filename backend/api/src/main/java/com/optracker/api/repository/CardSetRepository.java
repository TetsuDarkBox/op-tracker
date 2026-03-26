package com.optracker.api.repository;

import com.optracker.api.entity.CardSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// 🆕 Agora o JpaRepository recebe um Long em vez de String
@Repository
public interface CardSetRepository extends JpaRepository<CardSet, Long> {

    // 📝 MÉTODO: Procura um Set pelo seu código de texto (ex: OP-01)
    Optional<CardSet> findBySetId(String setId);

    // 📝 MÉTODO: Pergunta Rápida para saber se o Set já existe
    boolean existsBySetId(String setId);
}