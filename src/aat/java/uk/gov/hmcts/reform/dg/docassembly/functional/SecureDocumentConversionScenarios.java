package uk.gov.hmcts.reform.dg.docassembly.functional;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.dg.docassembly.testutil.TestUtil;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class SecureDocumentConversionScenarios extends BaseTest {

    @Autowired
    private TestUtil testUtil;

    @Value("${test.url}")
    private String testUrl;

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    private RequestSpecification request;
    private RequestSpecification cdamRequest;
    private RequestSpecification unAuthenticatedRequest;

    @Before
    public void setupRequestSpecification() {
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
    public void testPDFConversionWithWordDocument() throws Exception {
        Assume.assumeTrue(toggleProperties.isEnableSecureDocumentConversionEndpoint());
        String newDocId = extendedCcdHelper.uploadSecureDOCDocumentAndReturnUrl();
        Response response = createAndProcessSecureDocRequest(newDocId);

        Assert.assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testPDFConversionWithDocx() throws Exception {
        Assume.assumeTrue(toggleProperties.isEnableSecureDocumentConversionEndpoint());
        String newDocId = extendedCcdHelper.uploadSecureDocxDocumentAndReturnUrl();
        Response response = createAndProcessSecureDocRequest(newDocId);

        Assert.assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testPDFConversionWithPptx() throws Exception {
        Assume.assumeTrue(toggleProperties.isEnableSecureDocumentConversionEndpoint());
        String newDocId = extendedCcdHelper.uploadSecurePptxDocumentAndReturnUrl();
        Response response = createAndProcessSecureDocRequest(newDocId);

        Assert.assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testPDFConversionWithPPT() throws Exception {
        Assume.assumeTrue(toggleProperties.isEnableSecureDocumentConversionEndpoint());
        String newDocId = extendedCcdHelper.uploadSecurePptDocumentAndReturnUrl();
        Response response = createAndProcessSecureDocRequest(newDocId);

        Assert.assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testPDFConversionWithXlsx() throws Exception {
        Assume.assumeTrue(toggleProperties.isEnableSecureDocumentConversionEndpoint());
        String newDocId = extendedCcdHelper.uploadSecureXlsxDocumentAndReturnUrl();
        Response response = createAndProcessSecureDocRequest(newDocId);

        Assert.assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testPDFConversionWithXLS() throws Exception {
        Assume.assumeTrue(toggleProperties.isEnableSecureDocumentConversionEndpoint());
        String newDocId = extendedCcdHelper.uploadSecureXLSDocumentAndReturnUrl();
        Response response = createAndProcessSecureDocRequest(newDocId);

        Assert.assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testPDFConversionWithRTF() throws Exception {
        Assume.assumeTrue(toggleProperties.isEnableSecureDocumentConversionEndpoint());
        String newDocId = extendedCcdHelper.uploadSecureRTFDocumentAndReturnUrl();
        Response response = createAndProcessSecureDocRequest(newDocId);

        Assert.assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testPDFConversionWithTXT() throws Exception {
        Assume.assumeTrue(toggleProperties.isEnableSecureDocumentConversionEndpoint());
        String newDocId = extendedCcdHelper.uploadSecureTXTDocumentAndReturnUrl();
        Response response = createAndProcessSecureDocRequest(newDocId);

        Assert.assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testFailedConversion() {
        Assume.assumeTrue(toggleProperties.isEnableSecureDocumentConversionEndpoint());
        Response response = createAndProcessSecureDocRequest(UUID.randomUUID().toString());
        Assert.assertEquals(400, response.getStatusCode());
    }

    @Test
    public void shouldReturn401WhenUnAuthenticateUserConvertWordDocumentToPDF() {
        Assume.assumeTrue(toggleProperties.isEnableSecureDocumentConversionEndpoint());
        unAuthenticatedRequest
            .post("/api/convert/" + UUID.randomUUID())
            .then()
            .assertThat()
            .statusCode(401)
            .log().all();
    }

    @Test
    public void shouldReturn400WhenUnAuthorisedUserConvertWordDocumentToPDF() throws Exception {
        Assume.assumeTrue(toggleProperties.isEnableSecureDocumentConversionEndpoint());
        String newDocId = extendedCcdHelper.uploadSecureDocxDocumentAndReturnUrl();
        Response response = createAndProcessSecureDocRequestFailure(newDocId);

        Assert.assertEquals(400, response.getStatusCode());
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
