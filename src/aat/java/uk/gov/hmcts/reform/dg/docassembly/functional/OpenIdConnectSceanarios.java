package uk.gov.hmcts.reform.dg.docassembly.functional;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.dg.docassembly.testutil.ExtendedCcdHelper;
import uk.gov.hmcts.reform.dg.docassembly.testutil.TestUtil;
import uk.gov.hmcts.reform.dg.docassembly.testutil.ToggleProperties;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.dg.docassembly.testutil.Base64.base64;

class OpenIdConnectSceanarios extends BaseTest {

    public static final String API_TEMPLATE_RENDITIONS_URL = "/api/template-renditions";

    public OpenIdConnectSceanarios(
            TestUtil testUtil,
            ToggleProperties toggleProperties,
            ExtendedCcdHelper extendedCcdHelper
    ) {
        super(testUtil, toggleProperties, extendedCcdHelper);
    }

    @Test
    void testValidAuthenticationAndAuthorisation() {
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
    void testInvalidS2SAuth() {
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
    void testWithInvalidIdamAuth() {
        testUtil
                .invalidIdamAuthrequest()
                .contentType(APPLICATION_JSON_VALUE)
                .body(getBodyForRequest())
                .post(API_TEMPLATE_RENDITIONS_URL)
                .then()
                .assertThat()
                .statusCode(401);

    }



    private String getBodyForRequest() {
        return "{\"formPayload\":{\"a\":1}, "
                + "\"outputType\":\"DOC\","
                + " \"templateId\":\"" + base64("FL-FRM-APP-ENG-00002.docx") + "\"}";
    }
}
