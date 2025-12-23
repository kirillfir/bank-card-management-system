package com.example.bankcards.util;

public class CardUtils {

    private CardUtils() {
    }

    // Метод для маскирования номера карты: **** **** **** 1234
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}