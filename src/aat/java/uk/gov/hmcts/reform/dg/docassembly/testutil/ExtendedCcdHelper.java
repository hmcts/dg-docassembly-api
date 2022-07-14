package uk.gov.hmcts.reform.dg.docassembly.testutil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import uk.gov.hmcts.reform.em.test.ccddefinition.CcdDefinitionHelper;
import uk.gov.hmcts.reform.em.test.cdam.CdamHelper;
import uk.gov.hmcts.reform.em.test.idam.IdamHelper;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ExtendedCcdHelper {

    @Value("${test.url}")
    private String testUrl;

    @Autowired
    private IdamHelper idamHelper;

    @Autowired
    private CcdDataHelper ccdDataHelper;

    @Autowired
    private CcdDefinitionHelper ccdDefinitionHelper;

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
    private List<String> redactionTestUserRoles = Stream.of("caseworker", "caseworker-publiclaw", "ccd-import").collect(Collectors.toList());

    @PostConstruct
    public void init() throws Exception {
    //        importCcdDefinitionFile();
    }

    public void importCcdDefinitionFile() throws Exception {

        ccdDefinitionHelper.importDefinitionFile(redactionTestUser,
                "caseworker-publiclaw",
                getEnvSpecificDefinitionFile());
    }

    public CaseDetails createCase(String documents) throws Exception {
        return ccdDataHelper.createCase(redactionTestUser, "PUBLICLAW", getEnvCcdCaseTypeId(), "createCase",
            objectMapper.readTree(String.format(createCaseTemplate, documents)));
    }

    public CaseDetails getCase(String caseId) {
        return ccdDataHelper.getCase(redactionTestUser, caseId);
    }

    public JsonNode triggerEvent(String caseId, String eventId) throws Exception {
        return objectMapper.readTree(objectMapper.writeValueAsString(ccdDataHelper.triggerEvent(redactionTestUser,
            caseId, eventId)));
    }

    public JsonNode getCaseJson(String caseId) throws Exception {
        return objectMapper.readTree(objectMapper.writeValueAsString(ccdDataHelper.getCase(redactionTestUser, caseId)));
    }

    public String getEnvCcdCaseTypeId() {
        return String.format("REDACTION_%d", testUrl.hashCode());
    }

    public InputStream getEnvSpecificDefinitionFile() throws Exception {
        Workbook workbook = new XSSFWorkbook(ClassLoader.getSystemResourceAsStream(
            "adv_docassembly_functional_tests_ccd_def.xlsx"));
        Sheet caseEventSheet = workbook.getSheet("CaseEvent");


        Sheet caseTypeSheet = workbook.getSheet("CaseType");

        caseTypeSheet.getRow(3).getCell(3).setCellValue(getEnvCcdCaseTypeId());

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            for (Row row : sheet) {
                for (Cell cell : row) {
                    if (cell.getCellType().equals(CellType.STRING)
                            && cell.getStringCellValue().trim().equals("CCD_BUNDLE_MVP_TYPE_ASYNC")) {
                        cell.setCellValue(getEnvCcdCaseTypeId());
                    }
                    if (cell.getCellType().equals(CellType.STRING)
                            && cell.getStringCellValue().trim().equals("bundle-tester@gmail.com")) {
                        cell.setCellValue(redactionTestUser);
                    }
                }
            }
        }

        File outputFile = File.createTempFile("ccd", "ftest-def");

        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            workbook.write(fileOutputStream);
        }

        return new FileInputStream(outputFile);
    }

    public String getCcdDocumentJson(String documentName, String dmUrl, String fileName, String dochash) {
        return String.format(documentTemplate, documentName, dmUrl, dmUrl, fileName, dochash);
    }

    public JsonNode assignEnvCcdCaseTypeIdToCase(JsonNode ccdCase) {
        ((ObjectNode) ccdCase.get("case_details")).put("case_type_id", getEnvCcdCaseTypeId());
        return ccdCase;
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



