package com.optracker.api.service;

import com.optracker.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test") // <-- Isto diz ao Spring para ler o application-test.properties
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void deveRetornarErroQuandoUsernameJaExiste() {
        // Regista o primeiro
        userService.registerNewUser("Nami", "nami@weather.com", "pass123");

        // Tenta registar o segundo com o mesmo nome
        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerNewUser("Nami", "outronami@weather.com", "pass123");
        });
    }
}