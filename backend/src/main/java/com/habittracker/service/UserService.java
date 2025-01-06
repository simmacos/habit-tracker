package com.habittracker.service;

import com.habittracker.model.User;
import com.habittracker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@Transactional  // Aggiungiamo questa annotation importante
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    public User findOrCreateUser(String googleId, String email, String name, String pictureUrl) {
        logger.info("findOrCreateUser chiamato per googleId: {} email: {}", googleId, email);

        User user = userRepository.findByGoogleId(googleId);

        if (user == null) {
            logger.info("Utente non trovato, creazione nuovo utente");
            user = new User();
            user.setGoogleId(googleId);
            user.setEmail(email);
            user.setCreatedAt(LocalDateTime.now());
            user.setIsActive(true);
        } else {
            logger.info("Utente esistente trovato con ID: {}", user.getId());
        }

        user.setName(name);
        user.setPictureUrl(pictureUrl);
        user.setLastLogin(LocalDateTime.now());

        try {
            User savedUser = userRepository.save(user);
            logger.info("Utente salvato con successo, ID: {}", savedUser.getId());
            return savedUser;
        } catch (Exception e) {
            logger.error("Errore nel salvare l'utente: ", e);
            throw e;
        }
    }
}
