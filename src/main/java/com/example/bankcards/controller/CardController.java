package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    /**
     * Просмотр своих карт с пагинацией и сортировкой.
     * Пример:
     * GET /api/cards/my?page=0&size=10&sort=id,desc
     */
    @GetMapping("/my")
    public ResponseEntity<Page<CardResponse>> getMyCards(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort
    ) {
        User user = userService.getByUsername(userDetails.getUsername());

        Pageable pageable = PageRequest.of(
                page,
                size,
                parseSort(sort)
        );

        return ResponseEntity.ok(cardService.getMyCardsDto(user, pageable));
    }

    /**
     * Перевод между своими картами.
     */
    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody TransferRequest request
    ) {
        User user = userService.getByUsername(userDetails.getUsername());
        cardService.transferBetweenOwnCards(
                request.getFromCardId(),
                request.getToCardId(),
                request.getAmount(),
                user
        );
        return ResponseEntity.ok("Перевод успешно выполнен");
    }

    /**
     * Баланс по конкретной карте.
     */
    @GetMapping("/{cardId}/balance")
    public ResponseEntity<BigDecimal> getBalance(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cardId
    ) {
        User user = userService.getByUsername(userDetails.getUsername());
        return ResponseEntity.ok(cardService.getBalance(cardId, user));
    }

    /**
     * sort ожидаем как: "field,asc" или "field,desc".
     * Если sort пустой — сортируем по id ASC.
     */
    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "id");
        }

        String[] parts = sort.split(",");
        String field = parts[0].trim();

        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())) {
            direction = Sort.Direction.DESC;
        }

        return Sort.by(direction, field);
    }
}
