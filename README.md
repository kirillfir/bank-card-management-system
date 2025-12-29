# Bank Card Management System

REST API для управления банковскими картами: создание/управление картами админом, просмотр карт пользователем и переводы между своими картами.

## Стек
- Java 17
- Spring Boot 3
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Liquibase
- Swagger UI / OpenAPI (springdoc)

---

## Функциональность

### Роли
- **ADMIN**
    - Создаёт карту пользователю (опциональный стартовый баланс)
    - Меняет статус карты (ACTIVE / BLOCKED / EXPIRED)
    - Устанавливает баланс (DEV/TEST)
    - Удаляет карту

- **USER**
    - Просматривает свои карты (пагинация + сортировка)
    - Смотрит баланс по карте
    - Делает перевод между своими картами

### Атрибуты карты
- Номер карты хранится в БД **в зашифрованном виде**
- В API отдаётся **маска**: `**** **** **** 1234`
- Владелец, срок действия, статус, баланс

---

## Запуск проекта

### 1) Поднять PostgreSQL
Самый простой способ — через Docker:

```bash
docker run --name bank-postgres -e POSTGRES_DB=bank -e POSTGRES_USER=bank_user -e POSTGRES_PASSWORD=bank_pass -p 5432:5432 -d postgres:15

### 2) Настроить application.properties

Укажи свои параметры подключения к БД (пример):
spring.datasource.url=jdbc:postgresql://localhost:5432/bank
spring.datasource.username=bank_user
spring.datasource.password=bank_pass

spring.jpa.hibernate.ddl-auto=validate
spring.liquibase.enabled=true

# JWT
bank.jwt.secret=mySecretKeyForBankProjectWhichIsLongEnoughAndStrong
bank.jwt.expirationMs=86400000

### 3) Запуск

mvn clean test
mvn spring-boot:run
Swagger / OpenAPI

Swagger UI:

http://localhost:8080/swagger-ui.html

OpenAPI JSON:

http://localhost:8080/v3/api-docs

Файл спецификации:

docs/openapi.yaml

Авторизация (JWT)

Зарегистрируй пользователя:

POST /api/auth/signup

Получи токен:

POST /api/auth/signin
Ответ содержит token

Основные эндпоинты
Auth

POST /api/auth/signup

POST /api/auth/signin

User

GET /api/cards/my?page=0&size=10&sort=id,desc

GET /api/cards/{cardId}/balance

POST /api/cards/transfer

Пример тела для перевода:

{
  "fromCardId": 1,
  "toCardId": 3,
  "amount": 200.00
}

Admin (требуется ROLE_ADMIN)

POST /api/admin/cards/create/{userId} (+ optional body)
Пример:
{
  "initialBalance": 500.00
}

PATCH /api/admin/cards/{cardId}/status?status=ACTIVE

PATCH /api/admin/cards/{cardId}/balance?balance=1000

DELETE /api/admin/cards/{cardId}

Тесты

Запуск всех тестов:

mvn test

Примечания

Переводы разрешены только между своими картами

Переводы возможны только если обе карты ACTIVE и не просрочены

Номер карты в БД не хранится в открытом виде