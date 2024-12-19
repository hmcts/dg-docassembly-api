package uk.gov.hmcts.reform.dg.docassembly.functional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static uk.gov.hmcts.reform.dg.docassembly.testutil.Base64.base64;

class FormDefinitionResourceTests extends BaseTest {

    @Test
    void testFormDefinitionGetTemplateWithUIDefinition() {
        // If the Endpoint Toggles are enabled, continue, if not skip and ignore
        assumeTrue(toggleProperties.isEnableFormDefinitionEndpoint());

        testUtil
                .authRequest()
                .baseUri(testUtil.getTestUrl())
                .get("/api/form-definitions/" + base64("CV-CMC-GOR-ENG-0004-UI-Test.docx"))
                .then()
                .assertThat()
                .statusCode(200)
                .log()
                .all();

    }

    @Test
    void testFormDefinitionGetNotExistingTemplate() {
        // If the Endpoint Toggles are enabled, continue, if not skip and ignore
        assumeTrue(toggleProperties.isEnableFormDefinitionEndpoint());

        testUtil
                .authRequest()
                .baseUri(testUtil.getTestUrl())
                .get("/api/form-definitions/" + base64("dont-exist.docx"))
                .then()
                .assertThat()
                .statusCode(404)
                .log()
                .all();

    }

    @Test
    void testFormDefinitionGetTemplateWithoutUIDefinition() {
        // If the Endpoint Toggles are enabled, continue, if not skip and ignore
        assumeTrue(toggleProperties.isEnableFormDefinitionEndpoint());

        testUtil
                .authRequest()
                .baseUri(testUtil.getTestUrl())
                .get("/api/form-definitions/" + base64("FL-FRM-APP-ENG-00002.docx"))
                .then()
                .assertThat()
                .statusCode(404)
                .log()
                .all();

    }

    @Test
    void shouldReturn401WhenUnauthenticatedUserGetTemplateWithDefinition() {
        // If the Endpoint Toggles are enabled, continue, if not skip and ignore
        assumeTrue(toggleProperties.isEnableFormDefinitionEndpoint());

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
