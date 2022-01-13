package uk.gov.hmcts.reform.dg.docassembly.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.dg.docassembly.service.FileToPDFConverterService;
import uk.gov.hmcts.reform.dg.docassembly.service.exception.DocumentProcessingException;
import uk.gov.hmcts.reform.dg.docassembly.service.exception.FileTypeException;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
public class DocumentConversionResourceTest {

    @InjectMocks
    private DocumentConversionResource documentConversionResource;

    @Mock
    private FileToPDFConverterService fileToPDFConverterService;

    @Mock
    private HttpServletRequest request;

    private UUID docId = null;

    private static final File TEST_PDF_FILE = new File(
            ClassLoader.getSystemResource("Test.pdf").getPath());

    @Before
    public void setUp() {

        MockitoAnnotations.openMocks(this);
        docId = UUID.randomUUID();
    }

    @Test
    public void shouldConvertDocument() {

        when(fileToPDFConverterService.convertFile(docId))
            .thenReturn(TEST_PDF_FILE);

        ResponseEntity response = documentConversionResource.convert(request, docId);
        assertEquals(200, response.getStatusCodeValue());

        verify(fileToPDFConverterService, Mockito.atLeastOnce())
            .convertFile(docId);
    }

    @Test
    public void shouldConvertDocumentDisabled() {

        when(fileToPDFConverterService.convertFile(docId))
            .thenReturn(TEST_PDF_FILE);

        ResponseEntity response = documentConversionResource.convert(request, docId);
        assertEquals(200, response.getStatusCodeValue());

        verify(fileToPDFConverterService, Mockito.atLeastOnce())
            .convertFile(docId);
    }

    @Test
    public void shouldConvertSecureDocument() {

        String auth = "xyz";
        String serviceAuth = "abc";
        when(fileToPDFConverterService.convertFile(docId, auth, serviceAuth))
            .thenReturn(TEST_PDF_FILE);
        when(request.getHeader("Authorization")).thenReturn(auth);
        when(request.getHeader("ServiceAuthorization")).thenReturn(serviceAuth);
        documentConversionResource.cdamEnabled = true;

        ResponseEntity response = documentConversionResource.convert(request, docId);
        assertEquals(200, response.getStatusCodeValue());

        verify(fileToPDFConverterService, Mockito.atLeastOnce())
            .convertFile(docId, auth, serviceAuth);

        verify(fileToPDFConverterService, Mockito.atLeast(0))
                .convertFile(docId);
    }

    @Test
    public void shouldFailConvertDocumentToPDF() {

        when(fileToPDFConverterService.convertFile(docId))
                .thenThrow(DocumentProcessingException.class);

        ResponseEntity response = documentConversionResource.convert(request, docId);
        assertEquals(400, response.getStatusCodeValue());

        verify(fileToPDFConverterService, Mockito.atLeastOnce())
                .convertFile(docId);
    }

    @Test
    public void shouldFailConvertOtherThanAcceptedFormatDocumentToPDF() {

        when(fileToPDFConverterService.convertFile(docId))
            .thenThrow(FileTypeException.class);

        ResponseEntity response = documentConversionResource.convert(request, docId);
        assertEquals(400, response.getStatusCodeValue());

        verify(fileToPDFConverterService, Mockito.atLeastOnce())
            .convertFile(docId);
    }
}
