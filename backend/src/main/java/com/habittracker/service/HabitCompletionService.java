package com.habittracker.service;

import com.habittracker.model.Habit;
import com.habittracker.model.HabitCompletion;
import com.habittracker.model.HabitCompletionId;
import com.habittracker.repository.HabitCompletionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class HabitCompletionService {
    private static final Logger logger = LoggerFactory.getLogger(HabitCompletionService.class);

    @Autowired
    private HabitCompletionRepository completionRepository;

    @Autowired
    private HabitService habitService;

    public boolean toggleCompletion(Long habitId, Long userId, LocalDate date) {
        // Verifica che l'habit appartenga all'utente
        Habit habit = habitService.getHabitById(habitId, userId);

        // Usa il metodo corretto del repository
        boolean exists = completionRepository.existsByHabitIdAndId_CompletionDate(habitId, date);

        if (exists) {
            completionRepository.deleteById(new HabitCompletionId(habitId, date));
            logger.info("Rimosso completamento per habit {} il {}", habitId, date);
            return false;
        } else {
            HabitCompletion completion = new HabitCompletion();
            completion.setId(new HabitCompletionId(habitId, date));
            completion.setHabit(habit);
            completionRepository.save(completion);
            logger.info("Aggiunto completamento per habit {} il {}", habitId, date);
            return true;
        }
    }

    public long getCurrentStreak(Long habitId, Long userId) {
        Habit habit = habitService.getHabitById(habitId, userId);
        String schedule = habit.getSchedule();
        LocalDate currentDate = LocalDate.now();
        long streak = 0;
        boolean foundBreak = false;
        int consecutiveDaysLimit = 365;

        logger.debug("Calcolo streak per habit {} con schedule: {}", habitId, schedule);

        while (!foundBreak && streak < consecutiveDaysLimit) {
            int dayOfWeek = currentDate.getDayOfWeek().getValue() % 7;
            boolean isActiveDay = schedule.charAt(dayOfWeek) == '1';

            if (isActiveDay) {
                boolean isCompleted = completionRepository.existsByHabitIdAndId_CompletionDate(habitId, currentDate);

                if (isCompleted) {
                    streak++;
                } else {
                    foundBreak = true;
                }
            }
            currentDate = currentDate.minusDays(1);
        }

        logger.info("Streak finale per habit {}: {}", habitId, streak);
        return streak;
    }

    public List<HabitCompletion> getCompletionsForRange(Long habitId, Long userId,
                                                        LocalDate startDate, LocalDate endDate) {
        habitService.getHabitById(habitId, userId); // Verifica l'appartenenza
        // Usa il metodo corretto del repository
        return completionRepository.findByHabitIdAndId_CompletionDateBetween(habitId, startDate, endDate);
    }

    public List<HabitCompletion> getTodayCompletions(Long userId) {
        return completionRepository.findTodayCompletionsByUserId(userId, LocalDate.now());
    }
}