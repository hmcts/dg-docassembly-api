package uk.gov.hmcts.reform.dg.docassembly.functional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.specification.RequestSpecification;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.dg.docassembly.dto.CreateTemplateRenditionDto;
import uk.gov.hmcts.reform.dg.docassembly.dto.RenditionOutputType;
import uk.gov.hmcts.reform.dg.docassembly.testutil.TestUtil;
import uk.gov.hmcts.reform.document.domain.Document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.dg.docassembly.testutil.Base64.base64;

class SecureTemplateRenditionResourceTests extends BaseTest {

    //The calling service has enabled CDAM via a feature flag in the request payload in all instances in this test.
    @Autowired
    private TestUtil testUtil;

    private RequestSpecification cdamRequest;
    private RequestSpecification unAuthenticatedRequest;

    ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setupRequestSpecification() {

        cdamRequest = testUtil
            .cdamAuthRequest();

        unAuthenticatedRequest = testUtil
                .unAuthenticatedRequest()
                .contentType(APPLICATION_JSON_VALUE);
    }

    @Test
    void testTemplateRendition() throws JsonProcessingException {
        CreateTemplateRenditionDto createTemplateRenditionDto = populateRequestBody();
        createTemplateRenditionDto.setOutputType(null);

        final JSONObject jsonObject = new JSONObject(createTemplateRenditionDto);

        cdamRequest
                .body(jsonObject.toString())
                .post("/api/template-renditions")
                .then()
                .assertThat()
                .statusCode(200)
                .log()
                .all();
    }

    @Test
    void testTemplateRenditionToDoc() throws JsonProcessingException {
        CreateTemplateRenditionDto createTemplateRenditionDto = populateRequestBody();

        final JSONObject jsonObject = new JSONObject(createTemplateRenditionDto);

        cdamRequest
                .body(jsonObject.toString())
                .post("/api/template-renditions").then()
                .assertThat()
                .statusCode(200)
                .log()
                .all();
    }

    @Test
    void testTemplateRenditionToDocX() throws JsonProcessingException {
        CreateTemplateRenditionDto createTemplateRenditionDto = populateRequestBody();
        createTemplateRenditionDto.setOutputType(RenditionOutputType.DOCX);
        final JSONObject jsonObject = new JSONObject(createTemplateRenditionDto);

        cdamRequest
                .body(jsonObject.toString())
                .post("/api/template-renditions").then()
                .assertThat()
                .statusCode(200)
                .log()
                .all();
    }

    @Test
    void testTemplateRenditionToOutputName() throws JsonProcessingException {
        CreateTemplateRenditionDto createTemplateRenditionDto = populateRequestBody();
        createTemplateRenditionDto.setOutputType(RenditionOutputType.DOCX);
        createTemplateRenditionDto.setOutputFilename("test-output-name");
        final JSONObject jsonObject = new JSONObject(createTemplateRenditionDto);

        CreateTemplateRenditionDto response =
                cdamRequest
                        .body(jsonObject.toString())
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
    void shouldReturn500WhenMandatoryFormPayloadIsMissing() throws JsonProcessingException {
        CreateTemplateRenditionDto createTemplateRenditionDto = populateRequestBody();
        createTemplateRenditionDto.setOutputType(null);
        createTemplateRenditionDto.setFormPayload(null);
        final JSONObject jsonObject = new JSONObject(createTemplateRenditionDto);

        cdamRequest
                .body(jsonObject.toString())
                .post("/api/template-renditions")
                .then()
                .assertThat()
                .statusCode(400)
                .log()
                .all();
    }

    @Test
    void shouldReturn400WhenMandatoryTemplateIdIsMissing() throws JsonProcessingException {
        CreateTemplateRenditionDto createTemplateRenditionDto = populateRequestBody();
        createTemplateRenditionDto.setOutputType(null);
        createTemplateRenditionDto.setTemplateId(null);
        final JSONObject jsonObject = new JSONObject(createTemplateRenditionDto);

        cdamRequest
                .body(jsonObject.toString())
                .post("/api/template-renditions")
                .then()
                .assertThat()
                .statusCode(400)
                .log()
                .all();
    }

    @Test
    void shouldReturn401WhenUnAthenticateUserPostRequest() throws JsonProcessingException {
        CreateTemplateRenditionDto createTemplateRenditionDto = populateRequestBody();
        final JSONObject jsonObject = new JSONObject(createTemplateRenditionDto);

        unAuthenticatedRequest
                .body(jsonObject.toString())
                .post("/api/template-renditions")
                .then()
                .assertThat()
                .statusCode(401)
                .log()
                .all();
    }

    @Test
    void shouldReturn400WhenPostRequestMissingJurisdication() throws JsonProcessingException {
        CreateTemplateRenditionDto createTemplateRenditionDto = populateRequestBody();
        createTemplateRenditionDto.setJurisdictionId(null);
        final JSONObject jsonObject = new JSONObject(createTemplateRenditionDto);

        cdamRequest
            .body(jsonObject.toString())
            .post("/api/template-renditions")
            .then()
            .assertThat()
            .statusCode(400)
            .log()
            .all();
    }

    @Test
    void shouldReturn400WhenPostRequestMissingCaseType() throws JsonProcessingException {
        CreateTemplateRenditionDto createTemplateRenditionDto = populateRequestBody();
        createTemplateRenditionDto.setCaseTypeId(null);
        final JSONObject jsonObject = new JSONObject(createTemplateRenditionDto);

        cdamRequest
            .body(jsonObject.toString())
            .post("/api/template-renditions")
            .then()
            .assertThat()
            .statusCode(400)
            .log()
            .all();
    }

    private CreateTemplateRenditionDto populateRequestBody() throws JsonProcessingException {

        JsonNode newNode = mapper.readTree("{\"a\": \"1\"}");

        CreateTemplateRenditionDto createTemplateRenditionDto = new CreateTemplateRenditionDto();
        createTemplateRenditionDto.setSecureDocStoreEnabled(true);
        createTemplateRenditionDto.setOutputType(RenditionOutputType.DOC);
        createTemplateRenditionDto.setTemplateId(base64("FL-FRM-APP-ENG-00002.docx"));
        createTemplateRenditionDto.setFormPayload(newNode);
        createTemplateRenditionDto.setJurisdictionId("PUBLICLAW");
        createTemplateRenditionDto.setCaseTypeId(extendedCcdHelper.getEnvCcdCaseTypeId());

        return createTemplateRenditionDto;
    }
}
