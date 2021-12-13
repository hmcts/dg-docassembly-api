package uk.gov.hmcts.reform.dg.docassembly.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.dg.docassembly.service.exception.DocumentTaskProcessingException;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CdamServiceTest {
    @InjectMocks
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

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void downloadFileCdam() throws Exception {

        Document document = Document.builder().originalDocumentName("template1.docx").build();
        File mockFile = new File("src/test/resources/template1.docx");
        InputStream inputStream = new FileInputStream(mockFile);

        when(caseDocumentClientApi.getMetadataForDocument("xxx", "serviceAuth", docStoreUUID))
            .thenReturn(document);
        ResponseEntity responseEntity = ResponseEntity.accepted().body(byteArrayResource);
        when(byteArrayResource.getInputStream()).thenReturn(inputStream);
        when(caseDocumentClientApi.getDocumentBinary("xxx", "serviceAuth", docStoreUUID)).thenReturn(responseEntity);

        cdamService.downloadFile("xxx", "serviceAuth", docStoreUUID);

        verify(caseDocumentClientApi, Mockito.atLeast(1)).getDocumentBinary("xxx", "serviceAuth", docStoreUUID);
        verify(caseDocumentClientApi, Mockito.atLeast(1)).getMetadataForDocument("xxx", "serviceAuth", docStoreUUID);
    }

    @Test(expected = DocumentTaskProcessingException.class)
    public void downloadFileCdamNullResponseBody() throws Exception {

        ResponseEntity responseEntity = ResponseEntity.accepted().body(null);
        when(caseDocumentClientApi.getDocumentBinary("xxx", "serviceAuth", docStoreUUID)).thenReturn(responseEntity);

        cdamService.downloadFile("xxx", "serviceAuth", docStoreUUID);

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

