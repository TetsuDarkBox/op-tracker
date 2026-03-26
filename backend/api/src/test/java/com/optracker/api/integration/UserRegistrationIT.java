package com.optracker.api.integration;

import com.optracker.api.repository.UserRepository;
import com.optracker.api.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // <-- Isto diz ao Spring para ler o application-test.properties
@Transactional // Limpa a BD depois de cada teste
public class UserRegistrationIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void fluxoCompleto_DeveBarrarEmailDuplicado() throws Exception {
        String userJson = """
                {
                    "username": "Robin",
                    "email": "robin@ohara.com",
                    "password": "password123"
                }
                """;

        // 1. Primeiro registo (Sucesso)
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated());

        // 2. Segundo registo com mesmo email (Falha Profissional)
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists()); // Verifica se o GlobalExceptionHandler funcionou
    }
}