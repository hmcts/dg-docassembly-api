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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class DocumentConversionAuthorizationScenarios extends BaseTest {

    @Value("${test.url}")
    private String testUrl;

    @Autowired
    private IdamHelper idamHelper;

    private RequestSpecification nonCaseworkerRequest;
    
    private static final String NON_CASEWORKER_EMAIL = "docassembly.citizen@test.com";
    private static final List<String> NON_CASEWORKER_ROLES = List.of("citizen", "letter-holder");

    @Autowired
    public DocumentConversionAuthorizationScenarios(
            TestUtil testUtil,
            ToggleProperties toggleProperties,
            ExtendedCcdHelper extendedCcdHelper
    ) {
        super(testUtil, toggleProperties, extendedCcdHelper);
    }

    @BeforeEach
    public void setupNonCaseworkerUser() {
        idamHelper.createUser(NON_CASEWORKER_EMAIL, NON_CASEWORKER_ROLES);
        String nonCaseworkerAuth = idamHelper.authenticateUser(NON_CASEWORKER_EMAIL);
        String s2sAuth = testUtil.getS2sAuth();
        
        nonCaseworkerRequest = RestAssured
                .given()
                .header(TestUtil.AUTHORIZATION, nonCaseworkerAuth)
                .header(TestUtil.SERVICE_AUTHORIZATION, s2sAuth)
                .baseUri(testUrl)
                .contentType(APPLICATION_JSON_VALUE);
    }

    @Test
    void shouldDenyAccessToNonCaseworkerUser() {
        String docUrl = testUtil.uploadDOCDocumentAndReturnUrl();
        UUID docId = UUID.fromString(docUrl.substring(docUrl.lastIndexOf('/') + 1));
        
        Response response = nonCaseworkerRequest.post(API_CONVERT + docId);
        
        int statusCode = response.getStatusCode();
        boolean isDenied = (statusCode == 400 || statusCode == 403);
        
        assertTrue(isDenied,
            String.format("Non-caseworker user should be denied. Expected 400/403, got: %d. "
                + "If 200, hardcoded 'caseworker' role is still being sent!", statusCode));
    }
}
