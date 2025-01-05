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

import java.util.List;

@RestController
@RequestMapping("/api/habits")
public class HabitController {
    private static final Logger logger = LoggerFactory.getLogger(HabitController.class);

    @Autowired
    private HabitService habitService;

    @Autowired
    private UserRepository userRepository;  // Aggiunto per getUserIdFromAuth

    @GetMapping
    public ResponseEntity<List<Habit>> getUserHabits(
            Authentication authentication,
            @RequestParam(defaultValue = "false") boolean includeHobbies) {
        Long userId = getUserIdFromAuth(authentication);
        logger.info("Fetching habits for user {}, includeHobbies: {}", userId, includeHobbies);
        List<Habit> habits = habitService.getUserHabits(userId, includeHobbies);
        return ResponseEntity.ok(habits);
    }

    @PostMapping
    public ResponseEntity<Habit> createHabit(
            Authentication authentication,
            @Valid @RequestBody Habit habit) {  // Aggiunto @Valid
        Long userId = getUserIdFromAuth(authentication);
        logger.info("Creating new habit for user {}: {}", userId, habit.getName());
        Habit createdHabit = habitService.createHabit(userId, habit);
        return ResponseEntity.ok(createdHabit);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Habit> getHabit(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = getUserIdFromAuth(authentication);
        logger.info("Fetching habit {} for user {}", id, userId);
        Habit habit = habitService.getHabitById(id, userId);
        return ResponseEntity.ok(habit);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Habit> updateHabit(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody Habit habit) {  // Aggiunto @Valid
        Long userId = getUserIdFromAuth(authentication);
        logger.info("Updating habit {} for user {}", id, userId);
        Habit updatedHabit = habitService.updateHabit(id, userId, habit);
        return ResponseEntity.ok(updatedHabit);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHabit(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = getUserIdFromAuth(authentication);
        logger.info("Deleting habit {} for user {}", id, userId);
        habitService.deleteHabit(id, userId);
        return ResponseEntity.ok().build();
    }

    protected Long getUserIdFromAuth(Authentication authentication) {
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
}