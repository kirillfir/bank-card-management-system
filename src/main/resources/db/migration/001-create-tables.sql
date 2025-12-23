-- liquibase formatted sql

-- changeset kuznetsov:1
-- 1. Сначала создаем таблицу ролей, так как на неё будут ссылаться юзеры
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE -- ROLE_USER, ROLE_ADMIN
);

-- changeset kuznetsov:2
-- 2. Создаем таблицу пользователей с привязкой к роли
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role_id BIGINT,
    CONSTRAINT fk_users_roles FOREIGN KEY (role_id) REFERENCES roles (id)
);

-- changeset kuznetsov:3
-- 3. Создаем таблицу карт
CREATE TABLE cards (
    id BIGSERIAL PRIMARY KEY,
    card_number VARCHAR(255) NOT NULL UNIQUE,
    owner_name VARCHAR(255) NOT NULL,
    expiry_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    user_id BIGINT,
    CONSTRAINT fk_cards_users FOREIGN KEY (user_id) REFERENCES users (id)
);

-- changeset kuznetsov:4
-- 4. Сразу добавим дефолтные роли, чтобы потом не делать это вручную
INSERT INTO roles (name) VALUES ('ROLE_USER');
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');