package uk.gov.hmcts.reform.dg.docassembly.rest;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


class WelcomeResourceTest {

    private final WelcomeResource welcomeResource = new WelcomeResource();

    @Test
    void testEndPointResponseCode() {
        ResponseEntity<Map<String,String>> responseEntity = welcomeResource.welcome();

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void testEndpointResponseMessage() {
        ResponseEntity<Map<String,String>> responseEntity = welcomeResource.welcome();

        Map<String,String> expectedResponse = new HashMap<>();
        expectedResponse.put("message","Welcome to Document Assembly API!");

        String cacheHeader = responseEntity.getHeaders().getCacheControl();

        assertNotNull(responseEntity);
        assertEquals("no-cache",cacheHeader);
        assertEquals(expectedResponse, responseEntity.getBody());
    }
}