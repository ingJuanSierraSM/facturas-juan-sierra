package com.juansierra.global_invoice_api.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.juansierra.global_invoice_api.entity.User;
import com.juansierra.global_invoice_api.enums.UserRole;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/invoices"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectRequestWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/invoices").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowOperatorToCreateInvoice() throws Exception {
        mockMvc.perform(post("/api/v1/invoices")
                        .header("Authorization", authorizationFor("operator", UserRole.OPERATOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "invoiceNumber": "%s",
                                  "type": "NATIONAL",
                                  "subtotal": 100000
                                }
                                """.formatted("SECURITY-" + UUID.randomUUID())))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldRejectInvoiceCreationForAuditor() throws Exception {
        mockMvc.perform(post("/api/v1/invoices")
                        .header("Authorization", authorizationFor("auditor", UserRole.AUDITOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "invoiceNumber": "SECURITY-AUDITOR",
                                  "type": "NATIONAL",
                                  "subtotal": 100000
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowBothRolesToReadInvoices() throws Exception {
        mockMvc.perform(get("/api/v1/invoices")
                        .header("Authorization", authorizationFor("operator", UserRole.OPERATOR)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/invoices")
                        .header("Authorization", authorizationFor("auditor", UserRole.AUDITOR)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowAuditorToReadDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/invoices-by-type")
                        .header("Authorization", authorizationFor("auditor", UserRole.AUDITOR)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectDashboardForOperator() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/invoices-by-type")
                        .header("Authorization", authorizationFor("operator", UserRole.OPERATOR)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectDashboardWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/invoices-by-type"))
                .andExpect(status().isUnauthorized());
    }

    private String authorizationFor(String username, UserRole role) {
        User user = User.builder().username(username).role(role).build();
        return "Bearer " + jwtService.generateToken(user);
    }
}
