package uk.gov.hmcts.reform.dg.docassembly.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.dg.docassembly.Application;
import uk.gov.hmcts.reform.dg.docassembly.dto.DocumentConversionDto;
import uk.gov.hmcts.reform.dg.docassembly.service.FileToPDFConverterService;
import uk.gov.hmcts.reform.dg.docassembly.service.exception.DocumentProcessingException;
import uk.gov.hmcts.reform.dg.docassembly.service.exception.FileTypeException;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class, TestSecurityConfiguration.class})
@AutoConfigureMockMvc
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

        MockitoAnnotations.initMocks(this);
        docId = UUID.randomUUID();
    }

    @Test
    public void shouldConvertDocument() {

        when(fileToPDFConverterService.convertFile(docId))
            .thenReturn(TEST_PDF_FILE);

        ResponseEntity response = documentConversionResource.convert(request, docId, null);
        assertEquals(200, response.getStatusCodeValue());

        verify(fileToPDFConverterService, Mockito.atMost(1))
            .convertFile(docId);
    }

    @Test
    public void shouldConvertSecureDocument() {

        DocumentConversionDto documentConversionDto = new DocumentConversionDto();
        documentConversionDto.setSecureDocStoreEnabled(true);

        String auth = "xyz";
        String serviceAuth = "abc";
        when(fileToPDFConverterService.convertFile(docId, auth, serviceAuth))
            .thenReturn(TEST_PDF_FILE);
        when(request.getHeader("Authorization")).thenReturn(auth);
        when(request.getHeader("ServiceAuthorization")).thenReturn(serviceAuth);

        ResponseEntity response = documentConversionResource.convert(request, docId, documentConversionDto);
        assertEquals(200, response.getStatusCodeValue());

        verify(fileToPDFConverterService, Mockito.atMost(1))
            .convertFile(docId, auth, serviceAuth);
    }

    @Test
    public void shouldFailConvertDocumentToPDF() {

        when(fileToPDFConverterService.convertFile(docId))
                .thenThrow(DocumentProcessingException.class);

        ResponseEntity response = documentConversionResource.convert(request, docId, null);
        assertEquals(400, response.getStatusCodeValue());

        verify(fileToPDFConverterService, Mockito.atMost(1))
                .convertFile(docId);
    }

    @Test
    public void shouldFailConvertOtherThanAcceptedFormatDocumentToPDF() {

        when(fileToPDFConverterService.convertFile(docId))
            .thenThrow(FileTypeException.class);

        ResponseEntity response = documentConversionResource.convert(request, docId, null);
        assertEquals(400, response.getStatusCodeValue());

        verify(fileToPDFConverterService, Mockito.atMost(1))
            .convertFile(docId);
    }
}
