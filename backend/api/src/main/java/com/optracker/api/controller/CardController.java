package com.optracker.api.controller;

import com.optracker.api.entity.Card;
import com.optracker.api.entity.CardVariant;
import com.optracker.api.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
@CrossOrigin(origins = "http://localhost:5173")
public class CardController {

    @Autowired
    private CardRepository cardRepository;

    @GetMapping("/code/{code}")
    public ResponseEntity<Map<String, Object>> getCardByCode(@PathVariable String code) {
        return cardRepository.findByCode(code)
                .map(card -> ResponseEntity.ok(buildCardMap(card)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Map<String, Object>> getAllCards() {
        List<Card> cards = cardRepository.findAllWithVariants();
        List<Map<String, Object>> response = new ArrayList<>();

        for (Card card : cards) {
            response.add(buildCardMap(card));
        }

        return response;
    }

    // No CardController.java (método buildCardMap)

    private Map<String, Object> buildCardMap(Card card) {
        Map<String, Object> map = new HashMap<>();

        map.put("id", card.getId());
        map.put("code", card.getCode());
        map.put("name", card.getName());
        map.put("type", card.getType());
        map.put("rarity", card.getRarity());

        map.put("cost", card.getCost());
        map.put("power", card.getPower());
        map.put("life", card.getLife());
        map.put("counter", card.getCounter());
        map.put("color", card.getColor());
        map.put("attribute", card.getAttribute());
        map.put("subTypes", card.getSubTypes());
        map.put("keywords", card.getKeywords());

        map.put("effect", card.getEffect());
        map.put("triggerEffect", card.getTriggerEffect());

        map.put("blockNumber", card.getBlockNumber());
        map.put("attributeIconUrl", card.getAttributeIconUrl());
        map.put("colorIconUrl", card.getColorIconUrl());
        map.put("blockIconUrl", card.getBlockIconUrl());

        // 📌 Pasta baseada no código (ex: OP05-034 -> OP-05)
        String setId = extractSetFolderFromCode(card.getCode());

        List<String> imageNames = new ArrayList<>();
        List<CardVariant> variants = card.getVariants();

        if (variants != null && !variants.isEmpty()) {
            for (CardVariant variant : variants) {
                String fileName = getFileNameFromUrl(variant.getImageUrl());
                if (fileName != null && !fileName.isEmpty()) {
                    imageNames.add(fileName);
                }
            }
        }

        map.put("setId", setId);
        map.put("images", imageNames);

        return map;
    }

    private String getFileNameFromUrl(String url) {
        if (url == null || url.trim().isEmpty()) return null;
        String cleanUrl = url.contains("?") ? url.substring(0, url.indexOf("?")) : url;
        return cleanUrl.substring(cleanUrl.lastIndexOf('/') + 1);
    }

    private String extractSetFolderFromCode(String code) {
        if (code != null && code.contains("-")) {
            String prefix = code.split("-")[0].toUpperCase();
            if (prefix.matches("^[A-Z]+[0-9]+$")) {
                String letters = prefix.replaceAll("[0-9]", "");
                String numbers = prefix.replaceAll("[^0-9]", "");
                return letters + "-" + numbers;
            }
            return prefix;
        }
        return "Promos";
    }
}