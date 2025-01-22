package com.habittracker.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "habit_completions")
@Getter
@Setter
public class HabitCompletion {
    @EmbeddedId
    private HabitCompletionId id;

    @ManyToOne
    @MapsId("habitId")
    @JoinColumn(name = "habit_id")
    @JsonBackReference
    private Habit habit;

    // Campo derivato per facilitare le query
    @Transient
    public LocalDate getCompletionDate() {
        return id != null ? id.getCompletionDate() : null;
    }
}