package uk.gov.hmcts.reform.dg.docassembly.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.mock.MockInterceptor;
import okhttp3.mock.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.core.user.UserResolver;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.dg.docassembly.dto.CreateTemplateRenditionDto;
import uk.gov.hmcts.reform.dg.docassembly.dto.RenditionOutputType;
import uk.gov.hmcts.reform.dg.docassembly.exception.DocumentUploaderException;

import java.io.File;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DmStoreUploaderTest {

    AuthTokenGenerator authTokenGenerator;

    MockInterceptor interceptor;

    UserResolver userResolver;

    DmStoreUploader dmStoreUploader;

    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {

        interceptor = new MockInterceptor();

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        authTokenGenerator = Mockito.mock(AuthTokenGenerator.class);

        dmStoreUploader = new DmStoreUploader(
                client,
                authTokenGenerator,
                "http://dmstore",
                userResolver = Mockito.mock(UserResolver.class));

    }

    @Test
    void testUploadNewFile() throws Exception {
        CreateTemplateRenditionDto createTemplateRenditionDto = new CreateTemplateRenditionDto();
        createTemplateRenditionDto.setJwt("x");
        createTemplateRenditionDto.setTemplateId(new String(Base64.getEncoder().encode("1".getBytes())));
        createTemplateRenditionDto.setOutputType(RenditionOutputType.PDF);
        createTemplateRenditionDto.setFormPayload(objectMapper.readTree("{}"));
        Mockito.when(authTokenGenerator.generate()).thenReturn("x");

        User mockedUser = Mockito.mock(User.class);
        Mockito.when(mockedUser.getPrincipal()).thenReturn("p1");
        Mockito.when(userResolver.getTokenDetails("x")).thenReturn(mockedUser);

        interceptor.addRule(
                new Rule.Builder()
                        .post()
                        .respond("{\"_embedded\""
                                + ":{\"documents\":[{\"_links\":{\"self\":{\"href\":\"http://success.com/1\"}}}]}}"
                        )
        );

        CreateTemplateRenditionDto updatedCreateTemplateRenditionDto =
                dmStoreUploader.uploadFile(
                        File.createTempFile("testing_doc_assembly_a", "testing_doc_assembly_b"),
                        createTemplateRenditionDto
                );

        assertEquals("http://success.com/1",
                updatedCreateTemplateRenditionDto.getRenditionOutputLocation());

    }

    @Test
    void testUploadNewFileHttpFailedException() throws Exception {
        CreateTemplateRenditionDto createTemplateRenditionDto = new CreateTemplateRenditionDto();
        createTemplateRenditionDto.setJwt("x");
        createTemplateRenditionDto.setTemplateId(new String(Base64.getEncoder().encode("1".getBytes())));
        createTemplateRenditionDto.setOutputType(RenditionOutputType.PDF);
        createTemplateRenditionDto.setFormPayload(objectMapper.readTree("{}"));
        Mockito.when(authTokenGenerator.generate()).thenReturn("x");

        User mockedUser = Mockito.mock(User.class);
        Mockito.when(mockedUser.getPrincipal()).thenReturn("p1");
        Mockito.when(userResolver.getTokenDetails("x")).thenReturn(mockedUser);

        interceptor.addRule(new Rule.Builder()
                .post()
                .respond("").code(404));

        File tempFile = File.createTempFile("testing_doc_assembly_a", "testing_doc_assembly_b");
        assertThrows(DocumentUploaderException.class, () ->
            dmStoreUploader.uploadFile(tempFile, createTemplateRenditionDto)
        );

    }

    @Test
    void testUploadNewVersionOfFile() throws Exception {
        CreateTemplateRenditionDto createTemplateRenditionDto = new CreateTemplateRenditionDto();
        createTemplateRenditionDto.setJwt("x");
        createTemplateRenditionDto.setTemplateId(new String(Base64.getEncoder().encode("1".getBytes())));
        createTemplateRenditionDto.setOutputType(RenditionOutputType.PDF);
        createTemplateRenditionDto.setFormPayload(objectMapper.readTree("{}"));
        createTemplateRenditionDto.setRenditionOutputLocation("http://success.com/1");
        Mockito.when(authTokenGenerator.generate()).thenReturn("x");

        User mockedUser = Mockito.mock(User.class);
        Mockito.when(mockedUser.getPrincipal()).thenReturn("p1");
        Mockito.when(userResolver.getTokenDetails("x")).thenReturn(mockedUser);

        interceptor.addRule(new Rule.Builder()
                .post()
                .url("http://success.com/1")
                .respond("{\"_embedded\""
                        + ":{\"documents\":[{\"_links\":{\"self\":{\"href\":\"http://success.com/1\"}}}]}}"
                )
        );

        CreateTemplateRenditionDto updatedCreateTemplateRenditionDto =
                dmStoreUploader.uploadFile(
                        File.createTempFile("testing_doc_assembly_a", "testing_doc_assembly_b"),
                        createTemplateRenditionDto
                );

        assertEquals("http://success.com/1",
                updatedCreateTemplateRenditionDto.getRenditionOutputLocation());

    }

    @Test
    void testUploadNewVersionOfFileException() throws Exception {
        CreateTemplateRenditionDto createTemplateRenditionDto = new CreateTemplateRenditionDto();
        createTemplateRenditionDto.setJwt("x");
        createTemplateRenditionDto.setTemplateId(new String(Base64.getEncoder().encode("1".getBytes())));
        createTemplateRenditionDto.setOutputType(RenditionOutputType.PDF);
        createTemplateRenditionDto.setFormPayload(objectMapper.readTree("{}"));
        createTemplateRenditionDto.setRenditionOutputLocation("http://success.com/1");
        Mockito.when(authTokenGenerator.generate()).thenReturn("x");

        User mockedUser = Mockito.mock(User.class);
        Mockito.when(mockedUser.getPrincipal()).thenReturn("p1");
        Mockito.when(userResolver.getTokenDetails("x")).thenReturn(mockedUser);

        interceptor.addRule(new Rule.Builder()
                .post()
                .url("http://success.com/1")
                .respond("").code(500));


        File tempFile = File.createTempFile("testing_doc_assembly_a", "testing_doc_assembly_b");
        assertThrows(DocumentUploaderException.class, () ->
            dmStoreUploader.uploadFile(tempFile, createTemplateRenditionDto)
        );
    }
}
