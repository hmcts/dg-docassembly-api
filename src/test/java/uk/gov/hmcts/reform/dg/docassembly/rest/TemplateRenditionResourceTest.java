package uk.gov.hmcts.reform.dg.docassembly.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.dg.docassembly.dto.CreateTemplateRenditionDto;
import uk.gov.hmcts.reform.dg.docassembly.service.TemplateRenditionService;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class TemplateRenditionResourceTest {

    @InjectMocks
    TemplateRenditionResource templateRenditionResource;

    @Mock
    TemplateRenditionService templateRenditionService;

    ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldCallTemplateRenditionService() throws Exception {

        CreateTemplateRenditionDto createTemplateRenditionDto = new CreateTemplateRenditionDto();
        createTemplateRenditionDto.setRenditionOutputLocation("x");
        createTemplateRenditionDto.setFormPayload(mapper.readTree("{\"outputType\":\"PDF\", \"templateId\":\"1\"}"));
        createTemplateRenditionDto.setTemplateId("1234");
        String auth = "xxx";
        String serviceAuth = "yyy";
        createTemplateRenditionDto.setJwt(auth);
        createTemplateRenditionDto.setServiceAuth(serviceAuth);

        when(templateRenditionService.renderTemplate(any()))
                .thenReturn(createTemplateRenditionDto);

        ResponseEntity<CreateTemplateRenditionDto> renditionDtoResponseEntity =
                templateRenditionResource.createTemplateRendition(createTemplateRenditionDto,
                auth,serviceAuth);

        verify(templateRenditionService, Mockito.times(1))
                .renderTemplate(Mockito.any(CreateTemplateRenditionDto.class));

        assertNull(renditionDtoResponseEntity.getBody().getServiceAuth());
        assertNull(renditionDtoResponseEntity.getBody().getJwt());
        assertNotNull(renditionDtoResponseEntity.getBody().getOutputFilename());
    }

}
