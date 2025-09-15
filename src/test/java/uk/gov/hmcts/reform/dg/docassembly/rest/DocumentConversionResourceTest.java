package uk.gov.hmcts.reform.dg.docassembly.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.dg.docassembly.exception.DocumentProcessingException;
import uk.gov.hmcts.reform.dg.docassembly.exception.FileTypeException;
import uk.gov.hmcts.reform.dg.docassembly.service.FileToPDFConverterService;

import java.io.File;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DocumentConversionResourceTest {

    @InjectMocks
    private DocumentConversionResource documentConversionResource;

    @Mock
    private FileToPDFConverterService fileToPDFConverterService;

    @Mock
    private HttpServletRequest request;

    private UUID docId;

    private static final File TEST_PDF_FILE = new File(
            ClassLoader.getSystemResource("Test.pdf").getPath());

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        docId = UUID.randomUUID();
    }

    @Test
    void shouldConvertDocument() {
        when(fileToPDFConverterService.convertFile(docId)).thenReturn(TEST_PDF_FILE);

        ResponseEntity<?> response = documentConversionResource.convert(request, docId);
        assertEquals(200, response.getStatusCode().value());

        verify(fileToPDFConverterService, Mockito.atLeastOnce()).convertFile(docId);
    }

    @Test
    void shouldConvertSecureDocument() {
        String auth = "xyz";
        String serviceAuth = "abc";

        when(fileToPDFConverterService.convertFile(docId, auth, serviceAuth)).thenReturn(TEST_PDF_FILE);
        when(request.getHeader("Authorization")).thenReturn(auth);
        when(request.getHeader("ServiceAuthorization")).thenReturn(serviceAuth);

        documentConversionResource.cdamEnabled = true;

        ResponseEntity<?> response = documentConversionResource.convert(request, docId);
        assertEquals(200, response.getStatusCode().value());

        verify(fileToPDFConverterService, Mockito.atLeastOnce()).convertFile(docId, auth, serviceAuth);
        verify(fileToPDFConverterService, Mockito.atLeast(0)).convertFile(docId);
    }

    @Test
    void shouldFailConvertDocumentToPDF() {
        when(fileToPDFConverterService.convertFile(docId)).thenThrow(DocumentProcessingException.class);

        ResponseEntity<?> response = documentConversionResource.convert(request, docId);
        assertEquals(400, response.getStatusCode().value());

        verify(fileToPDFConverterService, Mockito.atLeastOnce()).convertFile(docId);
    }

    @Test
    void shouldFailConvertOtherThanAcceptedFormatDocumentToPDF() {
        when(fileToPDFConverterService.convertFile(docId)).thenThrow(FileTypeException.class);

        ResponseEntity<?> response = documentConversionResource.convert(request, docId);
        assertEquals(400, response.getStatusCode().value());

        verify(fileToPDFConverterService, Mockito.atLeastOnce()).convertFile(docId);
    }
}
