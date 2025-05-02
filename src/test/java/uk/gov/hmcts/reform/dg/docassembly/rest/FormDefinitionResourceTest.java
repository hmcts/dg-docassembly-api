package uk.gov.hmcts.reform.dg.docassembly.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.dg.docassembly.dto.TemplateIdDto;
import uk.gov.hmcts.reform.dg.docassembly.service.FormDefinitionService;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FormDefinitionResourceTest {

    @Mock
    private FormDefinitionService formDefinitionService;

    @InjectMocks
    private FormDefinitionResource formDefinitionResource;

    @Captor
    private ArgumentCaptor<TemplateIdDto> templateIdDtoCaptor;

    private JsonNode mockFormDefinition;
    private static final String TEST_TEMPLATE_ID = "template123";
    private static final String TEST_JWT = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";

    @BeforeEach
    void setUp() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        mockFormDefinition = objectMapper.readTree("{\"fieldName\": \"fieldValue\", \"anotherField\": 123}");
    }

    @Test
    @DisplayName("Should return OK with JsonNode when service finds the definition")
    void getFormDefinition_shouldReturnOk_whenServiceReturnsDefinition() {
        when(formDefinitionService.getFormDefinition(any(TemplateIdDto.class)))
            .thenReturn(Optional.of(mockFormDefinition));

        ResponseEntity<JsonNode> response = formDefinitionResource.getFormDefinition(
            TEST_TEMPLATE_ID,
            TEST_JWT
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(formDefinitionService, times(1)).getFormDefinition(templateIdDtoCaptor.capture());

        TemplateIdDto capturedDto = templateIdDtoCaptor.getValue();
        assertNotNull(capturedDto);
        assertEquals(TEST_TEMPLATE_ID, capturedDto.getTemplateId());
        assertEquals(TEST_JWT, capturedDto.getJwt());

        assertNotNull(response.getBody());
        assertEquals(mockFormDefinition, response.getBody());
    }

    @Test
    @DisplayName("Should return Not Found when service does not find the definition")
    void getFormDefinition_shouldReturnNotFound_whenServiceReturnsEmpty() {

        when(formDefinitionService.getFormDefinition(any(TemplateIdDto.class)))
            .thenReturn(Optional.empty());


        ResponseEntity<JsonNode> response = formDefinitionResource.getFormDefinition(
            TEST_TEMPLATE_ID,
            TEST_JWT
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(formDefinitionService, times(1)).getFormDefinition(templateIdDtoCaptor.capture());

        TemplateIdDto capturedDto = templateIdDtoCaptor.getValue();
        assertNotNull(capturedDto);
        assertEquals(TEST_TEMPLATE_ID, capturedDto.getTemplateId());
        assertEquals(TEST_JWT, capturedDto.getJwt());

        assertNull(response.getBody());
    }

    @Test
    @DisplayName("Should propagate exception if service throws an error")
    void getFormDefinition_shouldThrowException_whenServiceThrowsError() {

        RuntimeException serviceException = new RuntimeException("Database connection failed");
        when(formDefinitionService.getFormDefinition(any(TemplateIdDto.class)))
            .thenThrow(serviceException);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            formDefinitionResource.getFormDefinition(TEST_TEMPLATE_ID, TEST_JWT);
        }, "Expected getFormDefinition to throw, but it didn't");

        assertEquals(serviceException, thrown);

        verify(formDefinitionService, times(1)).getFormDefinition(templateIdDtoCaptor.capture());

        TemplateIdDto capturedDto = templateIdDtoCaptor.getValue();
        assertNotNull(capturedDto);
        assertEquals(TEST_TEMPLATE_ID, capturedDto.getTemplateId());
        assertEquals(TEST_JWT, capturedDto.getJwt());
    }
}