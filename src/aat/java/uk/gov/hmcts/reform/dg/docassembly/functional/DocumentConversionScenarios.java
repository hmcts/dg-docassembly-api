package uk.gov.hmcts.reform.dg.docassembly.functional;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.dg.docassembly.testutil.TestUtil;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Ignore
public class DocumentConversionScenarios extends BaseTest {

    @Autowired
    private TestUtil testUtil;

    @Value("${test.url}")
    private String testUrl;

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    private RequestSpecification request;
    private RequestSpecification unAuthenticatedRequest;

    @Before
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
    public void testPDFConversionWithWordDocument() {
        Assume.assumeTrue(toggleProperties.isEnableDocumentConversionEndpoint());
        String newDocId = testUtil.uploadDOCDocumentAndReturnUrl();
        Response response = createAndProcessRequest(newDocId);

        Assert.assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testPDFConversionWithDocx() {
        Assume.assumeTrue(toggleProperties.isEnableDocumentConversionEndpoint());
        String newDocId = testUtil.uploadDocxDocumentAndReturnUrl();
        Response response = createAndProcessRequest(newDocId);

        Assert.assertEquals(200, response.getStatusCode());
    }


    @Test
    public void testPDFConversionWithPptx() {
        Assume.assumeTrue(toggleProperties.isEnableDocumentConversionEndpoint());
        String newDocId = testUtil.uploadPptxDocumentAndReturnUrl();
        Response response = createAndProcessRequest(newDocId);

        Assert.assertEquals(200, response.getStatusCode());
    }


    @Test
    public void testPDFConversionWithPPT() {
        Assume.assumeTrue(toggleProperties.isEnableDocumentConversionEndpoint());
        String newDocId = testUtil.uploadPptDocumentAndReturnUrl();
        Response response = createAndProcessRequest(newDocId);

        Assert.assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testPDFConversionWithXlsx() {
        Assume.assumeTrue(toggleProperties.isEnableDocumentConversionEndpoint());
        String newDocId = testUtil.uploadXlsxDocumentAndReturnUrl();

        Response response = createAndProcessRequest(newDocId);

        Assert.assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testPDFConversionWithXLS() {
        Assume.assumeTrue(toggleProperties.isEnableDocumentConversionEndpoint());
        String newDocId = testUtil.uploadXLSDocumentAndReturnUrl();
        Response response = createAndProcessRequest(newDocId);

        Assert.assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testPDFConversionWithRTF() {
        Assume.assumeTrue(toggleProperties.isEnableDocumentConversionEndpoint());
        String newDocId = testUtil.uploadRTFDocumentAndReturnUrl();
        Response response = createAndProcessRequest(newDocId);

        Assert.assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testPDFConversionWithTXT() {
        Assume.assumeTrue(toggleProperties.isEnableDocumentConversionEndpoint());
        String newDocId = testUtil.uploadTXTDocumentAndReturnUrl();
        Response response = createAndProcessRequest(newDocId);

        Assert.assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testFailedConversion() {
        Assume.assumeTrue(toggleProperties.isEnableDocumentConversionEndpoint());
        String newDocId = testUtil.uploadTXTDocumentAndReturnUrl();
        Response response = createAndProcessRequestFailure(newDocId + "567");

        Assert.assertEquals(400, response.getStatusCode());
    }

    @Test
    public void shouldReturn401WhenUnAuthenticateUserConvertWordDocumentToPDF() {
        Assume.assumeTrue(toggleProperties.isEnableDocumentConversionEndpoint());
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
