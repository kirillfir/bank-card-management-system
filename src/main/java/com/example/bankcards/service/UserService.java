package com.example.bankcards.service;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BankException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public User registerUser(User user) {
        // 1. Проверяем уникальность логина
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BankException("Пользователь с таким логином уже существует");
        }

        // 2. Хешируем пароль через нашу утилиту
        user.setPassword(SecurityUtils.hashPassword(user.getPassword()));

        // 3. Назначаем одну роль ROLE_USER по умолчанию
        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new BankException("Ошибка: Роль ROLE_USER не найдена"));

        // ВНИМАНИЕ: используем setRole (ед. число), так как у тебя в Entity так прописано
        user.setRole(defaultRole);

        return userRepository.save(user);
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BankException("Пользователь не найден: " + username));
    }
}