package uk.gov.hmcts.reform.dg.docassembly.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.dg.docassembly.conversion.DocmosisConverter;
import uk.gov.hmcts.reform.dg.docassembly.exception.DocumentProcessingException;
import uk.gov.hmcts.reform.dg.docassembly.exception.DocumentTaskProcessingException;
import uk.gov.hmcts.reform.dg.docassembly.exception.FileTypeException;
import uk.gov.hmcts.reform.dg.docassembly.service.impl.FileToPDFConverterServiceImpl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileToPDFConverterServiceImplTest {

    @InjectMocks
    private FileToPDFConverterServiceImpl fileToPDFConverterServiceImpl;

    @Mock
    private DmStoreDownloader dmStoreDownloader;

    @Mock
    DocmosisConverter docmosisConverter;

    @Mock
    private CdamService cdamService;

    private static final String AUTH = "abc";
    private static final String SERVICE_AUTH = "xyz";

    private static final UUID docStoreUUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        fileToPDFConverterServiceImpl.fileExtensionsList =
                Arrays.asList("doc", "docx","pptx", "ppt", "rtf", "txt", "xlsx", "xls","jpeg");
    }

    @Test
    void convertDocumentSuccessTest() throws DocumentTaskProcessingException, IOException {
        File mockFile = new File("potential_and_kinetic.ppt");
        Mockito.when(dmStoreDownloader.downloadFile(docStoreUUID.toString())).thenReturn(mockFile);
        Mockito.when(docmosisConverter.convertFileToPDF(mockFile)).thenReturn(mockFile);

        File convertedFile = fileToPDFConverterServiceImpl.convertFile(docStoreUUID);
        assertEquals(convertedFile.getName(), mockFile.getName());
    }

    @Test
    void convertSecurePptDocumentSuccessTest() throws DocumentTaskProcessingException, IOException {
        File mockFile = new File("potential_and_kinetic.ppt");
        Mockito.when(cdamService.downloadFile(AUTH, SERVICE_AUTH, docStoreUUID)).thenReturn(mockFile);
        Mockito.when(docmosisConverter.convertFileToPDF(mockFile)).thenReturn(mockFile);

        File convertedFile = fileToPDFConverterServiceImpl.convertFile(docStoreUUID, AUTH, SERVICE_AUTH);

        Mockito.verify(cdamService, Mockito.atLeast(1)).downloadFile(AUTH, SERVICE_AUTH, docStoreUUID);
        assertEquals(convertedFile.getName(), mockFile.getName());
    }

    @Test
    void convertSecureDocDocumentSuccessTest() throws DocumentTaskProcessingException, IOException {
        File mockFile = new File("template1.DOC");
        Mockito.when(cdamService.downloadFile(AUTH, SERVICE_AUTH, docStoreUUID)).thenReturn(mockFile);
        Mockito.when(docmosisConverter.convertFileToPDF(mockFile)).thenReturn(mockFile);

        File convertedFile = fileToPDFConverterServiceImpl.convertFile(docStoreUUID, AUTH, SERVICE_AUTH);

        Mockito.verify(cdamService, Mockito.atLeast(1)).downloadFile(AUTH, SERVICE_AUTH, docStoreUUID);
        assertEquals(convertedFile.getName(), mockFile.getName());
    }

    @Test
    void convertNotProgressAsCdamException() throws DocumentTaskProcessingException, IOException {
        Mockito.when(cdamService.downloadFile(AUTH, SERVICE_AUTH, docStoreUUID))
                .thenThrow(DocumentTaskProcessingException.class);


        assertThrows(DocumentProcessingException.class, () ->
                fileToPDFConverterServiceImpl.convertFile(docStoreUUID, AUTH, SERVICE_AUTH)
        );
    }

    @Test
    void convertFileAsCdamException() throws DocumentTaskProcessingException, IOException {
        Mockito.when(cdamService.downloadFile(AUTH, SERVICE_AUTH, docStoreUUID)).thenThrow(IOException.class);

        assertThrows(DocumentProcessingException.class, () ->
                fileToPDFConverterServiceImpl.convertFile(docStoreUUID, AUTH, SERVICE_AUTH)
        );
    }

    @Test
    void convertNotProgressAsDmStoreDownloaderException() throws DocumentTaskProcessingException {
        Mockito.when(dmStoreDownloader.downloadFile(docStoreUUID.toString()))
                .thenThrow(DocumentTaskProcessingException.class);

        assertThrows(DocumentProcessingException.class, () ->
                fileToPDFConverterServiceImpl.convertFile(docStoreUUID)
        );
    }

    @Test
    void convertNotAllowedFileTypeTest() throws DocumentTaskProcessingException {
        File mockFile = new File("sample.ppsx");
        Mockito.when(dmStoreDownloader.downloadFile(docStoreUUID.toString())).thenReturn(mockFile);
        assertThrows(FileTypeException.class, () ->
                fileToPDFConverterServiceImpl.convertFile(docStoreUUID)
        );
    }

    @Test
    void convertNotAllowedAsIOExceptionIsThrownTest() throws DocumentTaskProcessingException, IOException {
        File mockFile = new File("potential_and_kinetic.ppt");
        Mockito.when(dmStoreDownloader.downloadFile(docStoreUUID.toString())).thenReturn(mockFile);
        Mockito.when(docmosisConverter.convertFileToPDF(mockFile)).thenThrow(IOException.class);
        assertThrows(DocumentProcessingException.class, () ->
                fileToPDFConverterServiceImpl.convertFile(docStoreUUID)
        );
    }

}
