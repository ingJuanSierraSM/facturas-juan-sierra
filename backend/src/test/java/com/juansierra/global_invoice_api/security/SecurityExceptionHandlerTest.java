package com.juansierra.global_invoice_api.security;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

@ExtendWith(MockitoExtension.class)
class SecurityExceptionHandlerTest {

    private final SecurityExceptionHandler securityExceptionHandler = new SecurityExceptionHandler();

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Test
    void shouldReturnUnauthorizedWithoutErrorDispatch() throws Exception {
        securityExceptionHandler.commence(request, response, new BadCredentialsException("Token invalido"));

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void shouldReturnForbiddenWithoutErrorDispatch() throws Exception {
        securityExceptionHandler.handle(request, response, new AccessDeniedException("Acceso denegado"));

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response, never()).sendError(anyInt(), anyString());
    }
}
