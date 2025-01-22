package com.habittracker.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    @JsonProperty("description")
    private String description;

    @Column(name = "is_hobby", nullable = false)
    @JsonProperty("isHobby")
    private Boolean isHobby = false;

    @JsonProperty("schedule")
    @Column(nullable = false, length = 7)
    @Size(min = 7, max = 7, message = "Schedule must be exactly 7 characters long")
    @Pattern(regexp = "[01]{7}", message = "Schedule must contain only 0s and 1s")
    private String schedule;

    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @Column(name = "last_modified", nullable = false)
    @JsonProperty("lastModified")
    private LocalDateTime lastModified;

    @Column(name = "is_active", nullable = false)
    @JsonProperty("isActive")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "habit")
    @JsonManagedReference
    @JsonProperty("completions")
    private List<HabitCompletion> completions;

    @JsonProperty("completedToday")
    @Transient
    private Boolean completedToday;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
        normalizeSchedule();
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastModified = LocalDateTime.now();
        normalizeSchedule();
    }

    private void normalizeSchedule() {
        if (this.schedule != null) {
            if (this.schedule.length() > 7) {
                this.schedule = this.schedule.substring(0, 7);
            } else if (this.schedule.length() < 7) {
                this.schedule = String.format("%-7s", this.schedule).replace(' ', '0');
            }
        }
    }

    public Boolean isCompletedToday() {
        LocalDate today = LocalDate.now();
        return completions != null &&
                completions.stream()
                        .anyMatch(c -> c.getId().getCompletionDate().equals(today));
    }

    @JsonProperty("completedToday")
    public Boolean getCompletedToday() {
        return isCompletedToday();
    }
}