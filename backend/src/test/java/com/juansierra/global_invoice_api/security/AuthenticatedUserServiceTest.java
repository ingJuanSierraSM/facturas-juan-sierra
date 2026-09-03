package com.juansierra.global_invoice_api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.juansierra.global_invoice_api.entity.User;
import com.juansierra.global_invoice_api.enums.UserRole;
import com.juansierra.global_invoice_api.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class AuthenticatedUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnCurrentAuthenticatedUser() {
        AuthenticatedUserService authenticatedUserService = new AuthenticatedUserService(userRepository);
        User user = User.builder().username("operator").role(UserRole.OPERATOR).build();
        authenticateAs("operator", "ROLE_OPERATOR");
        when(userRepository.findByUsername("operator")).thenReturn(Optional.of(user));

        assertThat(authenticatedUserService.getCurrentUser()).isSameAs(user);
    }

    @Test
    void shouldRejectRequestWithoutAuthenticatedUser() {
        AuthenticatedUserService authenticatedUserService = new AuthenticatedUserService(userRepository);

        assertThatThrownBy(authenticatedUserService::getCurrentUser)
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class)
                .hasMessage("No existe un usuario autenticado");

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldRejectAuthenticatedUsernameNotFoundInDatabase() {
        AuthenticatedUserService authenticatedUserService = new AuthenticatedUserService(userRepository);
        authenticateAs("operator", "ROLE_OPERATOR");
        when(userRepository.findByUsername("operator")).thenReturn(Optional.empty());

        assertThatThrownBy(authenticatedUserService::getCurrentUser)
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Usuario autenticado no encontrado");
    }

    private void authenticateAs(String username, String authority) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                username,
                null,
                List.of(new SimpleGrantedAuthority(authority))
        ));
    }
}
