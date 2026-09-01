package uk.gov.hmcts.reform.dg.docassembly.provider;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.hmcts.reform.dg.docassembly.rest.DocumentConversionResource;
import uk.gov.hmcts.reform.dg.docassembly.service.FileToPDFConverterService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Provider("doc_assembly_document_conversion_provider")
@WebMvcTest(value = DocumentConversionResource.class, excludeAutoConfiguration = {
    SecurityAutoConfiguration.class,
    OAuth2ClientAutoConfiguration.class
})
class DocumentConversionProviderTest extends BaseProviderTest {

    private DocumentConversionResource documentConversionResource;

    @MockitoBean
    private FileToPDFConverterService fileToPDFConverterService;

    private File tempPdfFile;

    @Autowired
    public DocumentConversionProviderTest(
            MockMvc mockMvc,
            JsonMapper objectMapper,
            DocumentConversionResource documentConversionResource) {
        super(mockMvc, objectMapper);
        this.documentConversionResource = documentConversionResource;
    }

    @Override
    protected Object[] getControllersUnderTest() {
        return new Object[]{documentConversionResource};
    }

    @BeforeEach
    @SuppressWarnings("java:S5443") // Safe: only used in tests
    void setup() throws IOException {
        tempPdfFile = File.createTempFile("test", ".pdf");
        tempPdfFile.deleteOnExit();
        Files.writeString(tempPdfFile.toPath(), "%PDF-1.4 sample content");
    }

    @State({"a document exists for conversion"})
    public void convertDocumentToPdf() {
        when(fileToPDFConverterService.convertFile(
                any(UUID.class),
                any(String.class),
                any(String.class)))
            .thenReturn(tempPdfFile);
        when(fileToPDFConverterService.convertFile(any(UUID.class), any(String.class))).thenReturn(tempPdfFile);
    }
}