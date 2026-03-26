package com.optracker.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_stats")
public class UserStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Reputação (Cardmarket Style) ---
    private Integer positiveEvaluations = 0;
    private Integer neutralEvaluations = 0;
    private Integer negativeEvaluations = 0;

    // --- Performance de Envio ---
    private Double cancellationRate = 0.0; // % de trocas canceladas
    private String avgResponseTime;        // Ex: "Sub 1 hour"
    private String avgShippingTime;        // Ex: "2 days"

    // --- Atividade na Plataforma ---
    private Integer totalTradesCompleted = 0;
    private Integer totalDecksShared = 0;
    private Integer totalCardsInCollection = 0;

    private LocalDateTime memberSince = LocalDateTime.now();
    private LocalDateTime lastTradeAt;

    public UserStats() {}

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPositiveEvaluations() {
        return positiveEvaluations;
    }

    public void setPositiveEvaluations(Integer positiveEvaluations) {
        this.positiveEvaluations = positiveEvaluations;
    }

    public Integer getNeutralEvaluations() {
        return neutralEvaluations;
    }

    public void setNeutralEvaluations(Integer neutralEvaluations) {
        this.neutralEvaluations = neutralEvaluations;
    }

    public Integer getNegativeEvaluations() {
        return negativeEvaluations;
    }

    public void setNegativeEvaluations(Integer negativeEvaluations) {
        this.negativeEvaluations = negativeEvaluations;
    }

    public Double getCancellationRate() {
        return cancellationRate;
    }

    public void setCancellationRate(Double cancellationRate) {
        this.cancellationRate = cancellationRate;
    }

    public String getAvgResponseTime() {
        return avgResponseTime;
    }

    public void setAvgResponseTime(String avgResponseTime) {
        this.avgResponseTime = avgResponseTime;
    }

    public String getAvgShippingTime() {
        return avgShippingTime;
    }

    public void setAvgShippingTime(String avgShippingTime) {
        this.avgShippingTime = avgShippingTime;
    }

    public Integer getTotalTradesCompleted() {
        return totalTradesCompleted;
    }

    public void setTotalTradesCompleted(Integer totalTradesCompleted) {
        this.totalTradesCompleted = totalTradesCompleted;
    }

    public Integer getTotalDecksShared() {
        return totalDecksShared;
    }

    public void setTotalDecksShared(Integer totalDecksShared) {
        this.totalDecksShared = totalDecksShared;
    }

    public Integer getTotalCardsInCollection() {
        return totalCardsInCollection;
    }

    public void setTotalCardsInCollection(Integer totalCardsInCollection) {
        this.totalCardsInCollection = totalCardsInCollection;
    }

    public LocalDateTime getMemberSince() {
        return memberSince;
    }

    public void setMemberSince(LocalDateTime memberSince) {
        this.memberSince = memberSince;
    }

    public LocalDateTime getLastTradeAt() {
        return lastTradeAt;
    }

    public void setLastTradeAt(LocalDateTime lastTradeAt) {
        this.lastTradeAt = lastTradeAt;
    }
}