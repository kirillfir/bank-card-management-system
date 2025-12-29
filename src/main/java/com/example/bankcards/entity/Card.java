package com.example.bankcards.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Здесь хранится ЗАШИФРОВАННЫЙ номер (ciphertext).
     * Его нельзя отдавать наружу.
     */
    @Column(name = "card_number", nullable = false, unique = true)
    private String cardNumber;

    /**
     * Последние 4 цифры в открытом виде, чтобы делать маску.
     */
    @Column(name = "last4", nullable = false, length = 4)
    private String last4;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false)
    private String status; // ACTIVE, BLOCKED, EXPIRED

    @Column(nullable = false)
    private BigDecimal balance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Card() {}

    // --- getters ---
    public Long getId() { return id; }
    public String getCardNumber() { return cardNumber; }
    public String getLast4() { return last4; }
    public String getOwnerName() { return ownerName; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public String getStatus() { return status; }
    public BigDecimal getBalance() { return balance; }
    public User getUser() { return user; }

    // --- setters ---
    public void setId(Long id) { this.id = id; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public void setLast4(String last4) { this.last4 = last4; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public void setStatus(String status) { this.status = status; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public void setUser(User user) { this.user = user; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return Objects.equals(id, card.id) && Objects.equals(cardNumber, card.cardNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cardNumber);
    }
}
