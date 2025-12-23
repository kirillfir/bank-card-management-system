package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class CardService {

    private final CardRepository cardRepository;

    // Внедряем репозиторий через конструктор
    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    // --- ЛОГИКА АДМИНИСТРАТОРА ---

    @Transactional
    public Card createCard(Card card) {
        // Здесь будет логика генерации номера и шифрования через util
        return cardRepository.save(card);
    }

    @Transactional
    public void updateCardStatus(Long cardId, String status) {
        cardRepository.findById(cardId).ifPresent(card -> {
            card.setStatus(status);
            cardRepository.save(card);
        });
    }

    @Transactional
    public void deleteCard(Long cardId) {
        cardRepository.deleteById(cardId);
    }

    // --- ЛОГИКА ПОЛЬЗОВАТЕЛЯ ---

    // Поиск своих карт с пагинацией (Требование ТЗ)
    public Page<Card> getMyCards(User user, Pageable pageable) {
        return cardRepository.findAllByUser(user, pageable);
    }

    // Просмотр баланса конкретной карты
    public BigDecimal getBalance(Long cardId, User user) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Карта не найдена"));

        // Проверяем, что карта принадлежит именно этому пользователю
        if (!card.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Доступ запрещен: это не ваша карта");
        }

        return card.getBalance();
    }

    // Перевод между своими картами (Требование ТЗ)
    @Transactional
    public void transferBetweenOwnCards(Long fromId, Long toId, BigDecimal amount, User user) {
        Card fromCard = cardRepository.findById(fromId)
                .orElseThrow(() -> new RuntimeException("Карта отправителя не найдена"));
        Card toCard = cardRepository.findById(toId)
                .orElseThrow(() -> new RuntimeException("Карта получателя не найдена"));

        // Проверка владения обеими картами
        if (!fromCard.getUser().getId().equals(user.getId()) || !toCard.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Обе карты должны принадлежать вам");
        }

        // Проверка баланса (используем compareTo для BigDecimal)
        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Недостаточно средств");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }
}