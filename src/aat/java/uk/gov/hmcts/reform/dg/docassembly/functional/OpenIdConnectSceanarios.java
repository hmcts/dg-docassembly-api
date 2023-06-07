package uk.gov.hmcts.reform.dg.docassembly.functional;

import io.restassured.response.Response;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;
import static uk.gov.hmcts.reform.dg.docassembly.testutil.Base64.base64;

public class OpenIdConnectSceanarios extends BaseTest {

    public static final String API_TEMPLATE_RENDITIONS_URL = "/api/template-renditions";

    @Rule
    public ExpectedException exceptionThrown = ExpectedException.none();

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Test
    public void testValidAuthenticationAndAuthorisation() {
        assumeTrue(toggleProperties.isEnableTemplateRenditionEndpoint());

        final Response response =
                testUtil
                        .authRequest()
                        .header("Content-Type", "application/json")
                        .body(getBodyForRequest())
                        .post(API_TEMPLATE_RENDITIONS_URL);

        assertEquals(200, response.getStatusCode());
    }

    @Ignore(value = "Cftlib needs to be fixed to return 401")
    @Test // Invalid S2SAuth
    public void testInvalidS2SAuth() {
        assumeTrue(toggleProperties.isEnableTemplateRenditionEndpoint());

        final Response response =
                testUtil
                        .invalidIdamAuthrequest()
                        .baseUri(testUtil.getTestUrl())
                        .header("Content-Type", "application/json")
                        .body(getBodyForRequest())
                        .post(API_TEMPLATE_RENDITIONS_URL);

        assertEquals(401, response.getStatusCode());
    }

    @Test
    public void testWithInvalidIdamAuth() {
        assumeTrue(toggleProperties.isEnableTemplateRenditionEndpoint());

        final Response response =
                testUtil
                        .invalidIdamAuthrequest()
                        .header("Content-Type", "application/json")
                        .body(getBodyForRequest())
                        .post(API_TEMPLATE_RENDITIONS_URL);

        assertEquals(401, response.getStatusCode());

    }

    @Test
    public void testWithEmptyS2SAuth() {
        assumeTrue(toggleProperties.isEnableTemplateRenditionEndpoint());

        exceptionThrown.expect(IllegalArgumentException.class);

        final Response response =
                testUtil
                        .validAuthRequestWithEmptyS2SAuth()
                        .header("Content-Type", "application/json")
                        .body(getBodyForRequest())
                        .post(API_TEMPLATE_RENDITIONS_URL);

        assertEquals(401, response.getStatusCode());

    }

    @Test
    public void testWithEmptyIdamAuthAndValidS2SAuth() {
        assumeTrue(toggleProperties.isEnableTemplateRenditionEndpoint());

        exceptionThrown.expect(IllegalArgumentException.class);

        testUtil
                .validS2SAuthWithEmptyIdamAuth()
                .header("Content-Type", "application/json")
                .body(getBodyForRequest())
                .post(API_TEMPLATE_RENDITIONS_URL);

    }

    @Test
    public void testIdamAuthAndS2SAuthAreEmpty() {
        assumeTrue(toggleProperties.isEnableTemplateRenditionEndpoint());

        exceptionThrown.expect(IllegalArgumentException.class);

        testUtil
                .validS2SAuthWithEmptyIdamAuth()
                .header("Content-Type", "application/json")
                .body(getBodyForRequest())
                .post(API_TEMPLATE_RENDITIONS_URL);
    }

    private String getBodyForRequest() {
        return "{\"formPayload\":{\"a\":1}, "
                + "\"outputType\":\"DOC\","
                + " \"templateId\":\"" + base64("FL-FRM-APP-ENG-00002.docx") + "\"}";
    }
}
