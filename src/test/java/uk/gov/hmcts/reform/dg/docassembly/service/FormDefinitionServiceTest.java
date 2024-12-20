package uk.gov.hmcts.reform.dg.docassembly.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.dg.docassembly.dto.TemplateIdDto;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class FormDefinitionServiceTest {

    @Mock
    TemplateManagementApiClient templateManagementApiClient;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    TemplateContentExtractor templateContentExtractor;

    @InjectMocks
    FormDefinitionService formDefinitionService;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetFormDefinition() throws Exception {

        ObjectMapper testObjectMapper = new ObjectMapper();

        TemplateIdDto dto =  Mockito.mock(TemplateIdDto.class);

        Mockito.when(templateManagementApiClient.getTemplate(Mockito.any(TemplateIdDto.class)))
                .thenReturn(Mockito.mock(InputStream.class));

        Mockito.when(templateContentExtractor.extractTextBetweenTags(
                Mockito.any(InputStream.class),
                Mockito.any(String.class),
                Mockito.any(String.class)))
                .thenReturn(Optional.of("{\"a\":1}"));

        Mockito.when(objectMapper.readTree(
                Mockito.any(String.class)))
                .thenReturn(testObjectMapper.readTree("{\"a\":1}"));

        Optional<JsonNode> json = formDefinitionService.getFormDefinition(dto);

        assertEquals(1, json.get().get("a").asInt());

    }

    @Test
    void testGetFormDefinitionException() throws Exception {

        TemplateIdDto dto =  Mockito.mock(TemplateIdDto.class);

        Mockito.when(templateManagementApiClient.getTemplate(Mockito.any(TemplateIdDto.class)))
                .thenThrow(new IOException());

        assertThrows(FormDefinitionRetrievalException.class, () -> formDefinitionService.getFormDefinition(dto));
    }

}
