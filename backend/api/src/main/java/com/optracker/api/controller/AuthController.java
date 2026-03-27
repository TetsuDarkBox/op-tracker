package com.optracker.api.controller;

import com.optracker.api.dto.LoginRequestDTO;
import com.optracker.api.dto.LoginResponseDTO;
import com.optracker.api.dto.UserResponseDTO;
import com.optracker.api.dto.RegisterRequest;
import com.optracker.api.entity.User;
import com.optracker.api.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody RegisterRequest request) {
        // 1. O Service cria o utilizador completo na BD
        User user = userService.registerNewUser(
                request.username(),
                request.email(),
                request.password()
        );

        // 2. Convertemos manualmente para o DTO de resposta (Segurança Ativa)
        UserResponseDTO response = new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getProfile().getDisplayName(),
                user.getAddress().getCity(),
                user.getAddress().getCountry(),
                user.getStats().getPositiveEvaluations(),
                user.getStats().getMemberSince()
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = userService.authenticate(request.username(), request.password());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Ligação estabelecida! 🏴‍☠️");
    }
}