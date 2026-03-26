package com.optracker.api.repository;

import com.optracker.api.entity.User;
import com.optracker.api.entity.UserProfile;
import com.optracker.api.entity.UserAddress;
import com.optracker.api.entity.UserStats;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test") // <-- Isto diz ao Spring para ler o application-test.properties
@DataJpaTest // Configura uma BD em memória leve para testes rápidos
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void deveSalvarUserComTodasAsSubclasses() {
        // Criar a estrutura
        User user = new User();
        user.setUsername("Robin");
        user.setEmail("robin@ohara.com");
        user.setPassword("12345");

        user.setProfile(new UserProfile());
        user.setAddress(new UserAddress());
        user.setStats(new UserStats());

        // Salvar
        User savedUser = userRepository.save(user);

        // Verificar se os IDs foram gerados para todos os módulos
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getProfile().getId()).isNotNull();
        assertThat(savedUser.getAddress().getId()).isNotNull();
        assertThat(savedUser.getStats().getId()).isNotNull();
    }
}