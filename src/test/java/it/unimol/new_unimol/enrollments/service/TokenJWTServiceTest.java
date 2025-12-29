package it.unimol.new_unimol.enrollments.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class TokenJWTServiceTest {

    @Autowired
    private TokenJWTService tokenService;

    @Test
    void testIsTokenValid() {
        String invalidToken = "invalid.token.value";

        assertThrows(Exception.class, () -> {
            tokenService.isTokenValid(invalidToken);
        });
    }

    @Test
    void testExtractUserIdFromInvalidToken() {
        assertThrows(Exception.class, () -> {
            tokenService.extractUserId("invalid");
        });
    }

    @Test
    void testExtractUsernameFromInvalidToken() {
        assertThrows(Exception.class, () -> {
            tokenService.extractUsername("invalid");
        });
    }
}
