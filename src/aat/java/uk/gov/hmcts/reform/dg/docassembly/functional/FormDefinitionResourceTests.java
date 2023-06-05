package uk.gov.hmcts.reform.dg.docassembly.functional;

import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import static uk.gov.hmcts.reform.dg.docassembly.testutil.Base64.base64;

@Ignore
public class FormDefinitionResourceTests extends BaseTest {

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Test
    public void testFormDefinitionGetTemplateWithUIDefinition() {
        // If the Endpoint Toggles are enabled, continue, if not skip and ignore
        Assume.assumeTrue(toggleProperties.isEnableFormDefinitionEndpoint());

        Response response =
                testUtil
                        .authRequest()
                        .baseUri(testUtil.getTestUrl())
                        .get("/api/form-definitions/" + base64("CV-CMC-GOR-ENG-0004-UI-Test.docx"));

        Assert.assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testFormDefinitionGetNotExistingTemplate() {
        // If the Endpoint Toggles are enabled, continue, if not skip and ignore
        Assume.assumeTrue(toggleProperties.isEnableFormDefinitionEndpoint());

        Response response =
                testUtil
                        .authRequest()
                        .baseUri(testUtil.getTestUrl())
                        .get("/api/form-definitions/" + base64("dont-exist.docx"));

        Assert.assertEquals(404, response.getStatusCode());
    }

    @Test
    public void testFormDefinitionGetTemplateWithoutUIDefinition() {
        // If the Endpoint Toggles are enabled, continue, if not skip and ignore
        Assume.assumeTrue(toggleProperties.isEnableFormDefinitionEndpoint());

        Response response = testUtil
                .authRequest()
                .baseUri(testUtil.getTestUrl())
                .get("/api/form-definitions/" + base64("FL-FRM-APP-ENG-00002.docx"));

        Assert.assertEquals(404, response.getStatusCode());
    }

    @Test
    public void shouldReturn401WhenUnauthenticatedUserGetTemplateWithDefinition() {
        // If the Endpoint Toggles are enabled, continue, if not skip and ignore
        Assume.assumeTrue(toggleProperties.isEnableFormDefinitionEndpoint());

        testUtil
                .unAuthenticatedRequest()
                .baseUri(testUtil.getTestUrl())
                .get("/api/form-definitions/" + base64("CV-CMC-GOR-ENG-0004-UI-Test.docx"))
                .then()
                .assertThat()
                .statusCode(401)
                .log()
                .all();
    }
}
