package uk.gov.hmcts.reform.dg.docassembly.functional;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.dg.docassembly.testutil.TestUtil;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class DocumentConversionScenarios extends BaseTest {

    @Autowired
    private TestUtil testUtil;

    @Value("${test.url}")
    private String testUrl;

    private RequestSpecification request;
    private RequestSpecification unAuthenticatedRequest;

    @BeforeEach
    public void setupRequestSpecification() {
        request = testUtil
                .authRequest()
                .baseUri(testUrl)
                .contentType(APPLICATION_JSON_VALUE);

        unAuthenticatedRequest = testUtil
                .unAuthenticatedRequest()
                .baseUri(testUrl)
                .contentType(APPLICATION_JSON_VALUE);
    }

    @Test
    void testPDFConversionWithWordDocument() {
        String newDocId = testUtil.uploadDOCDocumentAndReturnUrl();
        Response response = createAndProcessRequest(newDocId);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testPDFConversionWithDocx() {
        String newDocId = testUtil.uploadDocxDocumentAndReturnUrl();
        Response response = createAndProcessRequest(newDocId);

        assertEquals(200, response.getStatusCode());
    }


    @Test
    void testPDFConversionWithPptx() {
        String newDocId = testUtil.uploadPptxDocumentAndReturnUrl();
        Response response = createAndProcessRequest(newDocId);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testPDFConversionWithPPT() {
        String newDocId = testUtil.uploadPptDocumentAndReturnUrl();
        Response response = createAndProcessRequest(newDocId);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testPDFConversionWithXlsx() {
        String newDocId = testUtil.uploadXlsxDocumentAndReturnUrl();

        Response response = createAndProcessRequest(newDocId);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testPDFConversionWithXLS() {
        String newDocId = testUtil.uploadXLSDocumentAndReturnUrl();
        Response response = createAndProcessRequest(newDocId);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testPDFConversionWithRTF() {
        String newDocId = testUtil.uploadRTFDocumentAndReturnUrl();
        Response response = createAndProcessRequest(newDocId);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testPDFConversionWithTXT() {
        String newDocId = testUtil.uploadTXTDocumentAndReturnUrl();
        Response response = createAndProcessRequest(newDocId);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testFailedConversion() {
        String newDocId = testUtil.uploadTXTDocumentAndReturnUrl();
        Response response = createAndProcessRequestFailure(newDocId + "567");

        assertEquals(400, response.getStatusCode());
    }

    @Test
    void shouldReturn401WhenUnAuthenticateUserConvertWordDocumentToPDF() {
        final String newDocId = testUtil.uploadDOCDocumentAndReturnUrl();
        final UUID docId = UUID.fromString(newDocId.substring(newDocId.lastIndexOf('/') + 1));
        unAuthenticatedRequest
                .post("/api/convert/" + docId)
                .then()
                .assertThat()
                .statusCode(401)
                .log().all();
    }

    private Response createAndProcessRequest(String newDocId) {
        UUID docId = UUID.fromString(newDocId.substring(newDocId.lastIndexOf('/') + 1));
        return request.post("/api/convert/" + docId);
    }

    private Response createAndProcessRequestFailure(String newDocId) {
        String docId = newDocId.substring(newDocId.lastIndexOf('/') + 1);
        return request.post("/api/convert/" + docId);
    }

}
