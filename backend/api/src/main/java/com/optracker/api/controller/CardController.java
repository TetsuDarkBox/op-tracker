package com.optracker.api.controller;

import com.optracker.api.entity.Card;
import com.optracker.api.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
                .map(card -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("code", card.getCode());
                    response.put("name", card.getName());
                    response.put("cost", card.getCost());
                    response.put("power", card.getPower());
                    response.put("attribute", card.getAttribute());
                    response.put("effect", card.getEffect());
                    response.put("color", card.getColor());

                    // Descobrir o Set ID para construir o caminho da pasta
                    String setId = "Unknown";
                    if (card.getVariants() != null && !card.getVariants().isEmpty()) {
                        if (card.getVariants().get(0).getCardSet() != null) {
                            setId = card.getVariants().get(0).getCardSet().getSetId();
                        }
                    }

                    response.put("setId", setId);
                    // Nome do ficheiro limpo (igual ao que o Downloader usou)
                    // Garante que termina em .png
                    String fileName = card.getCode().replaceAll("[\\\\/:*?\"<>|]", "-") + ".png";
                    response.put("imageName", fileName);

                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}