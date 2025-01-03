package com.habittracker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long id;

    @Column(name = "google_id", unique = true, nullable = false)
    @JsonProperty("googleId")
    private String googleId;

    @Column(unique = true, nullable = false)
    @JsonProperty("email")
    private String email;

    @JsonProperty("name")
    private String name;

    @Column(name = "picture_url")
    @JsonProperty("pictureUrl")
    private String pictureUrl;

    @Column(name = "created_at")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @Column(name = "last_login")
    @JsonProperty("lastLogin")
    private LocalDateTime lastLogin;

    @Column(name = "is_active")
    @JsonProperty("isActive")
    private Boolean isActive = true;
}
