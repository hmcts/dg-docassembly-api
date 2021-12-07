package uk.gov.hmcts.reform.dg.docassembly.rest;

import okhttp3.OkHttpClient;
import okhttp3.mock.MockInterceptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.dg.docassembly.Application;
import uk.gov.hmcts.reform.dg.docassembly.dto.CreateTemplateRenditionDto;
import uk.gov.hmcts.reform.dg.docassembly.service.DmStoreUploader;
import uk.gov.hmcts.reform.dg.docassembly.service.DocmosisApiClient;
import uk.gov.hmcts.reform.dg.docassembly.service.TemplateRenditionService;

import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class, TestSecurityConfiguration.class})
public class TemplateRenditionResourceTest extends BaseTest {

    @MockBean
    TemplateRenditionService templateRenditionServiceMock;

    private MockInterceptor interceptor = new MockInterceptor();

    private TemplateRenditionService templateRenditionService;

    private DmStoreUploader dmStoreUploader;

    private OkHttpClient client;

    @Before
    public void setup() throws IOException {

        interceptor.reset();

        this.client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        dmStoreUploader = Mockito.mock(DmStoreUploader.class);

        templateRenditionService = new TemplateRenditionService(
                dmStoreUploader,
                new DocmosisApiClient(client, "http://tornado.com", "x")
        );
    }

    @Test
    public void shouldCallTemplateRenditionService() throws Exception {

        CreateTemplateRenditionDto templateRenditionOutputDto = new CreateTemplateRenditionDto();
        templateRenditionOutputDto.setRenditionOutputLocation("x");

        when(templateRenditionServiceMock.renderTemplate(Mockito.any(CreateTemplateRenditionDto.class)))
                .thenReturn(templateRenditionOutputDto);

        restLogoutMockMvc
                .perform(post("/api/template-renditions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"outputType\":\"PDF\", \"templateId\":\"1\"}")
                        .header("Authorization", "xxx"))
                .andDo(print())
                .andExpect(status().isOk());

        verify(templateRenditionServiceMock, Mockito.times(1))
                .renderTemplate(Mockito.any(CreateTemplateRenditionDto.class));
    }

    @Test
    public void shouldPassOutputNameFromClientToDmStore() throws Exception {
        CreateTemplateRenditionDto templateRenditionOutputDto = new CreateTemplateRenditionDto();

        restLogoutMockMvc
                .perform(post("/api/template-renditions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"outputType\":\"PDF\", \"templateId\":\"1\", \"outputName\": \"test-output-name\"}")
                        .header("Authorization", "xxx"))
                .andDo(print())
                .andExpect(status().isOk());



    }
}
