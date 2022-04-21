package uk.gov.hmcts.reform.dg.docassembly.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.dg.docassembly.conversion.DocmosisConverter;
import uk.gov.hmcts.reform.dg.docassembly.service.exception.DocumentProcessingException;
import uk.gov.hmcts.reform.dg.docassembly.service.exception.DocumentTaskProcessingException;
import uk.gov.hmcts.reform.dg.docassembly.service.exception.FileTypeException;
import uk.gov.hmcts.reform.dg.docassembly.service.impl.FileToPDFConverterServiceImpl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class FileToPDFConverterServiceImplTest {

    @InjectMocks
    private FileToPDFConverterServiceImpl fileToPDFConverterServiceImpl;

    @Mock
    private DmStoreDownloader dmStoreDownloader;

    @Mock
    DocmosisConverter docmosisConverter;

    @Mock
    private CdamService cdamService;

    private static final String auth = "abc";
    private static final String serviceAuth = "xyz";

    private static final UUID docStoreUUID = UUID.randomUUID();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        fileToPDFConverterServiceImpl.fileExtensionsList = Arrays.asList("doc", "docx","pptx", "ppt", "rtf", "txt", "xlsx", "xls","jpeg");
    }

    @Test
    public void convertDocumentSuccessTest() throws DocumentTaskProcessingException, IOException {
        File mockFile = new File("potential_and_kinetic.ppt");
        Mockito.when(dmStoreDownloader.downloadFile(docStoreUUID.toString())).thenReturn(mockFile);
        Mockito.when(docmosisConverter.convertFileToPDF(mockFile)).thenReturn(mockFile);

        File convertedFile = fileToPDFConverterServiceImpl.convertFile(docStoreUUID);
        Assert.assertEquals(convertedFile.getName(), mockFile.getName());
    }

    @Test
    public void convertSecurePptDocumentSuccessTest() throws DocumentTaskProcessingException, IOException {
        File mockFile = new File("potential_and_kinetic.ppt");
        Mockito.when(cdamService.downloadFile(auth, serviceAuth, docStoreUUID)).thenReturn(mockFile);
        Mockito.when(docmosisConverter.convertFileToPDF(mockFile)).thenReturn(mockFile);

        File convertedFile = fileToPDFConverterServiceImpl.convertFile(docStoreUUID, auth, serviceAuth);

        Mockito.verify(cdamService, Mockito.atLeast(1)).downloadFile(auth, serviceAuth, docStoreUUID);
        Assert.assertEquals(convertedFile.getName(), mockFile.getName());
    }

    @Test
    public void convertSecureDocDocumentSuccessTest() throws DocumentTaskProcessingException, IOException {
        File mockFile = new File("template1.DOC");
        Mockito.when(cdamService.downloadFile(auth, serviceAuth, docStoreUUID)).thenReturn(mockFile);
        Mockito.when(docmosisConverter.convertFileToPDF(mockFile)).thenReturn(mockFile);

        File convertedFile = fileToPDFConverterServiceImpl.convertFile(docStoreUUID, auth, serviceAuth);

        Mockito.verify(cdamService, Mockito.atLeast(1)).downloadFile(auth, serviceAuth, docStoreUUID);
        Assert.assertEquals(convertedFile.getName(), mockFile.getName());
    }

    @Test(expected = DocumentProcessingException.class)
    public void convertNotProgressAsCdamException() throws DocumentTaskProcessingException, IOException {

        UUID docStoreUUID = UUID.randomUUID();
        Mockito.when(cdamService.downloadFile(auth, serviceAuth, docStoreUUID)).thenThrow(DocumentTaskProcessingException.class);

        fileToPDFConverterServiceImpl.convertFile(docStoreUUID, auth, serviceAuth);
    }

    @Test(expected = DocumentProcessingException.class)
    public void convertFileAsCdamException() throws DocumentTaskProcessingException, IOException {

        UUID docStoreUUID = UUID.randomUUID();
        Mockito.when(cdamService.downloadFile(auth, serviceAuth, docStoreUUID)).thenThrow(IOException.class);

        fileToPDFConverterServiceImpl.convertFile(docStoreUUID, auth, serviceAuth);
    }

    @Test(expected = DocumentProcessingException.class)
    public void convertNotProgressAsDmStoreDownloaderException() throws DocumentTaskProcessingException {

        UUID docStoreUUID = UUID.randomUUID();
        Mockito.when(dmStoreDownloader.downloadFile(docStoreUUID.toString())).thenThrow(DocumentTaskProcessingException.class);

        fileToPDFConverterServiceImpl.convertFile(docStoreUUID);
    }

    @Test(expected = FileTypeException.class)
    public void convertNotAllowedFileTypeTest() throws DocumentTaskProcessingException, IOException {
        File mockFile = new File("sample.ppsx");
        Mockito.when(dmStoreDownloader.downloadFile(docStoreUUID.toString())).thenReturn(mockFile);


        File convertedFile = fileToPDFConverterServiceImpl.convertFile(docStoreUUID);
        Assert.assertEquals(convertedFile.getName(), mockFile.getName());
    }

    @Test(expected = DocumentProcessingException.class)
    public void convertNotAllowedAsIOExceptionIsThrownTest() throws DocumentTaskProcessingException, IOException {
        File mockFile = new File("potential_and_kinetic.ppt");
        Mockito.when(dmStoreDownloader.downloadFile(docStoreUUID.toString())).thenReturn(mockFile);
        Mockito.when(docmosisConverter.convertFileToPDF(mockFile)).thenThrow(IOException.class);
        File convertedFile = fileToPDFConverterServiceImpl.convertFile(docStoreUUID);
        Assert.assertEquals(convertedFile.getName(), mockFile.getName());
    }

}
