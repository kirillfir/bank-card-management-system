package com.example.bankcards.controller;

import com.example.bankcards.dto.AdminCreateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/admin/cards")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCardController {

    private final CardService cardService;

    public AdminCardController(CardService cardService) {
        this.cardService = cardService;
    }

    /**
     * Создать карту пользователю.
     * initialBalance — опционально (если не передать — будет 0).
     */
    @PostMapping("/create/{userId}")
    public ResponseEntity<Card> createCard(
            @PathVariable Long userId,
            @Valid @RequestBody(required = false) AdminCreateCardRequest request
    ) {
        BigDecimal initialBalance = (request == null) ? null : request.getInitialBalance();
        return ResponseEntity.ok(cardService.createCardForUser(userId, initialBalance));
    }

    /**
     * DEV/TEST endpoint: установить баланс карты вручную (только ADMIN).
     */
    @PatchMapping("/{cardId}/balance")
    public ResponseEntity<Void> setBalance(
            @PathVariable Long cardId,
            @RequestParam BigDecimal balance
    ) {
        cardService.setBalance(cardId, balance);
        return ResponseEntity.ok().build();
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
