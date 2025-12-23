package com.example.bankcards.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CardResponseDTO {
    private Long id;
    private String maskedNumber;
    private String ownerName;
    private LocalDate expiryDate;
    private String status;
    private BigDecimal balance;

    public CardResponseDTO() {}

    // Гетеры
    public Long getId() { return id; }
    public String getMaskedNumber() { return maskedNumber; }
    public String getOwnerName() { return ownerName; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public String getStatus() { return status; }
    public BigDecimal getBalance() { return balance; }

    // Сеторы
    public void setId(Long id) { this.id = id; }
    public void setMaskedNumber(String maskedNumber) { this.maskedNumber = maskedNumber; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public void setStatus(String status) { this.status = status; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}