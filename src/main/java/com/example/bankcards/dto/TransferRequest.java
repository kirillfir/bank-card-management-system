package com.example.bankcards.dto;

import java.math.BigDecimal;

public class TransferRequest {
    private Long fromCardId;
    private Long toCardId;
    private BigDecimal amount;

    public TransferRequest() {}

    // Геторы
    public Long getFromCardId() { return fromCardId; }
    public Long getToCardId() { return toCardId; }
    public BigDecimal getAmount() { return amount; }

    //Сеторы
    public void setFromCardId(Long fromCardId) { this.fromCardId = fromCardId; }
    public void setToCardId(Long toCardId) { this.toCardId = toCardId; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}