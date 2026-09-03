package com.juansierra.global_invoice_api.service.interfaces;

import com.juansierra.global_invoice_api.dto.request.LoginRequest;
import com.juansierra.global_invoice_api.dto.response.LoginResponse;

public interface AuthenticationService {

    LoginResponse login(LoginRequest request);
}
