package com.habittracker.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "habits")
@Getter
@Setter
public class Habit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @JsonProperty("name")
    private String name;

    @Column(columnDefinition = "TEXT")
    @JsonProperty("description")
    private String description;

    @Column(name = "is_hobby")
    @JsonProperty("isHobby")
    private Boolean isHobby = false;

    @JsonProperty("schedule")
    private String schedule;

    @Column(name = "created_at")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @Column(name = "last_modified")
    @JsonProperty("lastModified")
    private LocalDateTime lastModified;

    @Column(name = "is_active")
    @JsonProperty("isActive")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "habit")
    @JsonManagedReference
    @JsonProperty("completions")
    private List<HabitCompletion> completions;

    @JsonProperty("completedToday")
    @Transient // Non salvato nel DB, calcolato al volo
    private Boolean completedToday;

    public Boolean isCompletedToday() {
        LocalDate today = LocalDate.now();
        return completions != null &&
                completions.stream()
                        .anyMatch(c -> c.getId().getCompletionDate().equals(today));
    }

    // Aggiungi questo metodo per assicurarti che il campo completedToday
    // venga sempre valorizzato durante la serializzazione JSON
    @JsonProperty("completedToday")
    public Boolean getCompletedToday() {
        return isCompletedToday();
    }
}
