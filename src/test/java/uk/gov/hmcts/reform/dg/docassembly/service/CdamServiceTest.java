package uk.gov.hmcts.reform.dg.docassembly.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.dg.docassembly.dto.CreateTemplateRenditionDto;
import uk.gov.hmcts.reform.dg.docassembly.dto.RenditionOutputType;
import uk.gov.hmcts.reform.dg.docassembly.service.exception.DocumentTaskProcessingException;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CdamServiceTest {

    private CdamService cdamService;

    @Mock
    private CaseDocumentClientApi caseDocumentClientApi;

    @Mock
    private ByteArrayResource byteArrayResource;

    @Mock
    private List<Document> documents;

    @Mock
    private UploadResponse uploadResponse;

    private static final UUID docStoreUUID = UUID.randomUUID();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        cdamService = new CdamService(caseDocumentClientApi);
    }

    @Test
    void downloadFileCdam() throws Exception {

        Document document = Document.builder().originalDocumentName("template1.docx").build();
        File mockFile = new File("src/test/resources/template1.docx");
        InputStream inputStream = new FileInputStream(mockFile);

        when(caseDocumentClientApi.getMetadataForDocument("xxx", "serviceAuth", docStoreUUID))
            .thenReturn(document);
        ResponseEntity responseEntity = ResponseEntity.accepted().body(byteArrayResource);
        when(byteArrayResource.getInputStream()).thenReturn(inputStream);
        when(caseDocumentClientApi.getDocumentBinary("xxx", "serviceAuth", docStoreUUID))
                .thenReturn(responseEntity);

        cdamService.downloadFile("xxx", "serviceAuth", docStoreUUID);

        verify(caseDocumentClientApi, Mockito.atLeast(1))
                .getDocumentBinary("xxx", "serviceAuth", docStoreUUID);
        verify(caseDocumentClientApi, Mockito.atLeast(1))
                .getMetadataForDocument("xxx", "serviceAuth", docStoreUUID);
    }

    void downloadFileCdamNullResponseBody() {

        ResponseEntity responseEntity = ResponseEntity.accepted().body(null);
        when(caseDocumentClientApi.getDocumentBinary("xxx", "serviceAuth", docStoreUUID))
                .thenReturn(responseEntity);

        assertThrows(DocumentTaskProcessingException.class, () -> {
            cdamService.downloadFile("xxx", "serviceAuth", docStoreUUID);
        });
    }

    @Test
    void testUploadDocuments() throws DocumentTaskProcessingException {
        Document testDoc = Document.builder().originalDocumentName("template1.docx")
                .hashToken("token")
                .links(getLinks())
                .build();

        when(caseDocumentClientApi.uploadDocuments(any(), any(), any(DocumentUploadRequest.class)))
                .thenReturn(uploadResponse);
        when(uploadResponse.getDocuments()).thenReturn(documents);
        when(uploadResponse.getDocuments().get(0)).thenReturn(testDoc);

        File mockFile = new File("src/test/resources/template1.docx");
        CreateTemplateRenditionDto createTemplateRenditionDto = populateRequestBody();

        cdamService.uploadDocuments(mockFile, createTemplateRenditionDto);

        verify(caseDocumentClientApi, Mockito.atLeast(1))
                .uploadDocuments(any(), any(), any(DocumentUploadRequest.class));

        assertNotNull(createTemplateRenditionDto.getHashToken());
        assertNotNull(createTemplateRenditionDto.getRenditionOutputLocation());
    }

    private CreateTemplateRenditionDto populateRequestBody() {


        CreateTemplateRenditionDto createTemplateRenditionDto = new CreateTemplateRenditionDto();
        createTemplateRenditionDto.setOutputType(RenditionOutputType.DOC);
        createTemplateRenditionDto.setOutputFilename("SampleTestFile");
        createTemplateRenditionDto.setJurisdictionId("PUBLICLAW");
        createTemplateRenditionDto.setCaseTypeId("XYZ");
        createTemplateRenditionDto.setTemplateId("XYZ");

        return createTemplateRenditionDto;
    }

    static Document.Links getLinks() {
        Document.Links links = new Document.Links();

        Document.Link self = new Document.Link();
        Document.Link binary = new Document.Link();

        var documentId =  UUID.fromString("3a6cfd54-ab2c-49c0-88ec-831f6376a726");
        var selfLink = "http://localhost:samplefile/" + documentId;
        var binaryLink = "http://localhost:samplefile/" + documentId + "/binary";

        self.href = selfLink;
        binary.href = binaryLink;

        links.self = self;
        links.binary = binary;

        return links;
    }
}

