package com.example.bankcards.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Base64;

public class SecurityUtils {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private SecurityUtils() {}

    // Хеширует пароль
    public static String hashPassword(String rawPassword) {
        return PASSWORD_ENCODER.encode(rawPassword);
    }

    // Проверяет пароль (нужен для логики аутентификации)
    public static boolean matches(String rawPassword, String encodedPassword) {
        return PASSWORD_ENCODER.matches(rawPassword, encodedPassword);
    }

    // Шифрует номер карты (Base64)
    public static String encrypt(String data) {
        if (data == null) return null;
        return Base64.getEncoder().encodeToString(data.getBytes());
    }

    // Расшифровывает номер карты с защитой от ошибок
    public static String decrypt(String encryptedData) {
        if (encryptedData == null) return null;
        try {
            return new String(Base64.getDecoder().decode(encryptedData));
        } catch (Exception e) {
            return "ОШИБКА_ДЕКОДИРОВАНИЯ";
        }
    }
}