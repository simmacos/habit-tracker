package com.habittracker.service;

import com.habittracker.exception.ResourceNotFoundException;
import com.habittracker.model.Habit;
import com.habittracker.model.User;
import com.habittracker.repository.HabitRepository;
import com.habittracker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;


@Service
@Transactional
public class HabitService {
    private static final Logger logger = LoggerFactory.getLogger(HabitService.class);

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private UserRepository userRepository;

    // Create
    public Habit createHabit(Long userId, Habit habit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        habit.setUser(user);
        habit.setCreatedAt(LocalDateTime.now());
        habit.setLastModified(LocalDateTime.now());
        habit.setIsActive(true);

        logger.info("Creating new habit for user {}: {}", userId, habit.getName());
        return habitRepository.save(habit);
    }

    // Il resto dei metodi rimane invariato
    public List<Habit> getUserHabits(Long userId, boolean includeHobbies) {
        // Verifica che l'utente esista
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        if (includeHobbies) {
            return habitRepository.findByUserIdAndIsActiveTrue(userId);
        }
        return habitRepository.findByUserIdAndIsHobbyFalseAndIsActiveTrue(userId);
    }

    public Habit getHabitById(Long habitId, Long userId) {
        return habitRepository.findById(habitId)
                .filter(habit -> habit.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Habit not found"));
    }

    // Update
    public Habit updateHabit(Long habitId, Long userId, Habit updatedHabit) {
        Habit habit = getHabitById(habitId, userId);

        habit.setName(updatedHabit.getName());
        habit.setDescription(updatedHabit.getDescription());
        habit.setSchedule(updatedHabit.getSchedule());
        habit.setIsHobby(updatedHabit.getIsHobby());
        habit.setLastModified(LocalDateTime.now());

        return habitRepository.save(habit);
    }

    // Delete (soft delete)
    public void deleteHabit(Long habitId, Long userId) {
        Habit habit = getHabitById(habitId, userId);
        habit.setIsActive(false);
        habit.setLastModified(LocalDateTime.now());
        habitRepository.save(habit);
    }
}