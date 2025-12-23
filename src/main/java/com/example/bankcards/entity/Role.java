package com.example.bankcards.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // Сюда будем записывать "ROLE_USER" или "ROLE_ADMIN"

    public Role() {}

    // Геттеры
    public Long getId() {return id;}
    public String getName() {return name;}

    //  Сеттеры
    public void setId(Long id) {this.id = id;}
    public void setName(String name) {this.name = name;}

    // Методы сравнения
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(id, role.id) && Objects.equals(name, role.name);
    }

    @Override
    public int hashCode() {return Objects.hash(id, name);}
}