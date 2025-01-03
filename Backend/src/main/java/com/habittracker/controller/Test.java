package com.habittracker.controller;

import com.habittracker.model.TestHabit;
import com.habittracker.repository.TestHabitRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;  // Aggiungi questo import
import java.util.List;
import java.util.Optional;

@RestController
@Slf4j  // Aggiungi questa annotazione
public class Test {

    @Autowired
    private TestHabitRepository repository;

    @GetMapping("/api/public")
    public List<TestHabit> tutti() {
        List<TestHabit> habits = repository.findAll();
        System.out.println("Trovati " + habits.size() + " habits");
        habits.forEach(System.out::println);
        return habits;
    }

    @GetMapping("/id:{id}")
    public Optional<TestHabit> byId(@PathVariable Long id){
        return repository.findById(id);
    }
}
