package com.example.bankcards.service;

//Добавляем логер для отслеживания переводов (решил не перегружать проект сущностями)
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BankException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    // --- ЛОГИКА АДМИНИСТРАТОРА ---

    @Transactional
    public Card createCardForUser(Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new BankException("Пользователь для привязки карты не найден"));

        Card card = new Card();

        // 1. Генерируем случайный номер
        String rawNumber = generateRandomCardNumber();

        // 2. Используем SecurityUtils для шифрования (как мы писали ранее)
        // В твоем коде было CardUtils.encryptNumber, лучше использовать SecurityUtils.encrypt
        card.setCardNumber(com.example.bankcards.util.SecurityUtils.encrypt(rawNumber));

        // 3. ЗАПОЛНЯЕМ ОБЯЗАТЕЛЬНЫЕ ПОЛЯ ИЗ ТВОЕЙ СУЩНОСТИ
        card.setUser(owner);
        card.setOwnerName(owner.getUsername()); // По ТЗ: владелец
        card.setExpiryDate(java.time.LocalDate.now().plusYears(5)); // Срок действия 5 лет

        card.setBalance(BigDecimal.ZERO);
        card.setStatus("ACTIVE");

        return cardRepository.save(card);
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

    // --- ЛОГИКА ПОЛЬЗОВАТЕЛЯ ---

    public Page<Card> getMyCards(User user, Pageable pageable) {
        // Здесь мы получаем сущности из базы.
        // Маскирование будет происходить в DTO перед отправкой пользователю.
        return cardRepository.findAllByUser(user, pageable);
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
        // 1. Логируем неудачу
        log.info("Пользователь ID: {} инициировал перевод. С карты {} на карту {} сумма {}",
                user.getId(), fromId, toId, amount);
        Card fromCard = cardRepository.findById(fromId)
                .orElseThrow(() -> new BankException("Карта отправителя не найдена"));
        Card toCard = cardRepository.findById(toId)
                .orElseThrow(() -> new BankException("Карта получателя не найдена"));

        // ТЗ: Переводы только между СВОИМИ картами
        if (!fromCard.getUser().getId().equals(user.getId()) || !toCard.getUser().getId().equals(user.getId())) {
            log.warn("ПОДОЗРИТЕЛЬНАЯ АКТИВНОСТЬ: Пользователь {} пытался перевести деньги между чужими картами", user.getId());
            throw new BankException("Обе карты должны принадлежать вам");
        }

        // Проверка баланса
        if (fromCard.getBalance().compareTo(amount) < 0) {
            log.info("Отказ в переводе: недостаточно средств на карте {}", fromId);
            throw new BankException("Недостаточно средств для перевода");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
        // 2. Логируем успех
        log.info("УСПЕХ: Перевод выполнен. С карты {} на карту {} списано {}", fromId, toId, amount);
    }

    // Вспомогательный метод для генерации номера карты
    private String generateRandomCardNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}