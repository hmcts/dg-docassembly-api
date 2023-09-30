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
import uk.gov.hmcts.reform.dg.docassembly.config.Constants;
import uk.gov.hmcts.reform.dg.docassembly.dto.CreateTemplateRenditionDto;
import uk.gov.hmcts.reform.dg.docassembly.service.TemplateRenditionService;
import uk.gov.hmcts.reform.dg.docassembly.service.exception.DocumentTaskProcessingException;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
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

    public static final String auth = "xxx";
    public static final String serviceAuth = "yyy";

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
        assertEquals(0, renditionDtoResponseEntity.getBody().getErrors().size());
    }

    @Test
    public void validateCdamChecks() throws DocumentTaskProcessingException, IOException {
        templateRenditionResource.cdamEnabled = true;
        CreateTemplateRenditionDto createTemplateRenditionDto = new CreateTemplateRenditionDto();
        createTemplateRenditionDto.setCaseTypeId("dummyCaseTypeId");
        createTemplateRenditionDto.setJurisdictionId("dummyJurisdictionId");
        when(templateRenditionService.renderTemplate(any()))
                .thenReturn(createTemplateRenditionDto);

        ResponseEntity<CreateTemplateRenditionDto> renditionDtoResponseEntity =
                templateRenditionResource.createTemplateRendition(createTemplateRenditionDto,
                        auth,serviceAuth);

        assertEquals(0, renditionDtoResponseEntity.getBody().getErrors().size());
    }

    @Test
    public void validateCdamChecksCaseTypeIdMissing() throws DocumentTaskProcessingException, IOException {
        templateRenditionResource.cdamEnabled = true;
        CreateTemplateRenditionDto createTemplateRenditionDto = new CreateTemplateRenditionDto();
        createTemplateRenditionDto.setJurisdictionId("dummyJurisdictionId");
        when(templateRenditionService.renderTemplate(any()))
            .thenReturn(createTemplateRenditionDto);

        ResponseEntity<CreateTemplateRenditionDto> renditionDtoResponseEntity =
            templateRenditionResource.createTemplateRendition(createTemplateRenditionDto,
                auth,serviceAuth);

        assertEquals(1, renditionDtoResponseEntity.getBody().getErrors().size());
        assertEquals(Constants.CDAM_VALIDATION_MSG, renditionDtoResponseEntity.getBody().getErrors().get(0));
    }

    @Test
    public void validateCdamChecksJurisdictionIdMissing() throws DocumentTaskProcessingException, IOException {
        templateRenditionResource.cdamEnabled = true;
        CreateTemplateRenditionDto createTemplateRenditionDto = new CreateTemplateRenditionDto();
        createTemplateRenditionDto.setCaseTypeId("dummyCaseTypeId");
        when(templateRenditionService.renderTemplate(any()))
            .thenReturn(createTemplateRenditionDto);

        ResponseEntity<CreateTemplateRenditionDto> renditionDtoResponseEntity =
            templateRenditionResource.createTemplateRendition(createTemplateRenditionDto,
                auth,serviceAuth);

        assertEquals(1, renditionDtoResponseEntity.getBody().getErrors().size());
        assertEquals(Constants.CDAM_VALIDATION_MSG, renditionDtoResponseEntity.getBody().getErrors().get(0));
    }

    @Test
    public void validateCdamChecksBothMissingErrorScenario() throws DocumentTaskProcessingException, IOException {
        templateRenditionResource.cdamEnabled = true;
        CreateTemplateRenditionDto createTemplateRenditionDto = new CreateTemplateRenditionDto();

        when(templateRenditionService.renderTemplate(any()))
            .thenReturn(createTemplateRenditionDto);

        ResponseEntity<CreateTemplateRenditionDto> renditionDtoResponseEntity =
            templateRenditionResource.createTemplateRendition(createTemplateRenditionDto,
                auth,serviceAuth);

        assertEquals(1, renditionDtoResponseEntity.getBody().getErrors().size());
        assertEquals(Constants.CDAM_VALIDATION_MSG, renditionDtoResponseEntity.getBody().getErrors().get(0));
    }
}
