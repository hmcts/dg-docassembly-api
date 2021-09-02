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
    private Document document;
    @Mock
    private List<Document> documents;
    @Mock
    private Document.Links links;

    @Mock
    private Document.Link link;

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

    @Test
    public void testUploadDocuments() throws DocumentTaskProcessingException {

        String docUrl = "http://localhost:samplefile";
        File mockFile = new File("src/test/resources/template1.docx");
        UploadResponse uploadResponse = Mockito.mock(UploadResponse.class);

        CreateTemplateRenditionDto createTemplateRenditionDto = populateRequestBody();
        when(caseDocumentClientApi.uploadDocuments(Mockito.anyString(),
            Mockito.anyString(), Mockito.any(DocumentUploadRequest.class)))
            .thenReturn(uploadResponse);
        when(documents.get(0)).thenReturn(document);

        cdamService.uploadDocuments(mockFile, createTemplateRenditionDto);
    }

    private CreateTemplateRenditionDto populateRequestBody() {


        CreateTemplateRenditionDto createTemplateRenditionDto = new CreateTemplateRenditionDto();
        createTemplateRenditionDto.setOutputType(RenditionOutputType.DOC);
        createTemplateRenditionDto.setOutputFilename("SampleTestFile");
        createTemplateRenditionDto.setJurisdictionId("PUBLICLAW");
        createTemplateRenditionDto.setCaseTypeId("XYZ");

        return createTemplateRenditionDto;
    }
}

