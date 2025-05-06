package uk.gov.hmcts.reform.dg.docassembly.functional;

import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.dg.docassembly.dto.CreateTemplateRenditionDto;
import uk.gov.hmcts.reform.dg.docassembly.testutil.TestUtil;
import uk.gov.hmcts.reform.document.domain.Document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.dg.docassembly.testutil.Base64.base64;

class TemplateRenditionResourceTests extends BaseTest {

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
    void testTemplateRendition() {
        assumeFalse(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());

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
    void testTemplateRenditionToDoc() {
        assumeFalse(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());

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
    void testTemplateRenditionToDocX() {
        assumeFalse(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());

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
    void testTemplateRenditionToOutputName() {
        assumeFalse(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());

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

        assertEquals("test-output-name.docx", doc.originalDocumentName);
    }

    @Test
    void shouldReturn500WhenMandatoryFormPayloadIsMissing() {
        assumeFalse(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());

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
    void shouldReturn400WhenMandatoryTemplateIdIsMissing() {
        assumeFalse(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());

        request
                .body("{\"formPayload\":{\"a\":1}}")
                .post("/api/template-renditions")
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
    void shouldReturn400WhenCaseTypeIdFromPayloadIsMissing() {
        assumeTrue(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());
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
    void shouldReturn400WhenJurisdictionIdFromPayloadIsMissing() {
        assumeTrue(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());
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
    void shouldReturn400WhenCaseTypeIdAndJurisdictionIdFromPayloadIsMissing() {
        assumeTrue(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());
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
