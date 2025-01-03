package com.habittracker.repository;

import com.habittracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByGoogleId(String googleId);
    User findByEmail(String email);
}
