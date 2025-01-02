package com.habittracker.repository;

import com.habittracker.model.TestHabit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestHabitRepository extends JpaRepository<TestHabit, Long> {
}