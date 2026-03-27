package com.optracker.api.repository;

import com.optracker.api.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    @Query("SELECT DISTINCT c FROM Card c LEFT JOIN FETCH c.variants")
    List<Card> findAllWithVariants();
    Optional<Card> findByCode(String code);

    List<Card> findByVariantsCardSetSetId(String setId);
}