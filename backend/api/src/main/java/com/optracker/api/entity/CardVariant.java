package com.optracker.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "card_variants")
public class CardVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;
    private String language;
    private String artStyle;

    // A nossa gaveta vazia pronta para os ilustradores no futuro!
    private String artist;

    @ManyToOne
    @JoinColumn(name = "card_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cardvariant_card"))
    @JsonIgnore
    private Card card;


    @ManyToOne
    @JoinColumn(name = "set_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cardvariant_cardset"))
    private CardSet cardSet;

    public CardVariant() {}

    // --- GETTERS E SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; } // Faltava-te o setter do ID!

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getArtStyle() { return artStyle; }
    public void setArtStyle(String artStyle) { this.artStyle = artStyle; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public Card getCard() { return card; }
    public void setCard(Card card) { this.card = card; }

    public CardSet getCardSet() { return cardSet; }
    public void setCardSet(CardSet cardSet) { this.cardSet = cardSet; }
}