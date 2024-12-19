package uk.gov.hmcts.reform.dg.docassembly.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.dg.docassembly.Application;
import uk.gov.hmcts.reform.dg.docassembly.dto.TemplateIdDto;
import uk.gov.hmcts.reform.dg.docassembly.service.FormDefinitionService;
import uk.gov.hmcts.reform.dg.docassembly.service.TemplateRenditionService;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class, TestSecurityConfiguration.class})
public class EndpointTogglesTest extends BaseTest {

    @MockBean
    TemplateRenditionService templateRenditionService;

    @MockBean
    FormDefinitionService formDefinitionService;

    @BeforeClass
    public static void setup() {
        System.setProperty("endpoint-toggles.form-definitions", "false");
    }

    @AfterClass
    public static void cleanup() {
        System.setProperty("endpoint-toggles.form-definitions", "true");
    }


    @Test
    public void testFormDefinitionToggle() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        when(formDefinitionService.getFormDefinition(Mockito.any(TemplateIdDto.class)))
                .thenReturn(Optional.of(objectMapper.readTree("{}")));

        restLogoutMockMvc
                .perform(get("/api/form-definitions/1234")
                        .header("Authorization", "xxx")
                        .header("ServiceAuthorization", "xxx"))
                .andExpect(status().isNotFound());
    }
}
