package com.example.bankcards.dto;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public class AdminCreateCardRequest {

    /**
     * Опциональный стартовый баланс.
     * Если null — будет 0.
     */
    @DecimalMin(value = "0.0", inclusive = true, message = "Начальный баланс не может быть отрицательным")
    private BigDecimal initialBalance;

    public AdminCreateCardRequest() {}

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }
}
