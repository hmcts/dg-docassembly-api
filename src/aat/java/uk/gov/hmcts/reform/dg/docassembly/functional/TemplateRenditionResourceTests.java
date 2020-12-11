package uk.gov.hmcts.reform.dg.docassembly.functional;

import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.hmcts.reform.dg.docassembly.dto.CreateTemplateRenditionDto;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.dg.docassembly.testutil.Base64.base64;

public class TemplateRenditionResourceTests extends BaseTest {

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Test
    public void testTemplateRendition() {
        // If the Endpoint Toggles are enabled, continue, if not skip and ignore
        Assume.assumeTrue(toggleProperties.isEnableTemplateRenditionEndpoint());

        Response response =
                testUtil
                        .authRequest()
                        .baseUri(testUtil.getTestUrl())
                        .contentType(APPLICATION_JSON_VALUE)
                        .body("{\"formPayload\":{\"a\":1}, \"templateId\":\""
                                + base64("FL-FRM-APP-ENG-00002.docx")
                                + "\"}")
                        .post("/api/template-renditions");

        Assert.assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testTemplateRenditionToDoc() {
        // If the Endpoint Toggles are enabled, continue, if not skip and ignore
        Assume.assumeTrue(toggleProperties.isEnableTemplateRenditionEndpoint());

        Response response =
                testUtil
                        .authRequest()
                        .baseUri(testUtil.getTestUrl())
                        .contentType(APPLICATION_JSON_VALUE)
                        .body("{\"formPayload\":{\"a\":1}, \"outputType\":\"DOC\", \"templateId\":\""
                                + base64("FL-FRM-APP-ENG-00002.docx")
                                + "\"}")
                        .post("/api/template-renditions");

        Assert.assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testTemplateRenditionToDocX() {
        // If the Endpoint Toggles are enabled, continue, if not skip and ignore
        Assume.assumeTrue(toggleProperties.isEnableTemplateRenditionEndpoint());

        Response response =
                testUtil
                        .authRequest()
                        .baseUri(testUtil.getTestUrl())
                        .contentType(APPLICATION_JSON_VALUE)
                        .body("{\"formPayload\":{\"a\":1}, \"outputType\":\"DOCX\", \"templateId\":\""
                                + base64("FL-FRM-APP-ENG-00002.docx")
                                + "\"}")
                        .post("/api/template-renditions");

        Assert.assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testTemplateRenditionToOutputName() {
        // If the Endpoint Toggles are enabled, continue, if not skip and ignore
        Assume.assumeTrue(toggleProperties.isEnableTemplateRenditionEndpoint());

        CreateTemplateRenditionDto response =
                testUtil
                        .authRequest()
                        .baseUri(testUtil.getTestUrl())
                        .contentType(APPLICATION_JSON_VALUE)
                        .body("{\"formPayload\":{\"a\":1}, \"outputType\":\"DOCX\", \"outputFilename\":\"test-output-name\", \"templateId\":\""
                                + base64("FL-FRM-APP-ENG-00002.docx")
                                + "\"}")
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
}
