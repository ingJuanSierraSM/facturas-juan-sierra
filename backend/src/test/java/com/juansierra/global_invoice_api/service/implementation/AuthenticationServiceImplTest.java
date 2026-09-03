package com.juansierra.global_invoice_api.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.juansierra.global_invoice_api.dto.request.LoginRequest;
import com.juansierra.global_invoice_api.dto.response.LoginResponse;
import com.juansierra.global_invoice_api.entity.User;
import com.juansierra.global_invoice_api.enums.UserRole;
import com.juansierra.global_invoice_api.repository.UserRepository;
import com.juansierra.global_invoice_api.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationServiceImpl(authenticationManager, userRepository, jwtService);
    }

    @Test
    void shouldAuthenticateUserAndReturnToken() {
        LoginRequest request = new LoginRequest("operator", "secret");
        User user = User.builder().username("operator").role(UserRole.OPERATOR).build();

        when(userRepository.findByUsername("operator")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.getExpirationInSeconds()).thenReturn(3600L);

        LoginResponse response = authenticationService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(3600L);
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void shouldPropagateInvalidCredentials() {
        LoginRequest request = new LoginRequest("operator", "incorrect");
        BadCredentialsException exception = new BadCredentialsException("Credenciales invalidas");
        when(authenticationManager.authenticate(any())).thenThrow(exception);

        assertThatThrownBy(() -> authenticationService.login(request)).isSameAs(exception);

        verifyNoInteractions(userRepository, jwtService);
    }
}
