package uk.gov.hmcts.reform.dg.docassembly.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.dg.docassembly.dto.TemplateIdDto;
import uk.gov.hmcts.reform.dg.docassembly.service.FormDefinitionService;
import uk.gov.hmcts.reform.dg.docassembly.service.TemplateRenditionService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({MockitoExtension.class})
class EndpointTogglesTest extends RestTestBase {

    @MockitoBean
    TemplateRenditionService templateRenditionService;

    @MockitoBean
    FormDefinitionService formDefinitionService;

    EndpointTogglesTest(WebApplicationContext context) {
        super(context);
    }

    @BeforeAll
    public static void setup() {
        System.setProperty("endpoint-toggles.form-definitions", "false");
    }

    @AfterAll
    public static void cleanup() {
        System.setProperty("endpoint-toggles.form-definitions", "true");
    }


    @Test
    void testFormDefinitionToggle() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        when(formDefinitionService.getFormDefinition(any(TemplateIdDto.class)))
                .thenReturn(Optional.of(objectMapper.readTree("{}")));

        restLogoutMockMvc
                .perform(get("/api/form-definitions/1234")
                        .header("Authorization", "xxx")
                        .header("ServiceAuthorization", "xxx"))
                .andExpect(status().isNotFound());
    }
}
