package com.juansierra.global_invoice_api.integration;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Component
public class DataFlexNumberConversionClient implements NumberConversionClient {

    private static final String SERVICE_NAMESPACE = "http://www.dataaccess.com/webservicesserver/";
    private static final String SOAP_ACTION = SERVICE_NAMESPACE + "NumberToDollars";

    private final RestClient restClient;
    private final String serviceUrl;

    public DataFlexNumberConversionClient(
            RestClient.Builder restClientBuilder,
            @Value("${integration.dataflex.number-conversion-url}") String serviceUrl
    ) {
        this.restClient = restClientBuilder.build();
        this.serviceUrl = serviceUrl;
    }

    @Override
    public String convertToWords(BigDecimal amount) {
        try {
            String responseBody = restClient.post()
                    .uri(serviceUrl)
                    .contentType(MediaType.TEXT_XML)
                    .header("SOAPAction", SOAP_ACTION)
                    .body(buildSoapRequest(amount))
                    .retrieve()
                    .body(String.class);

            if (responseBody == null || responseBody.isBlank()) {
                throw new LegacyServiceException("La respuesta del servicio DataFlex esta vacia");
            }

            return removeDollarLabel(extractConversionResult(responseBody));
        } catch (RestClientException | IOException | ParserConfigurationException | SAXException exception) {
            throw new LegacyServiceException("No fue posible convertir el total de la factura a letras", exception);
        }
    }

    private String buildSoapRequest(BigDecimal amount) {
        return """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <NumberToDollars xmlns="http://www.dataaccess.com/webservicesserver/">
                      <dNum>%s</dNum>
                    </NumberToDollars>
                  </soap:Body>
                </soap:Envelope>
                """.formatted(amount.toPlainString());
    }

    private String extractConversionResult(String responseBody)
            throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(responseBody)));
        NodeList resultNodes = document.getElementsByTagNameNS(SERVICE_NAMESPACE, "NumberToDollarsResult");
        if (resultNodes.getLength() != 1) {
            throw new LegacyServiceException("La respuesta del servicio DataFlex no contiene una conversion valida");
        }

        String conversion = resultNodes.item(0).getTextContent();
        if (conversion == null || conversion.isBlank()) {
            throw new LegacyServiceException("La respuesta del servicio DataFlex no contiene texto convertido");
        }

        return conversion;
    }

    private String removeDollarLabel(String conversion) {
        return conversion.replaceAll("(?i)\\s+dollars?\\b", "")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
