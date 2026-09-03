package com.juansierra.global_invoice_api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.juansierra.global_invoice_api.entity.User;
import com.juansierra.global_invoice_api.enums.UserRole;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

class JwtServiceTest {

    private static final String SECRET = "global-invoice-academic-secret-change-me-2026";

    @Test
    void shouldGenerateTokenForAuthenticatedUser() {
        JwtService jwtService = new JwtService(SECRET, 3_600_000L);
        User user = user("operator", UserRole.OPERATOR);
        UserDetails userDetails = userDetails("operator", "OPERATOR");

        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractUsername(token)).isEqualTo("operator");
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void shouldRejectExpiredToken() {
        JwtService jwtService = new JwtService(SECRET, -1L);
        String token = jwtService.generateToken(user("auditor", UserRole.AUDITOR));

        assertThatThrownBy(() -> jwtService.extractUsername(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void shouldRejectTokenForDifferentUser() {
        JwtService jwtService = new JwtService(SECRET, 3_600_000L);
        String token = jwtService.generateToken(user("operator", UserRole.OPERATOR));

        assertThat(jwtService.isTokenValid(token, userDetails("auditor", "AUDITOR"))).isFalse();
    }

    private User user(String username, UserRole role) {
        return User.builder().username(username).role(role).build();
    }

    private UserDetails userDetails(String username, String role) {
        return org.springframework.security.core.userdetails.User.withUsername(username)
                .password("hash")
                .roles(role)
                .build();
    }
}
