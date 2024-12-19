package uk.gov.hmcts.reform.dg.docassembly.service;

import okhttp3.OkHttpClient;
import okhttp3.mock.ClasspathResources;
import okhttp3.mock.MockInterceptor;
import okhttp3.mock.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.dg.docassembly.dto.TemplateIdDto;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TemplateManagementApiClientTest {

    AuthTokenGenerator authTokenGenerator;

    MockInterceptor interceptor;

    TemplateManagementApiClient templateManagementApiClient;

    @BeforeEach
    public void setup() {

        interceptor = new MockInterceptor();

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        authTokenGenerator = Mockito.mock(AuthTokenGenerator.class);

        templateManagementApiClient = new TemplateManagementApiClient(
                client,
                "http://template-management-api/templates/",
                "xxx");
    }


    @Test
    void testRetrieval() throws Exception {
        TemplateIdDto templateIdDto = new TemplateIdDto();

        templateIdDto.setJwt("x");
        templateIdDto.setTemplateId("YWJj");

        Mockito.when(authTokenGenerator.generate()).thenReturn("x");

        interceptor.addRule(new Rule.Builder()
                .get()
                .url("http://template-management-api/templates/abc")
                .respond(ClasspathResources.resource("template1.docx")));

        InputStream response = templateManagementApiClient.getTemplate(templateIdDto);

        byte[] expectedBytes =  ClasspathResources.resource("template1.docx").readAllBytes();
        byte[] actualBytes = response.readAllBytes();

        assertArrayEquals(expectedBytes, actualBytes, "The response bytes do not match the expected template.");


    }

    @Test
    void testRetrievalException() {
        TemplateIdDto templateIdDto = new TemplateIdDto();

        templateIdDto.setJwt("x");
        templateIdDto.setTemplateId("YWJj");

        Mockito.when(authTokenGenerator.generate()).thenReturn("x");

        interceptor.addRule(new Rule.Builder()
                .get()
                .url("http://template-management-api/templates/abc")
                .respond("").code(500));

        assertThrows(FormDefinitionRetrievalException.class,
                () -> templateManagementApiClient.getTemplate(templateIdDto));
    }

    @Test
    void testTemplateNotFoundException() {
        TemplateIdDto templateIdDto = new TemplateIdDto();

        templateIdDto.setJwt("x");
        templateIdDto.setTemplateId("YWJj");

        Mockito.when(authTokenGenerator.generate()).thenReturn("x");

        interceptor.addRule(new Rule.Builder()
                .get()
                .url("http://template-management-api/templates/abc")
                .respond("").code(404));


        assertThrows(TemplateNotFoundException.class,
                () -> templateManagementApiClient.getTemplate(templateIdDto));
    }
}
