package uk.gov.hmcts.reform.dg.docassembly.functional;

import io.restassured.specification.RequestSpecification;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.dg.docassembly.dto.CreateTemplateRenditionDto;
import uk.gov.hmcts.reform.dg.docassembly.testutil.TestUtil;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.dg.docassembly.testutil.Base64.base64;

public class TemplateRenditionResourceTests extends BaseTest {

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
    public void testTemplateRendition() {
        Assume.assumeFalse(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());

        request
                .body("{\"formPayload\":{\"a\":1}, \"templateId\":\""
                        + base64("FL-FRM-APP-ENG-00002.docx")
                        + "\"}")
                .post("/api/template-renditions")
                .then()
                .assertThat()
                .statusCode(200)
                .log()
                .all();
    }

    @Test
    public void testTemplateRenditionToDoc() {
        Assume.assumeFalse(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());

        request
                .body("{\"formPayload\":{\"a\":1}, \"outputType\":\"DOC\", \"templateId\":\""
                        + base64("FL-FRM-APP-ENG-00002.docx")
                        + "\"}")
                .post("/api/template-renditions").then()
                .assertThat()
                .statusCode(200)
                .log()
                .all();
    }

    @Test
    public void testTemplateRenditionToDocX() {
        Assume.assumeFalse(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());

        request
                .body("{\"formPayload\":{\"a\":1}, \"outputType\":\"DOCX\", \"templateId\":\""
                        + base64("FL-FRM-APP-ENG-00002.docx")
                        + "\"}")
                .post("/api/template-renditions").then()
                .assertThat()
                .statusCode(200)
                .log()
                .all();
    }

    @Test
    public void testTemplateRenditionToOutputName() {
        Assume.assumeFalse(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());

        CreateTemplateRenditionDto response =
                request
                        .body("{\"formPayload\":{\"a\":1},"
                                + " \"outputType\":\"DOCX\", "
                                + "\"outputFilename\":\"test-output-name\","
                                + " \"templateId\":\"" + base64("FL-FRM-APP-ENG-00002.docx")
                                + "\"}")
                        .post("/api/template-renditions")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(CreateTemplateRenditionDto.class);

        String dmStoreHref = response.getRenditionOutputLocation();
        Document doc = testUtil.getDocumentMetadata(dmStoreHref.substring(dmStoreHref.lastIndexOf("/") + 1));

        Assert.assertEquals("test-output-name.docx", doc.originalDocumentName);
    }

    @Test
    public void shouldReturn500WhenMandatoryFormPayloadIsMissing() {
        Assume.assumeFalse(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());

        request
                .body("{\"templateId\":\"" + base64("FL-FRM-APP-ENG-00002.docx") + "\"}")
                .post("/api/template-renditions")
                .then()
                .assertThat()
                .statusCode(400)
                .log()
                .all();
    }

    @Test
    public void shouldReturn500WhenMandatoryTemplateIdIsMissing() {
        Assume.assumeFalse(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());

        request
                .body("{\"formPayload\":{\"a\":1}}")
                .post("/api/template-renditions")
                .then()
                .assertThat()
                .statusCode(500)//FIXME should be bad request
                .log()
                .all();
    }

    @Test
    public void shouldReturn401WhenUnAthenticateUserPostRequest() {
        Assume.assumeFalse(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());

        unAuthenticatedRequest
                .body("{\"formPayload\":{\"a\":1}, \"templateId\":\""
                        + base64("FL-FRM-APP-ENG-00002.docx")
                        + "\"}")
                .post("/api/template-renditions")
                .then()
                .assertThat()
                .statusCode(401)
                .log()
                .all();
    }

    @Test
    public void shouldReturn400WhenCaseTypeIdFromPayloadIsMissing() {
        Assume.assumeTrue(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());
        request
            .body("{\"formPayload\":{\"a\":1},"
                + " \"outputType\":\"DOCX\", "
                + "\"outputFilename\":\"test-output-name\","
                + "\"jurisdictionId\":\"dummyJurisdictionId\","
                + " \"templateId\":\"" + base64("FL-FRM-APP-ENG-00002.docx")
                + "\"}")
            .post("/api/template-renditions")
            .then()
            .assertThat()
            .statusCode(400)
            .log()
            .all();
    }

    @Test
    public void shouldReturn400WhenJurisdictionIdFromPayloadIsMissing() {
        Assume.assumeTrue(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());
        request
            .body("{\"formPayload\":{\"a\":1},"
                + " \"outputType\":\"DOCX\", "
                + "\"outputFilename\":\"test-output-name\","
                + "\"caseTypeId\":\"dummyCaseTypeId\","
                + " \"templateId\":\"" + base64("FL-FRM-APP-ENG-00002.docx")
                + "\"}")
            .post("/api/template-renditions")
            .then()
            .assertThat()
            .statusCode(400)
            .log()
            .all();
    }

    @Test
    public void shouldReturn400WhenCaseTypeIdAndJurisdictionIdFromPayloadIsMissing() {
        Assume.assumeTrue(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());
        request
            .body("{\"formPayload\":{\"a\":1},"
                + " \"outputType\":\"DOCX\", "
                + "\"outputFilename\":\"test-output-name\","
                + " \"templateId\":\"" + base64("FL-FRM-APP-ENG-00002.docx")
                + "\"}")
            .post("/api/template-renditions")
            .then()
            .assertThat()
            .statusCode(400)
            .log()
            .all();
    }
}
