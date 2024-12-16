package uk.gov.hmcts.reform.dg.docassembly.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.dg.docassembly.Application;
import uk.gov.hmcts.reform.dg.docassembly.dto.TemplateIdDto;
import uk.gov.hmcts.reform.dg.docassembly.service.FormDefinitionRetrievalException;
import uk.gov.hmcts.reform.dg.docassembly.service.FormDefinitionService;
import uk.gov.hmcts.reform.dg.docassembly.service.TemplateNotFoundException;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(classes = {Application.class, TestSecurityConfiguration.class})
@ExtendWith(MockitoExtension.class)
class FormDefinitionResourceTest extends BaseTest {

    @MockitoBean
    FormDefinitionService formDefinitionService;

    @Test
    void shouldCallFormDefinitionService() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        when(formDefinitionService.getFormDefinition(Mockito.any(TemplateIdDto.class)))
                .thenReturn(Optional.of(objectMapper.readTree("{}")));

        restLogoutMockMvc
                .perform(get("/api/form-definitions/123")
                        .header("Authorization", "xxx"))
                .andDo(print()).andExpect(status().isOk());
    }

    @Test
    void testTemplateNotFoundErrorCode() throws Exception {

        when(formDefinitionService.getFormDefinition(Mockito.any(TemplateIdDto.class)))
                .thenThrow(new TemplateNotFoundException("xxx"));

        restLogoutMockMvc
                .perform(get("/api/form-definitions/123")
                        .header("Authorization", "xxx"))
                .andDo(print()).andExpect(status().is4xxClientError());

        verify(formDefinitionService, Mockito.times(1))
                .getFormDefinition(Mockito.any(TemplateIdDto.class));
    }

    @Test
    void testTemplateRetrievalException() throws Exception {

        when(formDefinitionService.getFormDefinition(Mockito.any(TemplateIdDto.class)))
                .thenThrow(new FormDefinitionRetrievalException("xxx"));

        restLogoutMockMvc
                .perform(get("/api/form-definitions/123")
                        .header("Authorization", "xxx"))
                .andDo(print()).andExpect(status().is5xxServerError());

        verify(formDefinitionService, Mockito.times(1))
                .getFormDefinition(Mockito.any(TemplateIdDto.class));
    }

}
