package uk.gov.hmcts.reform.dg.docassembly.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.LambdaDslObject;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;

@Slf4j
@PactTestFor(providerName = "doc_assembly_template_rendition_provider")
class TemplateRenditionConsumerTest extends BaseConsumerTest {

    private static final String TEMPLATE_RENDITION_PROVIDER_NAME = "doc_assembly_template_rendition_provider";
    private static final String TEMPLATE_RENDITION_API_PATH = "/api/template-renditions";

    @Pact(provider = TEMPLATE_RENDITION_PROVIDER_NAME, consumer = DOC_ASSEMBLY_CONSUMER)
    public V4Pact createTemplateRendition200(PactDslWithProvider builder) {
        return builder
            .given("a template can be rendered successfully")
            .uponReceiving("A request to create a template rendition")
            .path(TEMPLATE_RENDITION_API_PATH)
            .method(HttpMethod.POST.toString())
            .headers(getHeaders())
            .body(createRequestDsl())
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .body(createResponseDsl())
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "createTemplateRendition200")
    void testCreateTemplateRendition200(MockServer mockServer) {
        SerenityRest
            .given()
            .headers(getHeaders())
            .contentType(ContentType.JSON)
            .body(createRequestDsl().getBody().toString())
            .post(mockServer.getUrl() + TEMPLATE_RENDITION_API_PATH)
            .then()
            .statusCode(HttpStatus.OK.value());
    }

    private DslPart createRequestDsl() {
        return newJsonBody(this::buildRequestBody).build();
    }

    private void buildRequestBody(LambdaDslObject body) {
        body
            .stringType("templateId", "FL-FRM-GOR-ENG-12345")
            .booleanType("secureDocStoreEnabled", true)
            .stringType("caseTypeId", "FinancialRemedyContested")
            .stringType("jurisdictionId", "DIVORCE")
            .stringType("hashToken", "Abcde12345")
            .stringMatcher("outputType", "(PDF|DOC|DOCX)", "PDF")
            .object("formPayload")
            .array("errors");
    }

    private DslPart createResponseDsl() {
        return newJsonBody(this::buildResponseBody).build();
    }

    private void buildResponseBody(LambdaDslObject body) {
        buildRequestBody(body);
        body.stringType(
            "renditionOutputLocation",
            "http://dm-store:8080/documents/d9a74b1e-188e-4a6c-9f82-3e28e0b2e8b0"
        );
    }
}