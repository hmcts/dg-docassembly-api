package uk.gov.hmcts.reform.dg.docassembly.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.dg.docassembly.dto.TemplateIdDto;
import uk.gov.hmcts.reform.dg.docassembly.service.FormDefinitionService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = "endpoint-toggles.form-definitions=false")
class FormDefinitionResourceDisabledTest extends RestTestBase {

    private static final String URI_TEMPLATE = "/api/form-definitions/123";
    private static final String AUTHORIZATION = "Authorization";
    private static final String DUMMY_AUTH_TOKEN = "Bearer xxx";

    @MockitoBean
    FormDefinitionService formDefinitionService;

    FormDefinitionResourceDisabledTest(WebApplicationContext context) {
        super(context);
    }

    @Test
    @DisplayName("GET /form-definitions/{templateId} - Not Found (Endpoint Disabled)")
    void shouldReturnNotFoundWhenEndpointDisabled() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        when(formDefinitionService.getFormDefinition(any(TemplateIdDto.class)))
            .thenReturn(Optional.of(objectMapper.readTree("{}")));

        restLogoutMockMvc
            .perform(get(URI_TEMPLATE)
                .header(AUTHORIZATION, DUMMY_AUTH_TOKEN))
            .andDo(print()).andExpect(status().isNotFound());

        Mockito.verify(formDefinitionService, never())
            .getFormDefinition(any(TemplateIdDto.class));
    }
}