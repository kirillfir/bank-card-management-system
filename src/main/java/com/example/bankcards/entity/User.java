package com.example.bankcards.entity;

import jakarta.persistence.*;
import java.util.Objects;
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true) // Поле не может быть пустым и должно быть уникальным
    private String username;

    @Column(nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.EAGER) // EAGER значит, что роль подгрузится сразу вместе с юзером
    @JoinColumn(name = "role_id") // Имя колонки в таблице users, которая ссылается на roles
    private Role role;


    public User() {}

    // Гетары
    public Long getId() {return id;}
    public String getUsername() {return username;}
    public String getPassword() {return password;}
    public Role getRole() {return role;}

    // Сетары
    public void setId(Long id) {this.id = id;}
    public void setUsername(String username) {this.username = username;}
    public void setPassword(String password) {this.password = password;}
    public void setRole(Role role) {this.role = role;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }
}