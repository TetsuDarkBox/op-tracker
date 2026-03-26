package com.optracker.api.controller;

import com.optracker.api.entity.Card;
import com.optracker.api.repository.CardRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@CrossOrigin(origins = "*") // Permite que o futuro Frontend comunique com o Backend
public class CardController {

    private final CardRepository cardRepository;

    public CardController(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    // 📝 MÉTODO: Devolve TODAS as cartas e as respetivas variantes (Cuidado, vai ser um ficheiro gigante!)
    @GetMapping
    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    // 📝 MÉTODO: Procura uma carta específica pelo seu Código exato (ex: OP01-001)
    @GetMapping("/{code}")
    public Card getCardByCode(@PathVariable String code) {
        return cardRepository.findByCode(code).orElse(null);
    }

    // 📝 MÉTODO: Procura todas as cartas de um Set específico (ex: OP-01) usando a nova arquitetura
    @GetMapping("/set/{setId}")
    public List<Card> getCardsBySet(@PathVariable String setId) {
        return cardRepository.findByVariantsCardSetSetId(setId); // 🆕 Atualizado!
    }
}