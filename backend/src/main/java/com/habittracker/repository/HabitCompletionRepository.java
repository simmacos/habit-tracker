package com.habittracker.repository;

import com.habittracker.model.HabitCompletion;
import com.habittracker.model.HabitCompletionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HabitCompletionRepository extends JpaRepository<HabitCompletion, HabitCompletionId> {
    // Trova i completamenti di un habit in un range di date
    List<HabitCompletion> findByHabitIdAndIdCompletionDateBetween(
            Long habitId,
            LocalDate startDate,
            LocalDate endDate
    );

    // Trova i completamenti di un habit per una data specifica
    Optional<HabitCompletion> findByHabitIdAndIdCompletionDate(
            Long habitId,
            LocalDate date
    );

    // Trova tutti i completamenti di oggi per un utente
    @Query("SELECT hc FROM HabitCompletion hc WHERE hc.habit.user.id = :userId " +
            "AND hc.id.completionDate = :date")
    List<HabitCompletion> findTodayCompletionsByUserId(
            @Param("userId") Long userId,
            @Param("date") LocalDate date
    );
}