package com.habittracker.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "google_id", unique = true, nullable = false)
    private String googleId;

    @Column(unique = true, nullable = false)
    private String email;

    private String name;

    @Column(name = "picture_url")
    private String pictureUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "user")
    @JsonManagedReference
    private List<Habit> habits;

    // Metodo per verificare se l'utente è attivo
    public boolean isActive() {
        return isActive != null && isActive;
    }

    // Metodo per ottenere l'identificatore univoco dell'utente
    public String getUsername() {
        return email;
    }
}