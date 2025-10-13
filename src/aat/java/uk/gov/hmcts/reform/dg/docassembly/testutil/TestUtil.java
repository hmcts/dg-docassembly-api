package uk.gov.hmcts.reform.dg.docassembly.testutil;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.em.test.dm.DmHelper;
import uk.gov.hmcts.reform.em.test.idam.IdamHelper;
import uk.gov.hmcts.reform.em.test.s2s.S2sHelper;

import java.io.IOException;
import java.util.stream.Stream;

@Service
public class TestUtil {

    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    public static final String AUTHORIZATION = "Authorization";
    private String idamAuth;
    private String s2sAuth;
    private static final String INVALID_IDAM = "238beab2-b563-4fee-80fa-63f224bc56f6";
    private static final String INVALID_SERVICE = "438bexy2-b545-4fef-80ab-63f234ae57f58f6";

    private final IdamHelper idamHelper;
    private final S2sHelper s2sHelper;
    private final DmHelper dmHelper;

    private final S2sHelper cdamS2sHelper;

    public final String testUrl;

    public final String dmApiUrl;

    public final String dmDocumentApiUrl;

    public TestUtil(IdamHelper idamHelper,
                    S2sHelper s2sHelper,
                    DmHelper dmHelper,
                    @Qualifier("xuiS2sHelper") S2sHelper cdamS2sHelper,
                    @Value("${test.url}") String testUrl,
                    @Value("${document_management.base-url}") String dmApiUrl,
                    @Value("${document_management.docker_url}") String dmDocumentApiUrl
    ) {
        this.idamHelper = idamHelper;
        this.s2sHelper = s2sHelper;
        this.dmHelper = dmHelper;
        this.cdamS2sHelper = cdamS2sHelper;
        this.testUrl = testUrl;
        this.dmApiUrl = dmApiUrl;
        this.dmDocumentApiUrl = dmDocumentApiUrl;
        RestAssured.baseURI = testUrl;
        this.init();
    }

    private void init() {
        idamHelper.createUser("docassemblyTestUser@docassemblyTest.com",
                Stream.of("caseworker", "caseworker-publiclaw", "ccd-import").toList());
        RestAssured.useRelaxedHTTPSValidation();
        idamAuth = idamHelper.authenticateUser("docassemblyTestUser@docassemblyTest.com");
        s2sAuth = s2sHelper.getS2sToken();
    }

    public RequestSpecification authRequest() {
        return RestAssured
                .given()
                .header(AUTHORIZATION, idamAuth)
                .header(SERVICE_AUTHORIZATION, s2sAuth)
                .header("Content-Type", "application/json");
    }

    public RequestSpecification cdamAuthRequest() {
        return RestAssured
                .given()
                .header(SERVICE_AUTHORIZATION, cdamS2sHelper.getS2sToken())
                .header("Content-Type", "application/json")
                .header(AUTHORIZATION, idamAuth);
    }

    public String getTestUrl() {
        return testUrl;
    }

    public RequestSpecification unAuthenticatedRequest() {
        return RestAssured.given();
    }

    private RequestSpecification s2sAuthRequest() {
        return RestAssured
                .given()
                .header(SERVICE_AUTHORIZATION, s2sAuth);
    }

    public RequestSpecification cdamS2sAuthRequest() {
        return RestAssured
                .given()
                .header(SERVICE_AUTHORIZATION, cdamS2sHelper.getS2sToken());
    }

    public RequestSpecification emptyIdamAuthRequest() {
        return s2sAuthRequest()
                .header(AUTHORIZATION, null);
    }

    public RequestSpecification emptyIdamAuthAndEmptyS2SAuth() {
        return RestAssured
                .given()
                .header(SERVICE_AUTHORIZATION, null)
                .header(AUTHORIZATION, null);
    }

    public RequestSpecification randomHeadersInRequest() {
        return RestAssured
                .given()
                .header("randomHeader1", "random1")
                .header("randomHeader2", "random2");
    }

    public RequestSpecification validAuthRequestWithEmptyS2SAuth() {
        return emptyS2sAuthRequest()
                .header(AUTHORIZATION, idamAuth);
    }

    public RequestSpecification validS2SAuthWithEmptyIdamAuth() {
        return s2sAuthRequest()
                .header(AUTHORIZATION, null);
    }

    private RequestSpecification emptyS2sAuthRequest() {
        return RestAssured
                .given()
                .header(SERVICE_AUTHORIZATION, null);
    }

    public RequestSpecification invalidIdamAuthrequest() {
        return s2sAuthRequest()
                .header(AUTHORIZATION, INVALID_IDAM);
    }

    public RequestSpecification noHeadersInRequest() {
        return RestAssured.given();
    }

    public RequestSpecification invalidS2SAuth() {
        return invalidS2sAuthRequest()
                .header(AUTHORIZATION, idamAuth);
    }

    private RequestSpecification invalidS2sAuthRequest() {
        return RestAssured
                .given()
                .header(SERVICE_AUTHORIZATION, INVALID_SERVICE);
    }

    public String getDmApiUrl() {
        return dmApiUrl;
    }

    public String getDmDocumentApiUrl() {
        return dmDocumentApiUrl;
    }

    public String uploadPptxDocumentAndReturnUrl() {
        return uploadDocumentAndReturnUrl(
                "Performance_Out.pptx",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        );
    }

    public String uploadPptDocumentAndReturnUrl() {
        return uploadDocumentAndReturnUrl("potential_and_kinetic.ppt", "application/vnd.ms-powerpoint");
    }

    public String uploadXlsxDocumentAndReturnUrl() {
        return uploadDocumentAndReturnUrl(
                "TestExcel.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
    }

    public String uploadDocxDocumentAndReturnUrl() {
        return uploadDocumentAndReturnUrl(
                "largeDocument.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        );
    }

    public String uploadDOCDocumentAndReturnUrl() {
        return uploadDocumentAndReturnUrl("wordDocument.doc", "application/msword");
    }

    public String uploadXLSDocumentAndReturnUrl() {
        return uploadDocumentAndReturnUrl("XLSample.xls", "application/vnd.ms-excel");
    }

    public String uploadRTFDocumentAndReturnUrl() {
        return uploadDocumentAndReturnUrl("test.rtf", "application/rtf");
    }

    public String uploadTXTDocumentAndReturnUrl() {
        return uploadDocumentAndReturnUrl("sampleFile.txt", "text/plain");
    }

    public String uploadDocumentAndReturnUrl(String fileName, String mimeType) {
        try {
            String url = dmHelper.getDocumentMetadata(
                    dmHelper.uploadAndGetId(ClassLoader.getSystemResourceAsStream(fileName), mimeType, fileName))
                    .links
                    .self
                    .href;

            return getDmApiUrl().equals("http://localhost:4603")
                    ? url.replaceAll(getDmApiUrl(), getDmDocumentApiUrl())
                    : url;
        } catch (IOException e) {
            throw new DocumentUploadException("Failed to upload document: " + fileName, e);
        }
    }

    public Document getDocumentMetadata(String fileId) {
        return dmHelper.getDocumentMetadata(fileId);
    }

}
