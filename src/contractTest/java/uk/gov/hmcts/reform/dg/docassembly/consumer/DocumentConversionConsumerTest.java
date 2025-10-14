package uk.gov.hmcts.reform.dg.docassembly.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.UUID;

@Slf4j
@PactTestFor(providerName = "doc_assembly_document_conversion_provider")
class DocumentConversionConsumerTest extends BaseConsumerTest {

    private static final String DOCUMENT_CONVERSION_PROVIDER_NAME = "doc_assembly_document_conversion_provider";
    private static final String DOCUMENT_CONVERSION_API_URI = "/api/convert/";
    private static final UUID EXAMPLE_DOCUMENT_ID = UUID.fromString("f401727b-5a50-40bb-ac4d-87dc34910b6e");

    @Pact(provider = DOCUMENT_CONVERSION_PROVIDER_NAME, consumer = DOC_ASSEMBLY_CONSUMER)
    public V4Pact convertDocumentToPdf200(PactDslWithProvider builder) {
        return builder
            .given("a document exists for conversion")
            .uponReceiving("A request to convert a document to PDF")
            .path(DOCUMENT_CONVERSION_API_URI + EXAMPLE_DOCUMENT_ID)
            .method(HttpMethod.POST.toString())
            .headers(getHeaders())
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .bodyMatchingContentType(
                MediaType.APPLICATION_PDF_VALUE,
                "%PDF-1.4 sample content"
            )
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "convertDocumentToPdf200")
    void testConvertDocumentToPdf200(MockServer mockServer) {
        SerenityRest
            .given()
            .headers(getHeaders())
            .post(mockServer.getUrl() + DOCUMENT_CONVERSION_API_URI + EXAMPLE_DOCUMENT_ID)
            .then()
            .statusCode(HttpStatus.OK.value());
    }
}