package com.optracker.api.repository;

import com.optracker.api.entity.Card;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    // 📝 MÉTODO: Traz a carta e a gaveta das variantes numa única viagem para poupar memória.
    @EntityGraph(attributePaths = {"variants"})
    Optional<Card> findByCode(String code);

    // 🆕 O NOVO CAMINHO: Vai à Variante, entra no CardSet, e procura pelo ID!
    List<Card> findByVariantsCardSetSetId(String setId);
}