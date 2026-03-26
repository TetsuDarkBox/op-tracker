package com.optracker.api.service;

import com.optracker.api.dto.LoginResponse; // Verifica se o pacote é minúsculo 'dto'
import com.optracker.api.config.JwtService;
import com.optracker.api.entity.*;
import com.optracker.api.repository.UserRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final MessageSource messageSource;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService; // <--- FALTAVA ESTA LINHA AQUI!

    public UserService(UserRepository userRepository,
                       MessageSource messageSource,
                       BCryptPasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.messageSource = messageSource;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService; // Agora esta atribuição funciona
    }

    @Transactional
    public User registerNewUser(String username, String email, String rawPassword) {
        Locale locale = LocaleContextHolder.getLocale();

        if (userRepository.existsByUsername(username)) {
            String msg = messageSource.getMessage("user.username.exists", null, locale);
            throw new IllegalArgumentException(msg);
        }

        if (userRepository.existsByEmail(email)) {
            String msg = messageSource.getMessage("user.email.exists", null, locale);
            throw new IllegalArgumentException(msg);
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));

        user.setProfile(new UserProfile());
        user.getProfile().setDisplayName(username);
        user.setAddress(new UserAddress());
        user.setStats(new UserStats());

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse authenticate(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(
                        messageSource.getMessage("user.not.found", null, LocaleContextHolder.getLocale())
                ));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException(
                    messageSource.getMessage("auth.password.invalid", null, LocaleContextHolder.getLocale())
            );
        }

        // O erro "Cannot resolve symbol" vai desaparecer agora:
        String token = jwtService.generateToken(user.getUsername());
        return new LoginResponse(token, user.getUsername());
    }
}