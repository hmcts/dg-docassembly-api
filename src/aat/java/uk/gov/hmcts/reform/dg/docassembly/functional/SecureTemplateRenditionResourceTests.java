package uk.gov.hmcts.reform.dg.docassembly.functional;

import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.dg.docassembly.dto.CreateTemplateRenditionDto;
import uk.gov.hmcts.reform.dg.docassembly.testutil.ExtendedCcdHelper;
import uk.gov.hmcts.reform.dg.docassembly.testutil.TestUtil;
import uk.gov.hmcts.reform.dg.docassembly.testutil.ToggleProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.dg.docassembly.testutil.Base64.base64;

/**
 * Secure (CDAM) template rendition functional tests.
 *
 * <p>Request bodies are literal JSON strings (same as {@link TemplateRenditionResourceTests}).
 * Do not serialise {@code CreateTemplateRenditionDto} with {@code org.json.JSONObject} after
 * Jackson 3 — it introspects {@code JsonNode} as a JavaBean and breaks {@code formPayload}.</p>
 */
class SecureTemplateRenditionResourceTests extends BaseTest {

    public static final String API_TEMPLATE_RENDITIONS = "/api/template-renditions";
    private static final String TEMPLATE_NAME = "FL-FRM-APP-ENG-00002.docx";

    @Value("${test.url}")
    private String testUrl;

    private RequestSpecification cdamRequest;
    private RequestSpecification unAuthenticatedRequest;

    @Autowired
    public SecureTemplateRenditionResourceTests(
            TestUtil testUtil,
            ToggleProperties toggleProperties,
            ExtendedCcdHelper extendedCcdHelper
    ) {
        super(testUtil, toggleProperties, extendedCcdHelper);
    }

    @BeforeEach
    public void setupRequestSpecification() {
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
    void testTemplateRendition() {
        cdamRequest
                .body(secureTemplateRenditionBody(null, null))
                .post(API_TEMPLATE_RENDITIONS)
                .then()
                .log().ifError()
                .assertThat()
                .statusCode(200)
                .log()
                .all();
    }

    @Test
    void testTemplateRenditionToDoc() {
        cdamRequest
                .body(secureTemplateRenditionBody("DOC", null))
                .post(API_TEMPLATE_RENDITIONS).then()
                .log().ifError()
                .assertThat()
                .statusCode(200)
                .log()
                .all();
    }

    @Test
    void testTemplateRenditionToDocX() {
        cdamRequest
                .body(secureTemplateRenditionBody("DOCX", null))
                .post(API_TEMPLATE_RENDITIONS).then()
                .log().ifError()
                .assertThat()
                .statusCode(200)
                .log()
                .all();
    }

    @Test
    void testTemplateRenditionToOutputName() {
        CreateTemplateRenditionDto response =
                cdamRequest
                        .body(secureTemplateRenditionBody("DOCX", "test-output-name"))
                        .post(API_TEMPLATE_RENDITIONS)
                        .then()
                        .log().ifError()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(CreateTemplateRenditionDto.class);

        assertEquals("test-output-name", response.getOutputFilename());
        assertNotNull(response.getRenditionOutputLocation());
        assertEquals("test-output-name.docx", response.getFullOutputFilename());
    }

    @Test
    void shouldReturn500WhenMandatoryFormPayloadIsMissing() {
        cdamRequest
                .body(secureTemplateRenditionBody(null, null).replace("\"formPayload\":{\"a\":1}, ", ""))
                .post(API_TEMPLATE_RENDITIONS)
                .then()
                .assertThat()
                .statusCode(400)
                .log()
                .all();
    }

    @Test
    void shouldReturn400WhenMandatoryTemplateIdIsMissing() {
        cdamRequest
                .body("{\"formPayload\":{\"a\":1}, \"secureDocStoreEnabled\":true,"
                        + " \"jurisdictionId\":\"PUBLICLAW\","
                        + " \"caseTypeId\":\"" + extendedCcdHelper.getEnvCcdCaseTypeId() + "\"}")
                .post(API_TEMPLATE_RENDITIONS)
                .then()
                .assertThat()
                .statusCode(400)
                .log()
                .all();
    }

    @Test
    void shouldReturn401WhenUnAthenticateUserPostRequest() {
        unAuthenticatedRequest
                .body(secureTemplateRenditionBody("DOC", null))
                .post(API_TEMPLATE_RENDITIONS)
                .then()
                .assertThat()
                .statusCode(401)
                .log()
                .all();
    }

    @Test
    void shouldReturn400WhenPostRequestMissingJurisdication() {
        cdamRequest
            .body("{\"formPayload\":{\"a\":1}, \"secureDocStoreEnabled\":true, \"outputType\":\"DOC\","
                    + " \"templateId\":\"" + base64(TEMPLATE_NAME) + "\","
                    + " \"caseTypeId\":\"" + extendedCcdHelper.getEnvCcdCaseTypeId() + "\"}")
            .post(API_TEMPLATE_RENDITIONS)
            .then()
            .assertThat()
            .statusCode(400)
            .log()
            .all();
    }

    @Test
    void shouldReturn400WhenPostRequestMissingCaseType() {
        cdamRequest
            .body("{\"formPayload\":{\"a\":1}, \"secureDocStoreEnabled\":true, \"outputType\":\"DOC\","
                    + " \"templateId\":\"" + base64(TEMPLATE_NAME) + "\","
                    + " \"jurisdictionId\":\"PUBLICLAW\"}")
            .post(API_TEMPLATE_RENDITIONS)
            .then()
            .assertThat()
            .statusCode(400)
            .log()
            .all();
    }

    private String secureTemplateRenditionBody(String outputType, String outputFilename) {
        StringBuilder body = new StringBuilder();
        body.append("{\"formPayload\":{\"a\":1}, \"secureDocStoreEnabled\":true, \"templateId\":\"")
                .append(base64(TEMPLATE_NAME))
                .append("\", \"jurisdictionId\":\"PUBLICLAW\", \"caseTypeId\":\"")
                .append(extendedCcdHelper.getEnvCcdCaseTypeId())
                .append("\"");
        if (outputType != null) {
            body.append(", \"outputType\":\"").append(outputType).append("\"");
        }
        if (outputFilename != null) {
            body.append(", \"outputFilename\":\"").append(outputFilename).append("\"");
        }
        body.append("}");
        return body.toString();
    }
}
