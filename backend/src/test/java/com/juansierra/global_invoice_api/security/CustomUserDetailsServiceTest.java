package com.juansierra.global_invoice_api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.juansierra.global_invoice_api.entity.User;
import com.juansierra.global_invoice_api.enums.UserRole;
import com.juansierra.global_invoice_api.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void shouldMapApplicationUserToDisabledAuditorDetails() {
        CustomUserDetailsService userDetailsService = new CustomUserDetailsService(userRepository);
        User user = User.builder()
                .username("auditor")
                .passwordHash("bcrypt-hash")
                .role(UserRole.AUDITOR)
                .enabled(false)
                .build();
        when(userRepository.findByUsername("auditor")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("auditor");

        assertThat(result.getUsername()).isEqualTo("auditor");
        assertThat(result.getPassword()).isEqualTo("bcrypt-hash");
        assertThat(result.isEnabled()).isFalse();
        assertThat(result.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_AUDITOR");
    }

    @Test
    void shouldRejectUnknownUser() {
        CustomUserDetailsService userDetailsService = new CustomUserDetailsService(userRepository);
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Usuario no encontrado");
    }
}
