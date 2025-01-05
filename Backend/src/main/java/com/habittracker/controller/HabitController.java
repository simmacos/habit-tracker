package com.habittracker.controller;

import com.habittracker.model.Habit;
import com.habittracker.model.User;
import com.habittracker.service.HabitService;
import com.habittracker.repository.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/habits")
public class HabitController {
    private static final Logger logger = LoggerFactory.getLogger(HabitController.class);

    @Autowired
    private HabitService habitService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Habit>> getUserHabits(
            Authentication authentication,
            @RequestParam(defaultValue = "false") boolean includeHobbies) {
        Long userId = getUserIdFromAuth(authentication);
        List<Habit> habits = habitService.getUserHabits(userId, includeHobbies);
        return ResponseEntity.ok(habits);
    }

    @PostMapping
    public ResponseEntity<Habit> createHabit(
            Authentication authentication,
            @Valid @RequestBody Habit habit) {
        Long userId = getUserIdFromAuth(authentication);
        Habit createdHabit = habitService.createHabit(userId, habit);
        return ResponseEntity.ok(createdHabit);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Habit> getHabit(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = getUserIdFromAuth(authentication);
        Habit habit = habitService.getHabitById(id, userId);
        return ResponseEntity.ok(habit);
    }

    @GetMapping("/{id}/streak")
    public ResponseEntity<Map<String, Long>> getCurrentStreak(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = getUserIdFromAuth(authentication);
        Habit habit = habitService.getHabitById(id, userId);
        long streak = calculateStreak(habit);
        return ResponseEntity.ok(Map.of("streak", streak));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Habit> updateHabit(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody Habit habit) {
        Long userId = getUserIdFromAuth(authentication);
        Habit updatedHabit = habitService.updateHabit(id, userId, habit);
        return ResponseEntity.ok(updatedHabit);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHabit(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = getUserIdFromAuth(authentication);
        habitService.deleteHabit(id, userId);
        return ResponseEntity.ok().build();
    }

    Long getUserIdFromAuth(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof OAuth2User)) {
            throw new IllegalStateException("User not properly authenticated");
        }

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String googleId = oauth2User.getAttribute("sub");

        User user = userRepository.findByGoogleId(googleId);
        if (user == null) {
            throw new IllegalStateException("User not found in database");
        }

        return user.getId();
    }

    private long calculateStreak(Habit habit) {
        if (habit.getCompletions().isEmpty()) return 0;

        LocalDate today = LocalDate.now();
        LocalDate date = today;
        long streak = 0;

        while (true) {
            // Se è un hobby, conta ogni giorno
            if (habit.getIsHobby()) {
                if (hasCompletion(habit, date)) {
                    streak++;
                    date = date.minusDays(1);
                } else {
                    break;
                }
            } else {
                // Per habits normali, conta solo i giorni schedulati
                int dayOfWeek = date.getDayOfWeek().getValue() % 7; // 0-6 (Sunday-Saturday)
                if (habit.getSchedule().charAt(dayOfWeek) == '1') {
                    if (hasCompletion(habit, date)) {
                        streak++;
                        date = date.minusDays(1);
                    } else {
                        break;
                    }
                } else {
                    // Se il giorno non è schedulato, passa al giorno precedente
                    date = date.minusDays(1);
                }
            }
        }

        return streak;
    }

    private boolean hasCompletion(Habit habit, LocalDate date) {
        return habit.getCompletions().stream()
                .anyMatch(completion -> completion.getId().getCompletionDate().equals(date));
    }
}
