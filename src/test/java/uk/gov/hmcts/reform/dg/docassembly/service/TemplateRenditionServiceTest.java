package uk.gov.hmcts.reform.dg.docassembly.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.mock.ClasspathResources;
import okhttp3.mock.MockInterceptor;
import okhttp3.mock.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.dg.docassembly.dto.CreateTemplateRenditionDto;
import uk.gov.hmcts.reform.dg.docassembly.dto.RenditionOutputType;
import uk.gov.hmcts.reform.dg.docassembly.service.exception.DocumentTaskProcessingException;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TemplateRenditionServiceTest {

    private final MockInterceptor interceptor = new MockInterceptor();

    private TemplateRenditionService templateRenditionService;

    private DmStoreUploader dmStoreUploader;

    private CreateTemplateRenditionDto createTemplateRenditionDto;

    @BeforeEach
    void setup() throws IOException {

        interceptor.reset();

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        dmStoreUploader = Mockito.mock(DmStoreUploader.class);

        CdamService cdamService = Mockito.mock(CdamService.class);

        templateRenditionService = new TemplateRenditionService(
            dmStoreUploader,
            new DocmosisApiClient(client, "http://tornado.com", "x"),
            cdamService
        );

        createTemplateRenditionDto = createTemplateRenditionDto();
    }

    @Test
    void testRenditionWithTypePDF() throws Exception {

        createTemplateRenditionDto.setOutputType(RenditionOutputType.PDF);

        interceptor.addRule(new Rule.Builder()
                .post()
                .respond(ClasspathResources.resource("template1.docx")));

        Mockito.when(
                dmStoreUploader.uploadFile(Mockito.any(File.class),
                Mockito.any(CreateTemplateRenditionDto.class))).thenReturn(createTemplateRenditionDto);


        CreateTemplateRenditionDto templateRenditionOutputDto =
                templateRenditionService.renderTemplate(createTemplateRenditionDto);

        assertEquals("x", templateRenditionOutputDto.getRenditionOutputLocation());
    }

    @Test
    void testRenditionWithTypeDoc() throws Exception {

        createTemplateRenditionDto.setOutputType(RenditionOutputType.DOC);

        interceptor.addRule(new Rule.Builder()
                .post()
                .respond(ClasspathResources.resource("template1.docx")));

        Mockito.when(
                dmStoreUploader.uploadFile(Mockito.any(File.class),
                        Mockito.any(CreateTemplateRenditionDto.class))).thenReturn(createTemplateRenditionDto);


        CreateTemplateRenditionDto templateRenditionOutputDto =
                templateRenditionService.renderTemplate(createTemplateRenditionDto);

        assertEquals("x", templateRenditionOutputDto.getRenditionOutputLocation());
    }

    @Test
    void testRenditionWithTypeDocX() throws Exception {

        createTemplateRenditionDto.setOutputType(RenditionOutputType.DOCX);

        interceptor.addRule(new Rule.Builder()
                .post()
                .respond(ClasspathResources.resource("template1.docx")));

        Mockito.when(
                dmStoreUploader.uploadFile(Mockito.any(File.class),
                        Mockito.any(CreateTemplateRenditionDto.class))).thenReturn(createTemplateRenditionDto);


        CreateTemplateRenditionDto templateRenditionOutputDto =
                templateRenditionService.renderTemplate(createTemplateRenditionDto);

        assertEquals("x", templateRenditionOutputDto.getRenditionOutputLocation());
    }

    @Test
    void testRenditionWithNoData() throws Exception {
        createTemplateRenditionDto.setFormPayload(null);

        interceptor.addRule(new Rule.Builder()
                .post()
                .respond(ClasspathResources.resource("template1.docx")));

        Mockito.when(
                dmStoreUploader.uploadFile(Mockito.any(File.class),
                Mockito.any(CreateTemplateRenditionDto.class))).thenReturn(createTemplateRenditionDto);


        CreateTemplateRenditionDto templateRenditionOutputDto =
                templateRenditionService.renderTemplate(createTemplateRenditionDto);

        assertEquals("x", templateRenditionOutputDto.getRenditionOutputLocation());
    }

    @Test
    void testRenditionException() {

        interceptor.addRule(new Rule.Builder()
                .post()
                .respond("").code(500));

        Mockito.when(
                dmStoreUploader.uploadFile(Mockito.any(File.class),
                        Mockito.any(CreateTemplateRenditionDto.class))).thenReturn(createTemplateRenditionDto);


        assertThrows(TemplateRenditionException.class,
                () -> templateRenditionService.renderTemplate(createTemplateRenditionDto));
    }

    @Test
    void testRenditionWithCdamEnabled() throws DocumentTaskProcessingException, IOException {
        ReflectionTestUtils.setField(templateRenditionService, "cdamEnabled", true);

        createTemplateRenditionDto.setOutputType(RenditionOutputType.PDF);

        interceptor.addRule(new Rule.Builder()
            .post()
            .respond(ClasspathResources.resource("template1.docx")));

        Mockito.when(
            dmStoreUploader.uploadFile(Mockito.any(File.class),
                Mockito.any(CreateTemplateRenditionDto.class))).thenReturn(createTemplateRenditionDto);


        CreateTemplateRenditionDto templateRenditionOutputDto =
            templateRenditionService.renderTemplate(createTemplateRenditionDto);

        assertEquals("x", templateRenditionOutputDto.getRenditionOutputLocation());
    }

    private CreateTemplateRenditionDto createTemplateRenditionDto() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        CreateTemplateRenditionDto createTemplateDto = new CreateTemplateRenditionDto();

        createTemplateDto.setJwt("x");
        createTemplateDto.setTemplateId(new String(Base64.getEncoder().encode("1".getBytes())));
        createTemplateDto.setOutputType(RenditionOutputType.PDF);
        createTemplateDto.setFormPayload(objectMapper.readTree("{}"));
        createTemplateDto.setRenditionOutputLocation("x");

        return createTemplateDto;
    }
}
