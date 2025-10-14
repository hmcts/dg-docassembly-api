package uk.gov.hmcts.reform.dg.docassembly.functional;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.dg.docassembly.testutil.ExtendedCcdHelper;
import uk.gov.hmcts.reform.dg.docassembly.testutil.TestUtil;
import uk.gov.hmcts.reform.dg.docassembly.testutil.ToggleProperties;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class SecureDocumentConversionScenarios extends BaseTest {

    @Value("${test.url}")
    private String testUrl;

    private RequestSpecification request;
    private RequestSpecification cdamRequest;
    private RequestSpecification unAuthenticatedRequest;

    @Autowired
    public SecureDocumentConversionScenarios(
            TestUtil testUtil,
            ToggleProperties toggleProperties,
            ExtendedCcdHelper extendedCcdHelper
    ) {
        super(testUtil, toggleProperties, extendedCcdHelper);
    }

    @BeforeEach
    void setupRequestSpecification() {
        request = testUtil
                .authRequest()
                .baseUri(testUrl)
                .contentType(APPLICATION_JSON_VALUE);

        cdamRequest = testUtil
            .cdamAuthRequest()
            .baseUri(testUrl)
            .contentType(APPLICATION_JSON_VALUE);

        unAuthenticatedRequest = testUtil
                .unAuthenticatedRequest()
                .baseUri(testUrl)
                .contentType(APPLICATION_JSON_VALUE);
    }

    @Test
    void testPDFConversionWithWordDocument() throws IOException {
        assumeTrue(toggleProperties.isEnableSecureDocumentConversionEndpoint());
        String newDocId = extendedCcdHelper.uploadSecureDOCDocumentAndReturnUrl();
        Response response = createAndProcessSecureDocRequest(newDocId);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testPDFConversionWithDocx() throws IOException {
        assumeTrue(toggleProperties.isEnableSecureDocumentConversionEndpoint());
        String newDocId = extendedCcdHelper.uploadSecureDocxDocumentAndReturnUrl();
        Response response = createAndProcessSecureDocRequest(newDocId);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testPDFConversionWithPptx() throws IOException {
        assumeTrue(toggleProperties.isEnableSecureDocumentConversionEndpoint());
        String newDocId = extendedCcdHelper.uploadSecurePptxDocumentAndReturnUrl();
        Response response = createAndProcessSecureDocRequest(newDocId);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testPDFConversionWithPPT() throws IOException {
        assumeTrue(toggleProperties.isEnableSecureDocumentConversionEndpoint());
        String newDocId = extendedCcdHelper.uploadSecurePptDocumentAndReturnUrl();
        Response response = createAndProcessSecureDocRequest(newDocId);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testPDFConversionWithXlsx() throws IOException {
        assumeTrue(toggleProperties.isEnableSecureDocumentConversionEndpoint());
        String newDocId = extendedCcdHelper.uploadSecureXlsxDocumentAndReturnUrl();
        Response response = createAndProcessSecureDocRequest(newDocId);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testPDFConversionWithXLS() throws IOException {
        assumeTrue(toggleProperties.isEnableSecureDocumentConversionEndpoint());
        String newDocId = extendedCcdHelper.uploadSecureXLSDocumentAndReturnUrl();
        Response response = createAndProcessSecureDocRequest(newDocId);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testPDFConversionWithRTF() throws IOException {
        assumeTrue(toggleProperties.isEnableSecureDocumentConversionEndpoint());
        String newDocId = extendedCcdHelper.uploadSecureRTFDocumentAndReturnUrl();
        Response response = createAndProcessSecureDocRequest(newDocId);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testPDFConversionWithTXT() throws IOException {
        assumeTrue(toggleProperties.isEnableSecureDocumentConversionEndpoint());
        String newDocId = extendedCcdHelper.uploadSecureTXTDocumentAndReturnUrl();
        Response response = createAndProcessSecureDocRequest(newDocId);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testFailedConversion() {
        assumeTrue(toggleProperties.isEnableSecureDocumentConversionEndpoint());
        Response response = createAndProcessSecureDocRequest(UUID.randomUUID().toString());
        assertEquals(400, response.getStatusCode());
    }

    @Test
    void shouldReturn401WhenUnAuthenticateUserConvertWordDocumentToPDF() {
        assumeTrue(toggleProperties.isEnableSecureDocumentConversionEndpoint());
        unAuthenticatedRequest
            .post(API_CONVERT + UUID.randomUUID())
            .then()
            .assertThat()
            .statusCode(401)
            .log().all();
    }

    @Test
    void shouldReturn400WhenUnAuthorisedUserConvertWordDocumentToPDF() throws IOException {
        assumeTrue(toggleProperties.isEnableSecureDocumentConversionEndpoint());
        String newDocId = extendedCcdHelper.uploadSecureDocxDocumentAndReturnUrl();
        Response response = createAndProcessSecureDocRequestFailure(newDocId);

        assertEquals(400, response.getStatusCode());
    }

    private Response createAndProcessSecureDocRequest(String newDocId) {
        UUID docId = UUID.fromString(newDocId.substring(newDocId.lastIndexOf('/') + 1));
        return cdamRequest
            .post("/api/convert/" + docId);
    }

    private Response createAndProcessSecureDocRequestFailure(String newDocId) {
        String docId = newDocId.substring(newDocId.lastIndexOf('/') + 1);
        return request
            .post("/api/convert/" + docId);
    }

}
