package com.habittracker.service;

import com.habittracker.model.User;
import com.habittracker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    public User findOrCreateUser(String googleId, String email, String name, String pictureUrl) {
        System.out.println("findOrCreateUser chiamato per email: " + email);

        try {
            User user = userRepository.findByGoogleId(googleId);
            if (user == null) {
                System.out.println("Creazione nuovo utente");
                user = new User();
                user.setGoogleId(googleId);
                user.setEmail(email);
                user.setCreatedAt(LocalDateTime.now());
                user.setIsActive(true);
            } else {
                System.out.println("Utente esistente trovato");
            }

            user.setName(name);
            user.setPictureUrl(pictureUrl);
            user.setLastLogin(LocalDateTime.now());

            User savedUser = userRepository.save(user);
            System.out.println("Utente salvato con ID: " + savedUser.getId());
            return savedUser;
        } catch (Exception e) {
            System.out.println("Errore nel service: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

}

