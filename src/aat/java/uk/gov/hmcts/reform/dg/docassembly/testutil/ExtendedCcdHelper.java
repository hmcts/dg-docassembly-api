package uk.gov.hmcts.reform.dg.docassembly.testutil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.em.test.ccddata.CcdDataHelper;
import uk.gov.hmcts.reform.em.test.cdam.CdamHelper;
import uk.gov.hmcts.reform.em.test.idam.IdamHelper;

import java.io.IOException;
import java.util.Arrays;

@Service
public class ExtendedCcdHelper {

    @Value("${test.url}")
    private String testUrl;

    @Autowired
    private IdamHelper idamHelper;

    @Autowired
    private CcdDataHelper ccdDataHelper;

    @Autowired
    private CdamHelper cdamHelper;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public final String createCaseTemplate = "{\n"
            + "    \"caseTitle\": null,\n"
            + "    \"caseOwner\": null,\n"
            + "    \"caseCreationDate\": null,\n"
            + "    \"caseDescription\": null,\n"
            + "    \"caseComments\": null,\n"
            + "    \"caseDocuments\": [%s]\n"
            + "  }";
    public final String documentTemplate = "{\n"
                    + "        \"value\": {\n"
                    + "          \"documentName\": \"%s\",\n"
                    + "          \"documentLink\": {\n"
                    + "            \"document_url\": \"%s\",\n"
                    + "            \"document_binary_url\": \"%s/binary\",\n"
                    + "            \"document_filename\": \"%s\",\n"
                    + "            \"document_hash\": \"%s\"\n"
                    + "          }\n"
                    + "        }\n"
                    + "      }";

    private String redactionTestUser = "docassemblyTestUser@docassemblyTest.com";

    public CaseDetails createCase(String documents) throws Exception {
        return ccdDataHelper.createCase(redactionTestUser, "PUBLICLAW", getEnvCcdCaseTypeId(), "createCase",
            objectMapper.readTree(String.format(createCaseTemplate, documents)));
    }

    public String getEnvCcdCaseTypeId() {
        return "CCD_BUNDLE_MVP_TYPE_ASYNC";
    }

    public String getCcdDocumentJson(String documentName, String dmUrl, String fileName, String dochash) {
        return String.format(documentTemplate, documentName, dmUrl, dmUrl, fileName, dochash);
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
                                              String fileName) throws Exception {
        String uploadedUrl = uploadResponse.getDocuments().get(0).links.self.href;
        String docHash = uploadResponse.getDocuments().get(0).hashToken;

        String documentString = getCcdDocumentJson(docName, uploadedUrl,
            fileName, docHash);

        createCase(documentString);

        String docId = uploadedUrl.substring(uploadResponse.getDocuments().get(0).links.self.href
            .lastIndexOf('/') + 1);
        return docId;

    }

    public String uploadSecureDOCDocumentAndReturnUrl() throws Exception {
        UploadResponse uploadResponse = uploadCdamDocument("a@b.com", getEnvCcdCaseTypeId(), "PUBLICLAW",
            "wordDocument.doc",
            "application/msword");

        return createCaseAndUploadDocument(uploadResponse, "wordDocument", "wordDocument.doc");
    }

    public String uploadSecureDocxDocumentAndReturnUrl() throws Exception {
        UploadResponse uploadResponse = uploadCdamDocument("a@b.com",
            getEnvCcdCaseTypeId(), "PUBLICLAW", "largeDocument.docx",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

        return createCaseAndUploadDocument(uploadResponse, "largeDocument", "largeDocument.docx");
    }

    public String uploadSecurePptxDocumentAndReturnUrl() throws Exception {
        UploadResponse uploadResponse = uploadCdamDocument("a@b.com",
            getEnvCcdCaseTypeId(), "PUBLICLAW", "largeDocument.docx",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation");

        return createCaseAndUploadDocument(uploadResponse, "Performance_Out", "Performance_Out.pptx");
    }

    public String uploadSecurePptDocumentAndReturnUrl() throws Exception {
        UploadResponse uploadResponse = uploadCdamDocument("a@b.com",
            getEnvCcdCaseTypeId(), "PUBLICLAW", "potential_and_kinetic.ppt",
            "application/vnd.ms-powerpoint");

        return createCaseAndUploadDocument(uploadResponse, "potential_and_kinetic", "potential_and_kinetic.ppt");
    }

    public String uploadSecureXlsxDocumentAndReturnUrl() throws Exception {
        UploadResponse uploadResponse = uploadCdamDocument("a@b.com",
            getEnvCcdCaseTypeId(), "PUBLICLAW", "TestExcel.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        return createCaseAndUploadDocument(uploadResponse, "TestExcel", "TestExcel.xlsx");
    }

    public String uploadSecureXLSDocumentAndReturnUrl() throws Exception {
        UploadResponse uploadResponse = uploadCdamDocument("a@b.com",
            getEnvCcdCaseTypeId(), "PUBLICLAW", "XLSample.xls",
            "application/vnd.ms-excel");

        return createCaseAndUploadDocument(uploadResponse, "XLSample", "XLSample.xls");
    }

    public String uploadSecureRTFDocumentAndReturnUrl() throws Exception {
        UploadResponse uploadResponse = uploadCdamDocument("a@b.com",
            getEnvCcdCaseTypeId(), "PUBLICLAW", "test.rtf",
            "application/rtf");

        return createCaseAndUploadDocument(uploadResponse, "test", "test.rtf");
    }

    public String uploadSecureTXTDocumentAndReturnUrl() throws Exception {
        UploadResponse uploadResponse = uploadCdamDocument("a@b.com",
            getEnvCcdCaseTypeId(), "PUBLICLAW", "sampleFile.txt",
            "text/plain");

        return createCaseAndUploadDocument(uploadResponse, "sampleFile", "sampleFile.txt");
    }
}



