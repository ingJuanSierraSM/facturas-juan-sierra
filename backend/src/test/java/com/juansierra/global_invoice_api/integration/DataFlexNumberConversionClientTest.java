package com.juansierra.global_invoice_api.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class DataFlexNumberConversionClientTest {

    private static final String SERVICE_URL = "https://dataflex.test/NumberConversion.wso";

    private MockRestServiceServer mockServer;
    private DataFlexNumberConversionClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        client = new DataFlexNumberConversionClient(restClientBuilder, SERVICE_URL);
    }

    @Test
    void shouldConvertAmountToWordsAndRemoveDollarLabel() {
        mockServer.expect(requestTo(SERVICE_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("SOAPAction", "http://www.dataaccess.com/webservicesserver/NumberToDollars"))
                .andExpect(content().string(containsString("<dNum>100.25</dNum>")))
                .andRespond(withSuccess(soapResponse("one hundred dollars and twenty five cents"), MediaType.TEXT_XML));

        String result = client.convertToWords(new BigDecimal("100.25"));

        assertThat(result).isEqualTo("one hundred and twenty five cents");
        mockServer.verify();
    }

    @Test
    void shouldThrowLegacyServiceExceptionWhenProviderRespondsWithError() {
        mockServer.expect(requestTo(SERVICE_URL))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> client.convertToWords(new BigDecimal("100.00")))
                .isInstanceOf(LegacyServiceException.class)
                .hasMessage("No fue posible convertir el total de la factura a letras");

        mockServer.verify();
    }

    private String soapResponse(String conversion) {
        return """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <NumberToDollarsResponse xmlns="http://www.dataaccess.com/webservicesserver/">
                      <NumberToDollarsResult>%s</NumberToDollarsResult>
                    </NumberToDollarsResponse>
                  </soap:Body>
                </soap:Envelope>
                """.formatted(conversion);
    }
}
