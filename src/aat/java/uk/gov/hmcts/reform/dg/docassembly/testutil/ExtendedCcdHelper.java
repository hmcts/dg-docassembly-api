package uk.gov.hmcts.reform.dg.docassembly.testutil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.em.test.ccddata.CcdDataHelper;
import uk.gov.hmcts.reform.em.test.cdam.CdamHelper;

import java.io.IOException;
import java.util.Arrays;

@Service
public class ExtendedCcdHelper {

    public static final String USERNAME = "a@b.com";
    public static final String LARGE_DOCUMENT_DOCX = "largeDocument.docx";
    public static final String JURISDICTION = "PUBLICLAW";

    private CcdDataHelper ccdDataHelper;

    private CdamHelper cdamHelper;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public static final String CREATE_CASE_TEMPLATE = """
            {
                "caseTitle": null,
                "caseOwner": null,
                "caseCreationDate": null,
                "caseDescription": null,
                "caseComments": null,
                "caseDocuments": [%s]
            }
            """;

    public static final String DOCUMENT_TEMPLATE = """
            {
                "value": {
                    "documentName": "%s",
                    "documentLink": {
                        "document_url": "%s",
                        "document_binary_url": "%s/binary",
                        "document_filename": "%s",
                        "document_hash": "%s"
                    }
                }
            }
            """;

    private String redactionTestUser = "docassemblyTestUser@docassemblyTest.com";

    public ExtendedCcdHelper(
            CcdDataHelper ccdDataHelper,
            CdamHelper cdamHelper) {
        this.ccdDataHelper = ccdDataHelper;
        this.cdamHelper = cdamHelper;
    }

    public CaseDetails createCase(String documents) throws JsonProcessingException {
        return ccdDataHelper.createCase(redactionTestUser, JURISDICTION, getEnvCcdCaseTypeId(), "createCase",
            objectMapper.readTree(String.format(CREATE_CASE_TEMPLATE, documents)));
    }

    public String getEnvCcdCaseTypeId() {
        return "CCD_BUNDLE_MVP_TYPE_ASYNC";
    }

    public String getCcdDocumentJson(String documentName, String dmUrl, String fileName, String dochash) {
        return String.format(DOCUMENT_TEMPLATE, documentName, dmUrl, dmUrl, fileName, dochash);
    }

    public UploadResponse uploadCdamDocument(String username, String caseTypeId, String jurisdictionId,
                                             String fileName, String contentType) throws IOException {

        final MultipartFile multipartFile = new MockMultipartFile(
            fileName, fileName, contentType,
            ClassLoader.getSystemResourceAsStream(fileName));

        DocumentUploadRequest uploadRequest = new DocumentUploadRequest(Classification.PUBLIC.toString(), caseTypeId,
            jurisdictionId, Arrays.asList(multipartFile));

        return cdamHelper.uploadDocuments(username, uploadRequest);
    }

    public String createCaseAndUploadDocument(UploadResponse uploadResponse, String docName,
                                              String fileName) throws JsonProcessingException {
        String uploadedUrl = uploadResponse.getDocuments().get(0).links.self.href;
        String docHash = uploadResponse.getDocuments().get(0).hashToken;

        String documentString = getCcdDocumentJson(docName, uploadedUrl,
            fileName, docHash);

        createCase(documentString);

        return uploadedUrl
                .substring(uploadResponse.getDocuments().get(0).links.self.href.lastIndexOf('/') + 1);
    }

    public String uploadSecureDOCDocumentAndReturnUrl() throws IOException {
        UploadResponse uploadResponse = uploadCdamDocument(USERNAME, getEnvCcdCaseTypeId(), JURISDICTION,
            "wordDocument.doc",
            "application/msword");

        return createCaseAndUploadDocument(uploadResponse, "wordDocument", "wordDocument.doc");
    }

    public String uploadSecureDocxDocumentAndReturnUrl() throws IOException {
        UploadResponse uploadResponse = uploadCdamDocument(USERNAME,
            getEnvCcdCaseTypeId(), JURISDICTION, LARGE_DOCUMENT_DOCX,
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

        return createCaseAndUploadDocument(uploadResponse, "largeDocument", LARGE_DOCUMENT_DOCX);
    }

    public String uploadSecurePptxDocumentAndReturnUrl() throws IOException {
        UploadResponse uploadResponse = uploadCdamDocument(USERNAME,
            getEnvCcdCaseTypeId(), JURISDICTION, LARGE_DOCUMENT_DOCX,
            "application/vnd.openxmlformats-officedocument.presentationml.presentation");

        return createCaseAndUploadDocument(uploadResponse, "Performance_Out", "Performance_Out.pptx");
    }

    public String uploadSecurePptDocumentAndReturnUrl() throws IOException {
        UploadResponse uploadResponse = uploadCdamDocument(USERNAME,
            getEnvCcdCaseTypeId(), JURISDICTION, "potential_and_kinetic.ppt",
            "application/vnd.ms-powerpoint");

        return createCaseAndUploadDocument(uploadResponse, "potential_and_kinetic", "potential_and_kinetic.ppt");
    }

    public String uploadSecureXlsxDocumentAndReturnUrl() throws IOException {
        UploadResponse uploadResponse = uploadCdamDocument(USERNAME,
            getEnvCcdCaseTypeId(), JURISDICTION, "TestExcel.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        return createCaseAndUploadDocument(uploadResponse, "TestExcel", "TestExcel.xlsx");
    }

    public String uploadSecureXLSDocumentAndReturnUrl() throws IOException {
        UploadResponse uploadResponse = uploadCdamDocument(USERNAME,
            getEnvCcdCaseTypeId(), JURISDICTION, "XLSample.xls",
            "application/vnd.ms-excel");

        return createCaseAndUploadDocument(uploadResponse, "XLSample", "XLSample.xls");
    }

    public String uploadSecureRTFDocumentAndReturnUrl() throws IOException {
        UploadResponse uploadResponse = uploadCdamDocument(USERNAME,
            getEnvCcdCaseTypeId(), JURISDICTION, "test.rtf",
            "application/rtf");

        return createCaseAndUploadDocument(uploadResponse, "test", "test.rtf");
    }

    public String uploadSecureTXTDocumentAndReturnUrl() throws IOException {
        UploadResponse uploadResponse = uploadCdamDocument(USERNAME,
            getEnvCcdCaseTypeId(), JURISDICTION, "sampleFile.txt",
            "text/plain");

        return createCaseAndUploadDocument(uploadResponse, "sampleFile", "sampleFile.txt");
    }
}



