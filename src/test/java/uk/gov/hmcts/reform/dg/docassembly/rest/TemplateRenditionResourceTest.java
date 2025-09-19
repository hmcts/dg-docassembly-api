package uk.gov.hmcts.reform.dg.docassembly.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.dg.docassembly.config.Constants;
import uk.gov.hmcts.reform.dg.docassembly.dto.CreateTemplateRenditionDto;
import uk.gov.hmcts.reform.dg.docassembly.exception.DocumentTaskProcessingException;
import uk.gov.hmcts.reform.dg.docassembly.service.TemplateRenditionService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TemplateRenditionResourceTest {

    TemplateRenditionResource templateRenditionResource;

    @Mock
    TemplateRenditionService templateRenditionService;

    public static final String AUTH = "xxx";
    public static final String SERVICE_AUTH = "yyy";

    ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        this.templateRenditionResource = new TemplateRenditionResource(templateRenditionService);
    }

    @Test
    void shouldCallTemplateRenditionService() throws Exception {
        CreateTemplateRenditionDto createTemplateRenditionDto = new CreateTemplateRenditionDto();
        createTemplateRenditionDto.setRenditionOutputLocation("x");
        createTemplateRenditionDto.setFormPayload(mapper.readTree("{\"outputType\":\"PDF\", \"templateId\":\"1\"}"));
        createTemplateRenditionDto.setTemplateId("1234");
        createTemplateRenditionDto.setJwt(AUTH);
        createTemplateRenditionDto.setServiceAuth(SERVICE_AUTH);

        when(templateRenditionService.renderTemplate(any()))
                .thenReturn(createTemplateRenditionDto);

        ResponseEntity<CreateTemplateRenditionDto> renditionDtoResponseEntity =
                templateRenditionResource.createTemplateRendition(createTemplateRenditionDto,
                        AUTH, SERVICE_AUTH);

        verify(templateRenditionService, Mockito.times(1))
                .renderTemplate(Mockito.any(CreateTemplateRenditionDto.class));

        assertNull(renditionDtoResponseEntity.getBody().getServiceAuth());
        assertNull(renditionDtoResponseEntity.getBody().getJwt());
        assertNotNull(renditionDtoResponseEntity.getBody().getOutputFilename());
        assertEquals(0, renditionDtoResponseEntity.getBody().getErrors().size());
    }

    @Test
    void validateCdamChecks() throws DocumentTaskProcessingException, IOException {
        ReflectionTestUtils.setField(templateRenditionResource, "cdamEnabled", true);
        CreateTemplateRenditionDto createTemplateRenditionDto = new CreateTemplateRenditionDto();
        createTemplateRenditionDto.setCaseTypeId("dummyCaseTypeId");
        createTemplateRenditionDto.setJurisdictionId("dummyJurisdictionId");
        when(templateRenditionService.renderTemplate(any()))
                .thenReturn(createTemplateRenditionDto);

        ResponseEntity<CreateTemplateRenditionDto> renditionDtoResponseEntity =
                templateRenditionResource.createTemplateRendition(createTemplateRenditionDto,
                        AUTH, SERVICE_AUTH);

        assertEquals(0, renditionDtoResponseEntity.getBody().getErrors().size());
    }

    @Test
    void validateCdamChecksCaseTypeIdMissing() throws DocumentTaskProcessingException, IOException {
        ReflectionTestUtils.setField(templateRenditionResource, "cdamEnabled", true);
        CreateTemplateRenditionDto createTemplateRenditionDto = new CreateTemplateRenditionDto();
        createTemplateRenditionDto.setJurisdictionId("dummyJurisdictionId");
        when(templateRenditionService.renderTemplate(any()))
                .thenReturn(createTemplateRenditionDto);

        ResponseEntity<CreateTemplateRenditionDto> renditionDtoResponseEntity =
                templateRenditionResource.createTemplateRendition(createTemplateRenditionDto,
                        AUTH, SERVICE_AUTH);

        assertEquals(1, renditionDtoResponseEntity.getBody().getErrors().size());
        assertEquals(Constants.CDAM_VALIDATION_MSG, renditionDtoResponseEntity.getBody().getErrors().get(0));
    }

    @Test
    void validateCdamChecksJurisdictionIdMissing() throws DocumentTaskProcessingException, IOException {
        ReflectionTestUtils.setField(templateRenditionResource, "cdamEnabled", true);
        CreateTemplateRenditionDto createTemplateRenditionDto = new CreateTemplateRenditionDto();
        createTemplateRenditionDto.setCaseTypeId("dummyCaseTypeId");
        when(templateRenditionService.renderTemplate(any()))
                .thenReturn(createTemplateRenditionDto);

        ResponseEntity<CreateTemplateRenditionDto> renditionDtoResponseEntity =
                templateRenditionResource.createTemplateRendition(createTemplateRenditionDto,
                        AUTH, SERVICE_AUTH);

        assertEquals(1, renditionDtoResponseEntity.getBody().getErrors().size());
        assertEquals(Constants.CDAM_VALIDATION_MSG, renditionDtoResponseEntity.getBody().getErrors().get(0));
    }

    @Test
    void validateCdamChecksBothMissingErrorScenario() throws DocumentTaskProcessingException, IOException {
        ReflectionTestUtils.setField(templateRenditionResource, "cdamEnabled", true);
        CreateTemplateRenditionDto createTemplateRenditionDto = new CreateTemplateRenditionDto();

        when(templateRenditionService.renderTemplate(any()))
                .thenReturn(createTemplateRenditionDto);

        ResponseEntity<CreateTemplateRenditionDto> renditionDtoResponseEntity =
                templateRenditionResource.createTemplateRendition(createTemplateRenditionDto,
                        AUTH, SERVICE_AUTH);

        assertEquals(1, renditionDtoResponseEntity.getBody().getErrors().size());
        assertEquals(Constants.CDAM_VALIDATION_MSG, renditionDtoResponseEntity.getBody().getErrors().get(0));
    }
}
