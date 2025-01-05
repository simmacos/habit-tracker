package com.habittracker.model;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HabitCompletionId implements Serializable {
    private Long habitId;
    private LocalDate completionDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HabitCompletionId that = (HabitCompletionId) o;
        return Objects.equals(habitId, that.habitId) &&
                Objects.equals(completionDate, that.completionDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(habitId, completionDate);
    }
}