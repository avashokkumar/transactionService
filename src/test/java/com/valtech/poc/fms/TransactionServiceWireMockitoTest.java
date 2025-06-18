
package com.valtech.poc.fms;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.valtech.poc.dto.TransactionDTO;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
  "spring.main.allow-bean-definition-overriding=true"
})
@ActiveProfiles("test")
public class TransactionServiceWireMockitoTest {

    private static WireMockServer wireMockServer;

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceWireMockitoTest.class);

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start(); // 1. Start first!
        int port = wireMockServer.port(); // 2. Then fetch the assigned dynamic port
        logger.debug("WireMock started at port: " + port);
        configureFor("localhost", port);
        // 3. Update system properties
        System.setProperty("fraud_detection_api", "http://localhost:" + port);
        System.setProperty("fraud_detection_path", "/api/frauddetection/v1/detect");
    }

    @AfterAll
    static void stopWireMock() {
        logger.debug("in stopWireMock");
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void testValidTransaction() {
        stubFor(post(urlEqualTo("/api/frauddetection/v1/detect"))
                // .withRequestBody(matchingJsonPath("$.transactionId", equalTo("142")))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                                            {
                                "transactionId": "142",
                                "status": "A",
                                "riskScore": "101",
                                "riskReason": "Allowed"
                                }
                                                        """)));

        TransactionDTO request = new TransactionDTO();
        request.setTransFromAccNo(100000000l);
        request.setTransToAccNo(200000000l);
        request.setTransAmount(new BigDecimal(9));
        request.setSourceCountryCode("USA");
        request.setDestCountryCode("INR");
        request.setSourceIp("132.132.132.1");
        request.setTransStatus("pending");
        request.setTransactionType("SWIFT");
        request.setTransactionDate(LocalDateTime.now());
        final String url = "/api/transactionservice/v1/transfer";
        final String externalAPi = "/api/frauddetection/v1/detect";
        ResponseEntity<TransactionDTO> response = restTemplate.postForEntity(url,request, TransactionDTO.class);
        TransactionDTO tdtoResponse = response.getBody();
        logger.debug("response--" +tdtoResponse);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        if(tdtoResponse!= null)
           assertEquals("A", tdtoResponse.getTransStatus());
        verify(postRequestedFor(urlEqualTo(externalAPi)));
        logger.debug("Total requests received by WireMock: ", WireMock.getAllServeEvents().size());
        WireMock.getAllServeEvents()
                .forEach(event -> logger.debug(">>> Incoming request: " + event.getRequest().getBodyAsString()));
        WireMock.getAllServeEvents().forEach(e -> {
            logger.debug(">>> Received request: " + e.getRequest().getUrl());
            logger.debug(">>> Body: " + e.getRequest().getBodyAsString());
        });
    }
}
