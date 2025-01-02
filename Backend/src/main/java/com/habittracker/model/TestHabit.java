package com.habittracker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_habits")
@Getter
@Setter
@ToString
public class TestHabit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @Column(name = "created_at")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @Column(name = "is_active")
    @JsonProperty("isActive")
    private Boolean isActive;
}
