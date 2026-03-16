package uk.gov.hmcts.reform.dg.docassembly.functional;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.dg.docassembly.testutil.ExtendedCcdHelper;
import uk.gov.hmcts.reform.dg.docassembly.testutil.TestUtil;
import uk.gov.hmcts.reform.dg.docassembly.testutil.ToggleProperties;
import uk.gov.hmcts.reform.em.test.idam.IdamHelper;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.dg.docassembly.testutil.Base64.base64;

class TemplateRenditionAuthorizationScenarios extends BaseTest {

    private static final String API_TEMPLATE_RENDITIONS = "/api/template-renditions";
    private static final String TEMPLATE_NAME = "FL-FRM-APP-ENG-00002.docx";
    private static final String TEMPLATE_REQUEST_BODY =
            "{\"formPayload\":{\"a\":1}, \"templateId\":\"" + base64(TEMPLATE_NAME) + "\"}";

    @Value("${test.url}")
    private String testUrl;

    @Autowired
    private IdamHelper idamHelper;

    private RequestSpecification nonCaseworkerRequest;
    private RequestSpecification multiRoleCaseworkerRequest;

    private final String nonCaseworkerEmail = "docassembly.citizen." + UUID.randomUUID() + "@test.com";
    private final String multiRoleCaseworkerEmail = "docassembly.multirole." + UUID.randomUUID() + "@test.com";

    private static final List<String> NON_CASEWORKER_ROLES = List.of("ccd-import");
    private static final List<String> MULTI_ROLE_CASEWORKER_ROLES = List.of("caseworker-ia", "caseworker");

    @Autowired
    public TemplateRenditionAuthorizationScenarios(
            TestUtil testUtil,
            ToggleProperties toggleProperties,
            ExtendedCcdHelper extendedCcdHelper
    ) {
        super(testUtil, toggleProperties, extendedCcdHelper);
    }

    @BeforeEach
    public void setupUsers() {
        String s2sAuth = testUtil.getS2sAuth();

        idamHelper.createUser(nonCaseworkerEmail, NON_CASEWORKER_ROLES);
        String nonCaseworkerAuth = idamHelper.authenticateUser(nonCaseworkerEmail);
        nonCaseworkerRequest = RestAssured
                .given()
                .header(TestUtil.AUTHORIZATION, nonCaseworkerAuth)
                .header(TestUtil.SERVICE_AUTHORIZATION, s2sAuth)
                .baseUri(testUrl)
                .contentType(APPLICATION_JSON_VALUE);

        idamHelper.createUser(multiRoleCaseworkerEmail, MULTI_ROLE_CASEWORKER_ROLES);
        String multiRoleCaseworkerAuth = idamHelper.authenticateUser(multiRoleCaseworkerEmail);
        multiRoleCaseworkerRequest = RestAssured
                .given()
                .header(TestUtil.AUTHORIZATION, multiRoleCaseworkerAuth)
                .header(TestUtil.SERVICE_AUTHORIZATION, s2sAuth)
                .baseUri(testUrl)
                .contentType(APPLICATION_JSON_VALUE);
    }

    @Test
    void shouldAllowMultiRoleCaseworkerToRenderAndUploadTemplate() {
        assumeFalse(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());

        Response response = multiRoleCaseworkerRequest
                .body(TEMPLATE_REQUEST_BODY)
                .post(API_TEMPLATE_RENDITIONS);

        assertEquals(200, response.getStatusCode(),
            "Multi-role caseworker should be allowed to render and upload template. "
                + "Dynamic role resolution from IDAM in DmStoreUploader must be working.");
    }

    @Test
    void shouldDenyNonCaseworkerUserFromUploadingTemplate() {
        assumeFalse(toggleProperties.isEnableSecureDocumentTemplRendEndpoint());

        Response response = nonCaseworkerRequest
                .body(TEMPLATE_REQUEST_BODY)
                .post(API_TEMPLATE_RENDITIONS);

        assertNotEquals(200, response.getStatusCode(),
            "Non-caseworker user should be denied when uploading to DM Store. "
                + "If 200, dynamic role resolution is not passing correct roles to DM Store.");
    }
}
