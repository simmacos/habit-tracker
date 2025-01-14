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
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class HabitService {
    private static final Logger logger = LoggerFactory.getLogger(HabitService.class);

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private UserRepository userRepository;

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

    public List<Habit> getUserHabits(Long userId, boolean includeHobbies) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        // Ottieni tutti gli habits attivi dell'utente
        List<Habit> allHabits = habitRepository.findByUserIdAndIsActiveTrue(userId);

        // Ottieni il giorno corrente (0 = Domenica, 1 = Lunedì, ..., 6 = Sabato)
        int currentDay = LocalDate.now().getDayOfWeek().getValue() % 7;

        return allHabits.stream()
                .filter(habit -> {
                    // Se è un hobby e non dobbiamo includere hobby, salta
                    if (!includeHobbies && habit.getIsHobby()) {
                        return false;
                    }

                    // Se è un hobby, mostralo sempre
                    if (habit.getIsHobby()) {
                        return true;
                    }

                    // Per gli habits normali, controlla lo schedule
                    String schedule = habit.getSchedule();
                    if (schedule == null || schedule.length() != 7) {
                        logger.warn("Invalid schedule for habit id {}: {}", habit.getId(), schedule);
                        return false;
                    }

                    // Controlla se l'habit è schedulato per oggi
                    return schedule.charAt(currentDay) == '1';
                })
                .collect(Collectors.toList());
    }

    public Habit getHabitById(Long habitId, Long userId) {
        return habitRepository.findById(habitId)
                .filter(habit -> habit.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Habit not found"));
    }

    public Habit updateHabit(Long habitId, Long userId, Habit updatedHabit) {
        Habit habit = getHabitById(habitId, userId);

        habit.setName(updatedHabit.getName());
        habit.setDescription(updatedHabit.getDescription());
        habit.setSchedule(updatedHabit.getSchedule());
        habit.setIsHobby(updatedHabit.getIsHobby());
        habit.setLastModified(LocalDateTime.now());

        return habitRepository.save(habit);
    }

    public void deleteHabit(Long habitId, Long userId) {
        Habit habit = getHabitById(habitId, userId);
        habit.setIsActive(false);
        habit.setLastModified(LocalDateTime.now());
        habitRepository.save(habit);
    }
}