package uk.gov.hmcts.reform.dg.docassembly.service;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.Document.Links;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.dg.docassembly.dto.ByteArrayMultipartFile;
import uk.gov.hmcts.reform.dg.docassembly.dto.CreateTemplateRenditionDto;
import uk.gov.hmcts.reform.dg.docassembly.dto.RenditionOutputType;
import uk.gov.hmcts.reform.dg.docassembly.service.exception.DocumentTaskProcessingException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CdamServiceTest {

    @Mock
    private CaseDocumentClientApi caseDocumentClientApi;

    @InjectMocks
    private CdamService cdamService;

    @Captor
    private ArgumentCaptor<DocumentUploadRequest> uploadRequestCaptor;

    @TempDir
    Path tempDir;

    private static final String AUTH_TOKEN = "Bearer user-token";
    private static final String S2S_TOKEN = "Bearer service-token";
    private static final UUID DOCUMENT_ID = UUID.randomUUID();
    private static final String TEST_FILE_NAME = "test-document.pdf";
    private static final String TEST_FILE_CONTENT = "Test file content";
    private static final String DOCUMENT_URL = "http://localhost/documents/" + DOCUMENT_ID;
    private static final String HASH_TOKEN = "testhashtokenvalue";


    private CreateTemplateRenditionDto createTemplateRenditionDto;
    private File testFile;

    @BeforeEach
    void setup() throws IOException {

        createTemplateRenditionDto = new CreateTemplateRenditionDto();
        createTemplateRenditionDto.setJwt(AUTH_TOKEN);
        createTemplateRenditionDto.setServiceAuth(S2S_TOKEN);

        testFile = tempDir.resolve(TEST_FILE_NAME).toFile();
        FileUtils.writeStringToFile(testFile, TEST_FILE_CONTENT, StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("downloadFile should download and copy file successfully")
    void downloadFileSuccess() throws Exception {

        Document documentMetadata = Document.builder()
            .originalDocumentName(TEST_FILE_NAME)
            .build();
        when(caseDocumentClientApi.getMetadataForDocument(AUTH_TOKEN, S2S_TOKEN, DOCUMENT_ID))
            .thenReturn(documentMetadata);

        byte[] fileContentBytes = TEST_FILE_CONTENT.getBytes(StandardCharsets.UTF_8);
        Resource fileResource = new ByteArrayResource(fileContentBytes);
        ResponseEntity<Resource> responseEntity = ResponseEntity.ok(fileResource);
        when(caseDocumentClientApi.getDocumentBinary(AUTH_TOKEN, S2S_TOKEN, DOCUMENT_ID))
            .thenReturn(responseEntity);

        File downloadedFile = cdamService.downloadFile(AUTH_TOKEN, S2S_TOKEN, DOCUMENT_ID);

        assertNotNull(downloadedFile);
        assertTrue(downloadedFile.exists());
        assertTrue(downloadedFile.getName().endsWith(".pdf"));
        assertEquals(TEST_FILE_CONTENT, FileUtils.readFileToString(downloadedFile, StandardCharsets.UTF_8));

        assertTrue(downloadedFile.getParentFile().exists());
        assertTrue(downloadedFile.getParentFile().isDirectory());

        verify(caseDocumentClientApi).getDocumentBinary(AUTH_TOKEN, S2S_TOKEN, DOCUMENT_ID);
        verify(caseDocumentClientApi).getMetadataForDocument(AUTH_TOKEN, S2S_TOKEN, DOCUMENT_ID);

        FileUtils.deleteDirectory(downloadedFile.getParentFile());
    }

    @Test
    @DisplayName("downloadFile should throw exception if getDocumentBinary returns null response")
    void downloadFileThrowsExceptionWhenBinaryResponseIsNull() {

        when(caseDocumentClientApi.getDocumentBinary(AUTH_TOKEN, S2S_TOKEN, DOCUMENT_ID))
            .thenReturn(null);

        DocumentTaskProcessingException exception = assertThrows(DocumentTaskProcessingException.class, () -> {
            cdamService.downloadFile(AUTH_TOKEN, S2S_TOKEN, DOCUMENT_ID);
        });

        assertTrue(exception.getMessage().contains("Could not access the binary. HTTP response: null"));
        verify(caseDocumentClientApi).getDocumentBinary(AUTH_TOKEN, S2S_TOKEN, DOCUMENT_ID);
        verify(caseDocumentClientApi, never()).getMetadataForDocument(any(), any(), any());
    }

    @Test
    @DisplayName("downloadFile should throw exception if getDocumentBinary response body is null")
    void downloadFileThrowsExceptionWhenBinaryResponseBodyIsNull() {

        ResponseEntity<Resource> responseEntity = ResponseEntity.ok(null);
        when(caseDocumentClientApi.getDocumentBinary(AUTH_TOKEN, S2S_TOKEN, DOCUMENT_ID))
            .thenReturn(responseEntity);

        DocumentTaskProcessingException exception = assertThrows(DocumentTaskProcessingException.class, () -> {
            cdamService.downloadFile(AUTH_TOKEN, S2S_TOKEN, DOCUMENT_ID);
        });

        assertTrue(exception.getMessage().contains("Could not access the binary. HTTP response: 200 OK"));
        verify(caseDocumentClientApi).getDocumentBinary(AUTH_TOKEN, S2S_TOKEN, DOCUMENT_ID);
        verify(caseDocumentClientApi, never()).getMetadataForDocument(any(), any(), any());
    }

    @Test
    @DisplayName("downloadFile should throw exception if getMetadataForDocument fails")
    void downloadFileThrowsExceptionWhenMetadataCallFails() {

        byte[] fileContentBytes = TEST_FILE_CONTENT.getBytes(StandardCharsets.UTF_8);
        Resource fileResource = new ByteArrayResource(fileContentBytes);
        ResponseEntity<Resource> responseEntity = ResponseEntity.ok(fileResource);
        when(caseDocumentClientApi.getDocumentBinary(AUTH_TOKEN, S2S_TOKEN, DOCUMENT_ID))
            .thenReturn(responseEntity);

        RuntimeException apiException = new RuntimeException("Metadata API error");
        when(caseDocumentClientApi.getMetadataForDocument(AUTH_TOKEN, S2S_TOKEN, DOCUMENT_ID))
            .thenThrow(apiException);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            cdamService.downloadFile(AUTH_TOKEN, S2S_TOKEN, DOCUMENT_ID);
        });

        assertEquals(apiException, thrown);
        verify(caseDocumentClientApi).getDocumentBinary(AUTH_TOKEN, S2S_TOKEN, DOCUMENT_ID);
        verify(caseDocumentClientApi).getMetadataForDocument(AUTH_TOKEN, S2S_TOKEN, DOCUMENT_ID);
    }

    @Test
    @DisplayName("downloadFile should throw exception if getInputStream throws IOException")
    void downloadFileThrowsExceptionWhenInputStreamFails() throws IOException {

        Resource mockResource = mock(ByteArrayResource.class);
        IOException ioException = new IOException("Failed to get input stream");
        when(mockResource.getInputStream()).thenThrow(ioException);

        ResponseEntity<Resource> responseEntity = ResponseEntity.ok(mockResource);
        when(caseDocumentClientApi.getDocumentBinary(AUTH_TOKEN, S2S_TOKEN, DOCUMENT_ID))
            .thenReturn(responseEntity);

        IOException thrown = assertThrows(IOException.class, () -> {
            cdamService.downloadFile(AUTH_TOKEN, S2S_TOKEN, DOCUMENT_ID);
        });

        assertEquals(ioException, thrown);
        verify(caseDocumentClientApi).getDocumentBinary(AUTH_TOKEN, S2S_TOKEN, DOCUMENT_ID);
        verify(caseDocumentClientApi, never()).getMetadataForDocument(AUTH_TOKEN, S2S_TOKEN, DOCUMENT_ID);
    }

    @Test
    @DisplayName("uploadDocuments should upload file successfully")
    void uploadDocumentsSuccess() throws DocumentTaskProcessingException {

        Document uploadedDoc = Document.builder()
            .originalDocumentName(TEST_FILE_NAME)
            .hashToken(HASH_TOKEN)
            .links(createTestLinks(DOCUMENT_ID))
            .build();
        UploadResponse uploadResponse = new UploadResponse(List.of(uploadedDoc));

        when(caseDocumentClientApi.uploadDocuments(eq(AUTH_TOKEN), eq(S2S_TOKEN), any(DocumentUploadRequest.class)))
            .thenReturn(uploadResponse);

        cdamService.uploadDocuments(testFile, createTemplateRenditionDto);

        assertEquals(DOCUMENT_URL, createTemplateRenditionDto.getRenditionOutputLocation());
        assertEquals(HASH_TOKEN, createTemplateRenditionDto.getHashToken());

        verify(caseDocumentClientApi).uploadDocuments(eq(AUTH_TOKEN), eq(S2S_TOKEN), uploadRequestCaptor.capture());

        DocumentUploadRequest capturedRequest = uploadRequestCaptor.getValue();
        assertEquals(Classification.PUBLIC.toString(), capturedRequest.getClassification());
        assertEquals(createTemplateRenditionDto.getCaseTypeId(), capturedRequest.getCaseTypeId());
        assertEquals(createTemplateRenditionDto.getJurisdictionId(), capturedRequest.getJurisdictionId());
        assertEquals(1, capturedRequest.getFiles().size());

        ByteArrayMultipartFile capturedFile = (ByteArrayMultipartFile) capturedRequest.getFiles().get(0);
        assertEquals(createTemplateRenditionDto.getFullOutputFilename(), capturedFile.getName());
        assertEquals(createTemplateRenditionDto.getOutputType().getMediaType(), capturedFile.getContentType());
        assertArrayEquals(TEST_FILE_CONTENT.getBytes(StandardCharsets.UTF_8), capturedFile.getContent());
    }

    @Test
    @DisplayName("uploadDocuments should throw DocumentTaskProcessingException on file read IOException")
    void uploadDocumentsThrowsExceptionOnFileReadError() {

        File nonExistentFile = new File(tempDir.toFile(), "non-existent-file.pdf");

        DocumentTaskProcessingException exception = assertThrows(DocumentTaskProcessingException.class, () -> {
            cdamService.uploadDocuments(nonExistentFile, createTemplateRenditionDto);
        });

        assertTrue(exception.getMessage().contains("Could not download the file from CDAM"));
        assertInstanceOf(IOException.class, exception.getCause());
        verify(caseDocumentClientApi, never()).uploadDocuments(any(), any(), any());
    }

    @Test
    @DisplayName("uploadDocuments should throw DocumentTaskProcessingException on MediaType parse error")
    void uploadDocumentsThrowsExceptionOnMediaTypeError() {

        RenditionOutputType mockOutputType = mock(RenditionOutputType.class);
        when(mockOutputType.getMediaType()).thenReturn("this is not a valid media type");
        when(mockOutputType.getFileExtension()).thenReturn(".bad");
        createTemplateRenditionDto.setOutputType(mockOutputType);

        DocumentTaskProcessingException exception = assertThrows(DocumentTaskProcessingException.class, () -> {
            cdamService.uploadDocuments(testFile, createTemplateRenditionDto);
        });

        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        verify(caseDocumentClientApi, never()).uploadDocuments(any(), any(), any());
    }


    @Test
    @DisplayName("uploadDocuments should throw DocumentTaskProcessingException on API client error")
    void uploadDocumentsThrowsExceptionOnApiClientError() {

        RuntimeException apiException = new RuntimeException("CDAM API unavailable");
        when(caseDocumentClientApi.uploadDocuments(eq(AUTH_TOKEN), eq(S2S_TOKEN), any(DocumentUploadRequest.class)))
            .thenThrow(apiException);

        DocumentTaskProcessingException exception = assertThrows(DocumentTaskProcessingException.class, () -> {
            cdamService.uploadDocuments(testFile, createTemplateRenditionDto);
        });

        assertEquals(apiException, exception.getCause());
        verify(caseDocumentClientApi).uploadDocuments(eq(AUTH_TOKEN), eq(S2S_TOKEN), any(DocumentUploadRequest.class));
    }

    @Test
    @DisplayName("uploadDocuments should throw DocumentTaskProcessingException if upload response documents is null")
    void uploadDocumentsThrowsExceptionWhenResponseDocumentsIsNull() {

        UploadResponse mockUploadResponse = mock(UploadResponse.class);
        when(mockUploadResponse.getDocuments()).thenReturn(null);

        when(caseDocumentClientApi.uploadDocuments(eq(AUTH_TOKEN), eq(S2S_TOKEN), any(DocumentUploadRequest.class)))
            .thenReturn(mockUploadResponse);

        DocumentTaskProcessingException exception = assertThrows(DocumentTaskProcessingException.class, () -> {
            cdamService.uploadDocuments(testFile, createTemplateRenditionDto);
        });

        assertInstanceOf(NullPointerException.class, exception.getCause());
        verify(caseDocumentClientApi).uploadDocuments(eq(AUTH_TOKEN), eq(S2S_TOKEN), any(DocumentUploadRequest.class));
    }

    @Test
    @DisplayName("uploadDocuments should throw DocumentTaskProcessingException if upload response documents is empty")
    void uploadDocumentsThrowsExceptionWhenResponseDocumentsIsEmpty() {

        UploadResponse uploadResponse = new UploadResponse(Collections.emptyList());

        when(caseDocumentClientApi.uploadDocuments(eq(AUTH_TOKEN), eq(S2S_TOKEN), any(DocumentUploadRequest.class)))
            .thenReturn(uploadResponse);

        DocumentTaskProcessingException exception = assertThrows(DocumentTaskProcessingException.class, () -> {
            cdamService.uploadDocuments(testFile, createTemplateRenditionDto);
        });

        assertInstanceOf(IndexOutOfBoundsException.class, exception.getCause());
        verify(caseDocumentClientApi).uploadDocuments(eq(AUTH_TOKEN), eq(S2S_TOKEN), any(DocumentUploadRequest.class));
    }



    private Links createTestLinks(UUID documentId) {
        Links links = new Links();
        Document.Link selfLink = new Document.Link();
        Document.Link binaryLink = new Document.Link();

        selfLink.href = "http://localhost/documents/" + documentId;
        binaryLink.href = selfLink.href + "/binary";

        links.self = selfLink;
        links.binary = binaryLink;
        return links;
    }
}