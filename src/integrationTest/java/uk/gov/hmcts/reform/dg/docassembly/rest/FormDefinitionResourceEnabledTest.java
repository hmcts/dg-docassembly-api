package uk.gov.hmcts.reform.dg.docassembly.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.dg.docassembly.dto.TemplateIdDto;
import uk.gov.hmcts.reform.dg.docassembly.service.FormDefinitionRetrievalException;
import uk.gov.hmcts.reform.dg.docassembly.service.FormDefinitionService;
import uk.gov.hmcts.reform.dg.docassembly.service.TemplateNotFoundException;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@TestPropertySource(properties = "endpoint-toggles.form-definitions=true")
class FormDefinitionResourceEnabledTest extends RestTestBase {

    private static final String URI_TEMPLATE = "/api/form-definitions/123";
    private static final String AUTHORIZATION = "Authorization";
    @MockitoBean
    FormDefinitionService formDefinitionService;

    FormDefinitionResourceEnabledTest(WebApplicationContext context) {
        super(context);
    }

    @Test
    void shouldCallFormDefinitionService() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        when(formDefinitionService.getFormDefinition(any(TemplateIdDto.class)))
                .thenReturn(Optional.of(objectMapper.readTree("{}")));

        restLogoutMockMvc
                .perform(get(URI_TEMPLATE)
                        .header(AUTHORIZATION, "xxx"))
                .andDo(print()).andExpect(status().isOk());
    }

    @Test
    void testTemplateNotFoundErrorCode() throws Exception {

        when(formDefinitionService.getFormDefinition(any(TemplateIdDto.class)))
                .thenThrow(new TemplateNotFoundException("xxx"));

        restLogoutMockMvc
                .perform(get(URI_TEMPLATE)
                        .header(AUTHORIZATION, "xxx"))
                .andDo(print()).andExpect(status().is4xxClientError());

        verify(formDefinitionService, Mockito.times(1))
                .getFormDefinition(any(TemplateIdDto.class));
    }

    @Test
    void testTemplateRetrievalException() throws Exception {

        when(formDefinitionService.getFormDefinition(any(TemplateIdDto.class)))
                .thenThrow(new FormDefinitionRetrievalException("xxx"));

        restLogoutMockMvc
                .perform(get(URI_TEMPLATE)
                        .header(AUTHORIZATION, "xxx"))
                .andDo(print()).andExpect(status().is5xxServerError());

        verify(formDefinitionService, Mockito.times(1))
                .getFormDefinition(any(TemplateIdDto.class));
    }

}
