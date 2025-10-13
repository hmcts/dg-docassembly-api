package uk.gov.hmcts.reform.dg.docassembly.functional;

import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.dg.docassembly.dto.CreateTemplateRenditionDto;
import uk.gov.hmcts.reform.dg.docassembly.testutil.ExtendedCcdHelper;
import uk.gov.hmcts.reform.dg.docassembly.testutil.TestUtil;
import uk.gov.hmcts.reform.dg.docassembly.testutil.ToggleProperties;
import uk.gov.hmcts.reform.document.domain.Document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.dg.docassembly.testutil.Base64.base64;

class TemplateRenditionResourceTests extends BaseTest {

    public static final String FL_FRM_APP_ENG_00002_DOCX = "FL-FRM-APP-ENG-00002.docx";
    public static final String API_TEMPLATE_RENDITIONS = "/api/template-renditions";
    @Value("${test.url}")
    private String testUrl;

    private RequestSpecification request;
    private RequestSpecification unAuthenticatedRequest;

    public TemplateRenditionResourceTests(
            TestUtil testUtil,
            ToggleProperties toggleProperties,
            ExtendedCcdHelper extendedCcdHelper
    ) {
        super(testUtil, toggleProperties, extendedCcdHelper);
    }

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
    void testTemplateRendition() {
        assumeFalse(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());

        request
                .body("{\"formPayload\":{\"a\":1}, \"templateId\":\""
                        + base64(FL_FRM_APP_ENG_00002_DOCX)
                        + "\"}")
                .post(API_TEMPLATE_RENDITIONS)
                .then()
                .assertThat()
                .statusCode(200)
                .log()
                .all();
    }

    @Test
    void testTemplateRenditionToDoc() {
        assumeFalse(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());

        request
                .body("{\"formPayload\":{\"a\":1}, \"outputType\":\"DOC\", \"templateId\":\""
                        + base64(FL_FRM_APP_ENG_00002_DOCX)
                        + "\"}")
                .post(API_TEMPLATE_RENDITIONS).then()
                .assertThat()
                .statusCode(200)
                .log()
                .all();
    }

    @Test
    void testTemplateRenditionToDocX() {
        assumeFalse(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());

        request
                .body("{\"formPayload\":{\"a\":1}, \"outputType\":\"DOCX\", \"templateId\":\""
                        + base64(FL_FRM_APP_ENG_00002_DOCX)
                        + "\"}")
                .post(API_TEMPLATE_RENDITIONS).then()
                .assertThat()
                .statusCode(200)
                .log()
                .all();
    }

    @Test
    void testTemplateRenditionToOutputName() {
        assumeFalse(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());

        CreateTemplateRenditionDto response =
                request
                        .body("{\"formPayload\":{\"a\":1},"
                                + " \"outputType\":\"DOCX\", "
                                + "\"outputFilename\":\"test-output-name\","
                                + " \"templateId\":\"" + base64(FL_FRM_APP_ENG_00002_DOCX)
                                + "\"}")
                        .post(API_TEMPLATE_RENDITIONS)
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(CreateTemplateRenditionDto.class);

        String dmStoreHref = response.getRenditionOutputLocation();
        Document doc = testUtil.getDocumentMetadata(dmStoreHref.substring(dmStoreHref.lastIndexOf("/") + 1));

        assertEquals("test-output-name.docx", doc.originalDocumentName);
    }

    @Test
    void shouldReturn500WhenMandatoryFormPayloadIsMissing() {
        assumeFalse(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());

        request
                .body("{\"templateId\":\"" + base64(FL_FRM_APP_ENG_00002_DOCX) + "\"}")
                .post(API_TEMPLATE_RENDITIONS)
                .then()
                .assertThat()
                .statusCode(400)
                .log()
                .all();
    }

    @Test
    void shouldReturn400WhenMandatoryTemplateIdIsMissing() {
        assumeFalse(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());

        request
                .body("{\"formPayload\":{\"a\":1}}")
                .post(API_TEMPLATE_RENDITIONS)
                .then()
                .assertThat()
                .statusCode(400)
                .log()
                .all();
    }

    @Test
    void shouldReturn401WhenUnAthenticateUserPostRequest() {
        assumeFalse(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());

        unAuthenticatedRequest
                .body("{\"formPayload\":{\"a\":1}, \"templateId\":\""
                        + base64(FL_FRM_APP_ENG_00002_DOCX)
                        + "\"}")
                .post(API_TEMPLATE_RENDITIONS)
                .then()
                .assertThat()
                .statusCode(401)
                .log()
                .all();
    }

    @Test
    void shouldReturn400WhenCaseTypeIdFromPayloadIsMissing() {
        assumeTrue(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());
        request
            .body("{\"formPayload\":{\"a\":1},"
                + " \"outputType\":\"DOCX\", "
                + "\"outputFilename\":\"test-output-name\","
                + "\"jurisdictionId\":\"dummyJurisdictionId\","
                + " \"templateId\":\"" + base64(FL_FRM_APP_ENG_00002_DOCX)
                + "\"}")
            .post(API_TEMPLATE_RENDITIONS)
            .then()
            .assertThat()
            .statusCode(400)
            .log()
            .all();
    }

    @Test
    void shouldReturn400WhenJurisdictionIdFromPayloadIsMissing() {
        assumeTrue(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());
        request
            .body("{\"formPayload\":{\"a\":1},"
                + " \"outputType\":\"DOCX\", "
                + "\"outputFilename\":\"test-output-name\","
                + "\"caseTypeId\":\"dummyCaseTypeId\","
                + " \"templateId\":\"" + base64(FL_FRM_APP_ENG_00002_DOCX)
                + "\"}")
            .post(API_TEMPLATE_RENDITIONS)
            .then()
            .assertThat()
            .statusCode(400)
            .log()
            .all();
    }

    @Test
    void shouldReturn400WhenCaseTypeIdAndJurisdictionIdFromPayloadIsMissing() {
        assumeTrue(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());
        request
            .body("{\"formPayload\":{\"a\":1},"
                + " \"outputType\":\"DOCX\", "
                + "\"outputFilename\":\"test-output-name\","
                + " \"templateId\":\"" + base64(FL_FRM_APP_ENG_00002_DOCX)
                + "\"}")
            .post(API_TEMPLATE_RENDITIONS)
            .then()
            .assertThat()
            .statusCode(400)
            .log()
            .all();
    }
}
