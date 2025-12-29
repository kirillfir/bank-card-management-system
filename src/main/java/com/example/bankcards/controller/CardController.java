package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;
    private final UserService userService;

    public CardController(CardService cardService, UserService userService) {
        this.cardService = cardService;
        this.userService = userService;
    }

    @GetMapping("/my")
    public ResponseEntity<Page<CardResponse>> getMyCards(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable
    ) {
        User user = userService.getByUsername(userDetails.getUsername());
        return ResponseEntity.ok(cardService.getMyCardsDto(user, pageable));
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody TransferRequest request
    ) {
        User user = userService.getByUsername(userDetails.getUsername());
        cardService.transferBetweenOwnCards(request.getFromCardId(), request.getToCardId(), request.getAmount(), user);
        return ResponseEntity.ok("Перевод успешно выполнен");
    }

    @GetMapping("/{cardId}/balance")
    public ResponseEntity<BigDecimal> getBalance(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cardId
    ) {
        User user = userService.getByUsername(userDetails.getUsername());
        return ResponseEntity.ok(cardService.getBalance(cardId, user));
    }
}
