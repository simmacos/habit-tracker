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
import java.util.Optional;

@Service
@Transactional
public class HabitCompletionService {
    private static final Logger logger = LoggerFactory.getLogger(HabitCompletionService.class);

    @Autowired
    private HabitCompletionRepository completionRepository;

    @Autowired
    private HabitService habitService;

    // Toggle completion for a habit
    public boolean toggleCompletion(Long habitId, Long userId, LocalDate date) {
        // Verifica che l'habit appartenga all'utente
        Habit habit = habitService.getHabitById(habitId, userId);

        // Cerca se esiste già un completamento
        HabitCompletionId completionId = new HabitCompletionId(habitId, date);
        boolean exists = completionRepository.existsById(completionId);

        if (exists) {
            // Se esiste, rimuovi il completamento
            completionRepository.deleteById(completionId);
            logger.info("Removed completion for habit {} on {}", habitId, date);
            return false;
        } else {
            // Se non esiste, crea nuovo completamento
            HabitCompletion completion = new HabitCompletion();
            completion.setId(completionId);
            completion.setHabit(habit);
            completionRepository.save(completion);
            logger.info("Added completion for habit {} on {}", habitId, date);
            return true;
        }
    }

    public long getCurrentStreak(Long habitId, Long userId) {
        Habit habit = habitService.getHabitById(habitId, userId);
        String schedule = habit.getSchedule();
        LocalDate currentDate = LocalDate.now();
        long streak = 0;
        boolean foundBreak = false;

        logger.info("Calculating streak for habit {} with schedule: {}", habitId, schedule);

        while (!foundBreak) {
            int dayOfWeek = currentDate.getDayOfWeek().getValue() % 7; // 0=Dom, 1=Lun, ..., 6=Sab
            boolean isActiveDay = schedule.charAt(dayOfWeek) == '1';

            if (isActiveDay) {
                Optional<HabitCompletion> completion = completionRepository.findByHabitIdAndIdCompletionDate(
                        habitId,
                        currentDate
                );

                logger.debug("Checking {} (day {}): Active={}, Completed={}",
                        currentDate, dayOfWeek, isActiveDay, completion.isPresent());

                if (completion.isPresent()) {
                    streak++;
                } else {
                    // Controlla se è il primo giorno attivo non completato
                    if (currentDate.isEqual(LocalDate.now())) {
                        currentDate = currentDate.minusDays(1);
                        continue;
                    }
                    foundBreak = true;
                }
            }

            // Interrompi se raggiungi 1 anno indietro (prevenzione loop infinito)
            if (currentDate.isBefore(LocalDate.now().minusYears(1))) {
                logger.warn("Streak calculation reached 1 year limit for habit {}", habitId);
                break;
            }

            currentDate = currentDate.minusDays(1);
        }

        logger.info("Final streak count for habit {}: {}", habitId, streak);
        return streak;
    }

    // Get completions for a date range
    public List<HabitCompletion> getCompletionsForRange(Long habitId, Long userId,
                                                        LocalDate startDate, LocalDate endDate) {
        habitService.getHabitById(habitId, userId); // Verifica che l'habit appartenga all'utente
        return completionRepository.findByHabitIdAndIdCompletionDateBetween(
                habitId, startDate, endDate);
    }

    // Get today's completions for user
    public List<HabitCompletion> getTodayCompletions(Long userId) {
        return completionRepository.findTodayCompletionsByUserId(userId, LocalDate.now());
    }
}
