package uk.gov.hmcts.reform.dg.docassembly.provider;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.dg.docassembly.dto.CreateTemplateRenditionDto;
import uk.gov.hmcts.reform.dg.docassembly.dto.RenditionOutputType;
import uk.gov.hmcts.reform.dg.docassembly.rest.TemplateRenditionResource;
import uk.gov.hmcts.reform.dg.docassembly.service.TemplateRenditionService;
import uk.gov.hmcts.reform.dg.docassembly.service.exception.DocumentTaskProcessingException;

import java.io.IOException;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Provider("doc_assembly_template_rendition_provider")
@WebMvcTest(value = TemplateRenditionResource.class, excludeAutoConfiguration = {
    SecurityAutoConfiguration.class,
    OAuth2ClientAutoConfiguration.class
})
public class TemplateRenditionProviderTest extends BaseProviderTest {

    @Autowired
    private TemplateRenditionResource templateRenditionResource;

    @MockitoBean
    private TemplateRenditionService templateRenditionService;

    @Override
    protected Object[] getControllersUnderTest() {
        return new Object[]{templateRenditionResource};
    }

    @State({"a template can be rendered successfully"})
    public void createTemplateRendition() throws DocumentTaskProcessingException, IOException {
        CreateTemplateRenditionDto mockResponseDto = createMockResponseDto();

        when(templateRenditionService.renderTemplate(any(CreateTemplateRenditionDto.class)))
            .thenReturn(mockResponseDto);
    }

    private CreateTemplateRenditionDto createMockResponseDto() {
        CreateTemplateRenditionDto dto = new CreateTemplateRenditionDto();
        dto.setTemplateId("FL-FRM-GOR-ENG-12345");
        dto.setSecureDocStoreEnabled(true);
        dto.setCaseTypeId("FinancialRemedyContested");
        dto.setJurisdictionId("DIVORCE");
        dto.setHashToken("Abcde12345");
        dto.setOutputType(RenditionOutputType.PDF);
        dto.setErrors(new ArrayList<>());
        dto.setRenditionOutputLocation("http://dm-store:8080/documents/a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8");

        try {
            ObjectMapper localObjectMapper = new ObjectMapper();
            JsonNode formPayload = localObjectMapper.readTree(
                "{\"caseReference\":\"1234-5678-9012-3456\","
                + "\"applicantFirstName\":\"John\","
                + "\"applicantLastName\":\"Smith\"}"
            );
            dto.setFormPayload(formPayload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create mock form payload", e);
        }

        return dto;
    }
}