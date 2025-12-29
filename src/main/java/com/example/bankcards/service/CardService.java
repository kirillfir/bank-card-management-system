package com.example.bankcards.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BankException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;

@Service
public class CardService {

    private static final Logger log = LoggerFactory.getLogger(CardService.class);

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public CardService(CardRepository cardRepository, UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }

    // -----------------------
    // ADMIN логика
    // -----------------------

    @Transactional
    public Card createCardForUser(Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new BankException("Пользователь для привязки карты не найден"));

        Card card = new Card();

        String rawNumber = generateRandomCardNumber();
        card.setLast4(rawNumber.substring(rawNumber.length() - 4));
        card.setCardNumber(SecurityUtils.encrypt(rawNumber));

        card.setUser(owner);
        card.setOwnerName(owner.getUsername());
        card.setExpiryDate(LocalDate.now().plusYears(5));
        card.setBalance(BigDecimal.ZERO);
        card.setStatus("ACTIVE");

        return cardRepository.save(card);
    }

    @Transactional
    public CardResponse createCardForUserDto(Long userId) {
        return toResponse(createCardForUser(userId));
    }

    @Transactional
    public void updateCardStatus(Long cardId, String status) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new BankException("Карта не найдена"));
        card.setStatus(status);
        cardRepository.save(card);
    }

    @Transactional
    public void deleteCard(Long cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new BankException("Невозможно удалить: карта не найдена");
        }
        cardRepository.deleteById(cardId);
    }

    // -----------------------
    // USER логика
    // -----------------------

    public Page<CardResponse> getMyCardsDto(User user, Pageable pageable) {
        return cardRepository.findAllByUser(user, pageable).map(this::toResponse);
    }

    public BigDecimal getBalance(Long cardId, User user) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new BankException("Карта не найдена"));

        if (!card.getUser().getId().equals(user.getId())) {
            throw new BankException("Доступ запрещен: вы не владелец этой карты");
        }

        return card.getBalance();
    }

    @Transactional
    public void transferBetweenOwnCards(Long fromId, Long toId, BigDecimal amount, User user) {
        log.info("Пользователь ID: {} инициировал перевод. С карты {} на карту {} сумма {}",
                user.getId(), fromId, toId, amount);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankException("Сумма перевода должна быть больше 0");
        }

        Card fromCard = cardRepository.findById(fromId)
                .orElseThrow(() -> new BankException("Карта отправителя не найдена"));
        Card toCard = cardRepository.findById(toId)
                .orElseThrow(() -> new BankException("Карта получателя не найдена"));

        if (!fromCard.getUser().getId().equals(user.getId()) || !toCard.getUser().getId().equals(user.getId())) {
            log.warn("ПОДОЗРИТЕЛЬНАЯ АКТИВНОСТЬ: Пользователь {} пытался перевести деньги между чужими картами", user.getId());
            throw new BankException("Обе карты должны принадлежать вам");
        }

        if (!"ACTIVE".equals(fromCard.getStatus()) || !"ACTIVE".equals(toCard.getStatus())) {
            throw new BankException("Переводы возможны только между активными картами");
        }

        if (fromCard.getBalance().compareTo(amount) < 0) {
            log.info("Отказ в переводе: недостаточно средств на карте {}", fromId);
            throw new BankException("Недостаточно средств для перевода");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        log.info("УСПЕХ: Перевод выполнен. С карты {} на карту {} списано {}", fromId, toId, amount);
    }

    // -----------------------
    // DTO mapping
    // -----------------------
    private CardResponse toResponse(Card card) {
        CardResponse dto = new CardResponse();
        dto.setId(card.getId());
        dto.setMaskedNumber("**** **** **** " + card.getLast4());
        dto.setOwnerName(card.getOwnerName());
        dto.setExpiryDate(card.getExpiryDate());
        dto.setStatus(card.getStatus());
        dto.setBalance(card.getBalance());
        return dto;
    }

    private String generateRandomCardNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
