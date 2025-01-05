package com.habittracker.repository;

import com.habittracker.model.Habit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HabitRepository extends JpaRepository<Habit, Long> {
    // Trova tutti gli habits di un utente (attivi)
    List<Habit> findByUserIdAndIsActiveTrue(Long userId);

    // Trova tutti gli hobby di un utente (attivi)
    List<Habit> findByUserIdAndIsHobbyTrueAndIsActiveTrue(Long userId);

    // Trova tutti gli habits non hobby di un utente (attivi)
    List<Habit> findByUserIdAndIsHobbyFalseAndIsActiveTrue(Long userId);

    // Cerca habits per nome (per funzionalità di ricerca)
    List<Habit> findByUserIdAndNameContainingAndIsActiveTrue(Long userId, String name);
}