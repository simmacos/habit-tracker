package com.habittracker.controller;

import com.habittracker.model.HabitCompletion;
import com.habittracker.service.HabitCompletionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/habits/{habitId}/completions")
public class HabitCompletionController {
    private static final Logger logger = LoggerFactory.getLogger(HabitCompletionController.class);

    @Autowired
    private HabitCompletionService completionService;

    @Autowired
    private HabitController habitController;  // Per riutilizzare getUserIdFromAuth

    // Toggle completion per una data specifica
    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Boolean>> toggleCompletion(
            Authentication authentication,
            @PathVariable Long habitId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long userId = habitController.getUserIdFromAuth(authentication);
        logger.info("Toggling completion for habit {} on date {} for user {}", habitId, date, userId);

        boolean isCompleted = completionService.toggleCompletion(habitId, userId, date);
        return ResponseEntity.ok(Map.of("completed", isCompleted));
    }

    // Ottieni lo streak corrente
    @GetMapping("/streak")
    public ResponseEntity<Map<String, Long>> getCurrentStreak(
            Authentication authentication,
            @PathVariable Long habitId) {
        Long userId = habitController.getUserIdFromAuth(authentication);
        logger.info("Getting current streak for habit {} for user {}", habitId, userId);

        long streak = completionService.getCurrentStreak(habitId, userId);
        return ResponseEntity.ok(Map.of("streak", streak));
    }

    // Ottieni i completamenti in un range di date
    @GetMapping("/range")
    public ResponseEntity<List<HabitCompletion>> getCompletionsForRange(
            Authentication authentication,
            @PathVariable Long habitId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = habitController.getUserIdFromAuth(authentication);
        logger.info("Getting completions for habit {} between {} and {} for user {}",
                habitId, startDate, endDate, userId);

        List<HabitCompletion> completions =
                completionService.getCompletionsForRange(habitId, userId, startDate, endDate);
        return ResponseEntity.ok(completions);
    }

    // Ottieni tutti i completamenti di oggi per l'utente
    @GetMapping("/today")
    public ResponseEntity<List<HabitCompletion>> getTodayCompletions(
            Authentication authentication) {
        Long userId = habitController.getUserIdFromAuth(authentication);
        logger.info("Getting today's completions for user {}", userId);

        List<HabitCompletion> completions = completionService.getTodayCompletions(userId);
        return ResponseEntity.ok(completions);
    }
}
