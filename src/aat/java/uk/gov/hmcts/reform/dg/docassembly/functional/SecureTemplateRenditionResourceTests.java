package uk.gov.hmcts.reform.dg.docassembly.functional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.specification.RequestSpecification;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.dg.docassembly.dto.CreateTemplateRenditionDto;
import uk.gov.hmcts.reform.dg.docassembly.dto.RenditionOutputType;
import uk.gov.hmcts.reform.dg.docassembly.testutil.TestUtil;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.dg.docassembly.testutil.Base64.base64;

public class SecureTemplateRenditionResourceTests extends BaseTest {

    private static Logger log = LoggerFactory.getLogger(SecureTemplateRenditionResourceTests.class);

    @Autowired
    private TestUtil testUtil;

    @Value("${test.url}")
    private String testUrl;

    @Rule
    public RetryRule retryRule = new RetryRule(1);

    private RequestSpecification request;
    private RequestSpecification cdamRequest;
    private RequestSpecification unAuthenticatedRequest;

    ObjectMapper mapper = new ObjectMapper();

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
    public void testTemplateRendition() throws JsonProcessingException {
        // If the Endpoint Toggles are enabled, continue, if not skip and ignore
        Assume.assumeTrue(toggleProperties.isEnableTemplateRenditionEndpoint());

        CreateTemplateRenditionDto createTemplateRenditionDto = populateRequestBody();
        createTemplateRenditionDto.setOutputType(null);

        log.info("testTemplateRendition getTemplateId post {}", createTemplateRenditionDto.getTemplateId());
        final JSONObject jsonObject = new JSONObject(createTemplateRenditionDto);
        log.info("testTemplateRendition jsonObject post {}", jsonObject.toString());

        cdamRequest
                .body(jsonObject)
                .post("/api/template-renditions")
                .then()
                .assertThat()
                .statusCode(200)
                .log()
                .all();
    }

    @Test
    public void testTemplateRenditionToDoc() throws JsonProcessingException {
        // If the Endpoint Toggles are enabled, continue, if not skip and ignore
        Assume.assumeTrue(toggleProperties.isEnableTemplateRenditionEndpoint());

        CreateTemplateRenditionDto createTemplateRenditionDto = populateRequestBody();

        final JSONObject jsonObject = new JSONObject(createTemplateRenditionDto);

        cdamRequest
                .body(jsonObject)
                .post("/api/template-renditions").then()
                .assertThat()
                .statusCode(200)
                .log()
                .all();
    }

    @Test
    public void testTemplateRenditionToDocX() throws JsonProcessingException {
        // If the Endpoint Toggles are enabled, continue, if not skip and ignore
        Assume.assumeTrue(toggleProperties.isEnableTemplateRenditionEndpoint());

        CreateTemplateRenditionDto createTemplateRenditionDto = populateRequestBody();
        createTemplateRenditionDto.setOutputType(RenditionOutputType.DOCX);
        final JSONObject jsonObject = new JSONObject(createTemplateRenditionDto);

        cdamRequest
                .body(jsonObject)
                .post("/api/template-renditions").then()
                .assertThat()
                .statusCode(200)
                .log()
                .all();
    }

    @Test
    public void testTemplateRenditionToOutputName() throws JsonProcessingException {
        // If the Endpoint Toggles are enabled, continue, if not skip and ignore
        Assume.assumeTrue(toggleProperties.isEnableTemplateRenditionEndpoint());

        CreateTemplateRenditionDto createTemplateRenditionDto = populateRequestBody();
        createTemplateRenditionDto.setOutputType(RenditionOutputType.DOCX);
        createTemplateRenditionDto.setOutputFilename("test-output-name");
        final JSONObject jsonObject = new JSONObject(createTemplateRenditionDto);

        CreateTemplateRenditionDto response =
                cdamRequest
                        .body(jsonObject)
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
    public void shouldReturn500WhenMandatoryFormPayloadIsMissing() throws JsonProcessingException {
        // If the Endpoint Toggles are enabled, continue, if not skip and ignore
        Assume.assumeTrue(toggleProperties.isEnableTemplateRenditionEndpoint());

        CreateTemplateRenditionDto createTemplateRenditionDto = populateRequestBody();
        createTemplateRenditionDto.setOutputType(null);
        createTemplateRenditionDto.setFormPayload(null);
        final JSONObject jsonObject = new JSONObject(createTemplateRenditionDto);

        cdamRequest
                .body(jsonObject)
                .post("/api/template-renditions")
                .then()
                .assertThat()
                .statusCode(400)
                .log()
                .all();
    }

    @Test
    public void shouldReturn500WhenMandatoryTemplateIdIsMissing() throws JsonProcessingException {
        // If the Endpoint Toggles are enabled, continue, if not skip and ignore
        Assume.assumeTrue(toggleProperties.isEnableTemplateRenditionEndpoint());

        CreateTemplateRenditionDto createTemplateRenditionDto = populateRequestBody();
        createTemplateRenditionDto.setOutputType(null);
        createTemplateRenditionDto.setTemplateId(null);
        final JSONObject jsonObject = new JSONObject(createTemplateRenditionDto);

        cdamRequest
                .body(jsonObject)
                .post("/api/template-renditions")
                .then()
                .assertThat()
                .statusCode(500)//FIXME should be bad request
                .log()
                .all();
    }

    @Test
    public void shouldReturn401WhenUnAthenticateUserPostRequest() throws JsonProcessingException {
        // If the Endpoint Toggles are enabled, continue, if not skip and ignore
        Assume.assumeTrue(toggleProperties.isEnableTemplateRenditionEndpoint());

        CreateTemplateRenditionDto createTemplateRenditionDto = populateRequestBody();
        final JSONObject jsonObject = new JSONObject(createTemplateRenditionDto);

        unAuthenticatedRequest
                .body(jsonObject)
                .post("/api/template-renditions")
                .then()
                .assertThat()
                .statusCode(401)
                .log()
                .all();
    }

    @Test
    public void shouldReturn400WhenPostRequestMissingJurisdication() throws JsonProcessingException {
        // If the Endpoint Toggles are enabled, continue, if not skip and ignore
        Assume.assumeTrue(toggleProperties.isEnableTemplateRenditionEndpoint());

        CreateTemplateRenditionDto createTemplateRenditionDto = populateRequestBody();
        createTemplateRenditionDto.setJurisdictionId(null);
        final JSONObject jsonObject = new JSONObject(createTemplateRenditionDto);

        cdamRequest
            .body(jsonObject)
            .post("/api/template-renditions")
            .then()
            .assertThat()
            .statusCode(400)
            .log()
            .all();
    }

    @Test
    public void shouldReturn400WhenPostRequestMissingCaseType() throws JsonProcessingException {
        // If the Endpoint Toggles are enabled, continue, if not skip and ignore
        Assume.assumeTrue(toggleProperties.isEnableTemplateRenditionEndpoint());

        CreateTemplateRenditionDto createTemplateRenditionDto = populateRequestBody();
        createTemplateRenditionDto.setCaseTypeId(null);
        final JSONObject jsonObject = new JSONObject(createTemplateRenditionDto);

        cdamRequest
            .body(jsonObject)
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
