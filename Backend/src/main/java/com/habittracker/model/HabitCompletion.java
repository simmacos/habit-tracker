package com.habittracker.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


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
}
