package uk.gov.hmcts.reform.dg.docassembly.functional;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import static org.junit.Assume.assumeTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.dg.docassembly.testutil.Base64.base64;

public class OpenIdConnectSceanarios extends BaseTest {

    public static final String API_TEMPLATE_RENDITIONS_URL = "/api/template-renditions";

    @Rule
    public ExpectedException exceptionThrown = ExpectedException.none();

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Test
    public void testValidAuthenticationAndAuthorisation() {
        testUtil
                .authRequest()
                .contentType(APPLICATION_JSON_VALUE)
                .body(getBodyForRequest())
                .post(API_TEMPLATE_RENDITIONS_URL)
                .then()
                .assertThat()
                .statusCode(200);

    }

    @Ignore(value = "Cftlib needs to be fixed to return 401")
    @Test // Invalid S2SAuth
    public void testInvalidS2SAuth() {
        testUtil
                .invalidIdamAuthrequest()
                .baseUri(testUtil.getTestUrl())
                .contentType(APPLICATION_JSON_VALUE)
                .body(getBodyForRequest())
                .post(API_TEMPLATE_RENDITIONS_URL)
                .then()
                .assertThat()
                .statusCode(401);
    }

    @Test
    public void testWithInvalidIdamAuth() {
        testUtil
                .invalidIdamAuthrequest()
                .contentType(APPLICATION_JSON_VALUE)
                .body(getBodyForRequest())
                .post(API_TEMPLATE_RENDITIONS_URL)
                .then()
                .assertThat()
                .statusCode(401);

    }

    @Test
    public void testWithEmptyS2SAuth() {
        exceptionThrown.expect(IllegalArgumentException.class);

        testUtil
                .validAuthRequestWithEmptyS2SAuth()
                .contentType(APPLICATION_JSON_VALUE)
                .body(getBodyForRequest())
                .post(API_TEMPLATE_RENDITIONS_URL);
    }

    @Test
    public void testWithEmptyIdamAuthAndValidS2SAuth() {
        exceptionThrown.expect(IllegalArgumentException.class);

        testUtil
                .validS2SAuthWithEmptyIdamAuth()
                .contentType(APPLICATION_JSON_VALUE)
                .body(getBodyForRequest())
                .post(API_TEMPLATE_RENDITIONS_URL);

    }

    @Test
    public void testIdamAuthAndS2SAuthAreEmpty() {
        exceptionThrown.expect(IllegalArgumentException.class);

        testUtil
                .validS2SAuthWithEmptyIdamAuth()
                .contentType(APPLICATION_JSON_VALUE)
                .body(getBodyForRequest())
                .post(API_TEMPLATE_RENDITIONS_URL);
    }

    private String getBodyForRequest() {
        return "{\"formPayload\":{\"a\":1}, "
                + "\"outputType\":\"DOC\","
                + " \"templateId\":\"" + base64("FL-FRM-APP-ENG-00002.docx") + "\"}";
    }
}
