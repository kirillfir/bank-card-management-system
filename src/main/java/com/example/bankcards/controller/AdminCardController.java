package com.example.bankcards.controller;

import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/cards")
@PreAuthorize("hasRole('ADMIN')") // Магия Spring Security: пустит только админа
public class AdminCardController {

    private final CardService cardService;

    public AdminCardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping("/create/{userId}")
    public ResponseEntity<Card> createCard(@PathVariable Long userId) {
        // Вызываем твой метод из CardService
        return ResponseEntity.ok(cardService.createCardForUser(userId));
    }

    @PatchMapping("/{cardId}/status")
    public ResponseEntity<Void> changeStatus(@PathVariable Long cardId, @RequestParam String status) {
        cardService.updateCardStatus(cardId, status);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.ok().build();
    }
}