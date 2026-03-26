package com.optracker.api.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    private String name;
    private String type;
    private String color;

    @Column(columnDefinition = "TEXT")
    private String effect;

    private String power;
    private String life;
    private String counter;
    private String attribute;
    private String subTypes;

    private String attributeIconUrl;
    private String blockIconUrl;
    private String colorIconUrl;
    private String blockNumber;
    private String cost;
    private String rarity;

    @Column(columnDefinition = "TEXT")
    private String triggerEffect;

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CardVariant> variants = new ArrayList<>();

    public Card() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getEffect() { return effect; }
    public void setEffect(String effect) { this.effect = effect; }

    public String getPower() { return power; }
    public void setPower(String power) { this.power = power; }

    public String getLife() { return life; }
    public void setLife(String life) { this.life = life; }

    public String getCounter() { return counter; }
    public void setCounter(String counter) { this.counter = counter; }

    public String getAttribute() { return attribute; }
    public void setAttribute(String attribute) { this.attribute = attribute; }

    public String getSubTypes() { return subTypes; }
    public void setSubTypes(String subTypes) { this.subTypes = subTypes; }

    public String getCost() { return cost; }
    public void setCost(String cost) { this.cost = cost; }

    public String getRarity() { return rarity; }
    public void setRarity(String rarity) { this.rarity = rarity; }

    public String getTriggerEffect() { return triggerEffect; }
    public void setTriggerEffect(String triggerEffect) { this.triggerEffect = triggerEffect; }

    public List<CardVariant> getVariants() { return variants; }
    public void setVariants(List<CardVariant> variants) { this.variants = variants; }

    public void addVariant(CardVariant variant) {
        variants.add(variant);
        variant.setCard(this);
    }

    @Column(name = "keywords")
    private String keywords; // Guardará algo como "On Play, Blocker"

    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }

    public String getAttributeIconUrl() {
        return attributeIconUrl;
    }

    public void setAttributeIconUrl(String attributeIconUrl) {
        this.attributeIconUrl = attributeIconUrl;
    }

    public String getBlockIconUrl() {
        return blockIconUrl;
    }

    public void setBlockIconUrl(String blockIconUrl) {
        this.blockIconUrl = blockIconUrl;
    }

    public String getColorIconUrl() {
        return colorIconUrl;
    }

    public void setColorIconUrl(String colorIconUrl) {
        this.colorIconUrl = colorIconUrl;
    }

    public String getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(String blockNumber) {
        this.blockNumber = blockNumber;
    }
}