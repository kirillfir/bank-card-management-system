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

    /**
     * Создание карты с опциональным стартовым балансом.
     */
    @Transactional
    public Card createCardForUser(Long userId, BigDecimal initialBalance) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new BankException("Пользователь для привязки карты не найден"));

        if (initialBalance != null && initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BankException("Начальный баланс не может быть отрицательным");
        }

        Card card = new Card();

        String rawNumber = generateRandomCardNumber();
        card.setLast4(rawNumber.substring(rawNumber.length() - 4));
        card.setCardNumber(SecurityUtils.encrypt(rawNumber));

        card.setUser(owner);
        card.setOwnerName(owner.getUsername());
        card.setExpiryDate(LocalDate.now().plusYears(5));
        card.setBalance(initialBalance == null ? BigDecimal.ZERO : initialBalance);
        card.setStatus("ACTIVE");

        return cardRepository.save(card);
    }

    /**
     * DEV/TEST: вручную установить баланс (только ADMIN через контроллер).
     */
    @Transactional
    public void setBalance(Long cardId, BigDecimal balance) {
        if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BankException("Баланс должен быть >= 0");
        }

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new BankException("Карта не найдена"));

        card.setBalance(balance);
        cardRepository.save(card);
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
        log.info("Пользователь {} инициировал перевод: from={} to={} amount={}",
                user.getUsername(), fromId, toId, amount);

        if (fromId == null || toId == null) {
            throw new BankException("fromCardId и toCardId обязательны");
        }
        if (fromId.equals(toId)) {
            throw new BankException("Нельзя переводить на ту же самую карту");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankException("Сумма перевода должна быть больше 0");
        }

        Card fromCard = cardRepository.findById(fromId)
                .orElseThrow(() -> new BankException("Карта отправителя не найдена"));
        Card toCard = cardRepository.findById(toId)
                .orElseThrow(() -> new BankException("Карта получателя не найдена"));

        // Только свои карты
        if (!fromCard.getUser().getId().equals(user.getId()) || !toCard.getUser().getId().equals(user.getId())) {
            throw new BankException("Обе карты должны принадлежать вам");
        }

        // Проверяем срок действия по expiry_date
        if (fromCard.getExpiryDate().isBefore(java.time.LocalDate.now())) {
            throw new BankException("Карта отправителя просрочена");
        }
        if (toCard.getExpiryDate().isBefore(java.time.LocalDate.now())) {
            throw new BankException("Карта получателя просрочена");
        }

        // Только ACTIVE
        String fromStatus = fromCard.getStatus() == null ? "" : fromCard.getStatus().trim();
        String toStatus = toCard.getStatus() == null ? "" : toCard.getStatus().trim();

        if (!"ACTIVE".equalsIgnoreCase(fromStatus) || !"ACTIVE".equalsIgnoreCase(toStatus)) {
            throw new BankException("Переводы возможны только между активными картами");
        }


        // Баланс
        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new BankException("Недостаточно средств для перевода");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        log.info("Перевод успешен: from={} to={} amount={}", fromId, toId, amount);
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
