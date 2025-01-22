package com.habittracker.repository;

import com.habittracker.model.HabitCompletion;
import com.habittracker.model.HabitCompletionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface HabitCompletionRepository extends JpaRepository<HabitCompletion, HabitCompletionId> {

    // Corretto con la sintassi dell'ID embedded
    boolean existsByHabitIdAndId_CompletionDate(Long habitId, LocalDate date);

    // Corretto il nome del metodo
    List<HabitCompletion> findByHabitIdAndId_CompletionDateBetween(
            Long habitId,
            LocalDate startDate,
            LocalDate endDate
    );

    // Mantenuto come prima
    @Query("SELECT hc FROM HabitCompletion hc WHERE hc.habit.user.id = :userId AND hc.id.completionDate = :date")
    List<HabitCompletion> findTodayCompletionsByUserId(
            @Param("userId") Long userId,
            @Param("date") LocalDate date
    );
}