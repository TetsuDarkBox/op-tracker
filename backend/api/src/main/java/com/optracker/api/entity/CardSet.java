package com.optracker.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "card_sets")
public class CardSet {

    // 🆕 A verdadeira Chave Primária (Número automático)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // O código oficial do Set (Ex: OP-01), que continua a ser obrigatório e único!
    @Column(name = "set_id", nullable = false, unique = true)
    private String setId;

    private String name; // Ex: ROMANCE DAWN

    private String releaseDate;
    private Integer totalCards;

    @OneToMany(mappedBy = "cardSet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CardVariant> variants = new ArrayList<>();

    public CardSet() {}

    // --- GETTERS E SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSetId() { return setId; }
    public void setSetId(String setId) { this.setId = setId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public Integer getTotalCards() { return totalCards; }
    public void setTotalCards(Integer totalCards) { this.totalCards = totalCards; }

    public List<CardVariant> getVariants() { return variants; }
    public void setVariants(List<CardVariant> variants) { this.variants = variants; }
}