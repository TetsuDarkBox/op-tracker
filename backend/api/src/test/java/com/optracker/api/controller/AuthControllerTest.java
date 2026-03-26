package com.optracker.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.optracker.api.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // <-- Isto diz ao Spring para ler o application-test.properties
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve retornar 201 Created quando o registo é válido")
    void deveRegistrarComSucesso() throws Exception {
        String json = """
                {
                    "username": "Brook",
                    "email": "brook@laboon.com",
                    "password": "yohohoho-password"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("Brook"))
                .andExpect(jsonPath("$.password").doesNotExist()); // Segurança: a password não volta no JSON
    }
}