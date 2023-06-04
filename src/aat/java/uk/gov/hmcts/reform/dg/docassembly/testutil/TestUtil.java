package uk.gov.hmcts.reform.dg.docassembly.testutil;

import io.restassured.specification.RequestSpecification;
import jakarta.annotation.PostConstruct;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.em.test.dm.DmHelper;
import uk.gov.hmcts.reform.em.test.idam.IdamHelper;
import uk.gov.hmcts.reform.em.test.s2s.S2sHelper;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TestUtil {

    private String idamAuth;
    private String s2sAuth;
    private final String invalidAuthToken = "238beab2-b563-4fee-80fa-63f224bc56f6";
    private final String invalidServiceAuthToken = "438bexy2-b545-4fef-80ab-63f234ae57f58f6";

    @Autowired
    private IdamHelper idamHelper;
    @Autowired
    private S2sHelper s2sHelper;
    @Autowired
    private DmHelper dmHelper;

    @Value("${test.url}")
    private String testUrl;

    @Value("${document_management.base-url}")
    private String dmApiUrl;

    @Value("${document_management.docker_url}")
    private String dmDocumentApiUrl;

    @Autowired
    @Qualifier("xuiS2sHelper")
    private S2sHelper cdamS2sHelper;

    @PostConstruct
    public void init() {
        idamHelper.createUser("docassemblyTestUser@docassemblyTest.com",
            Stream.of("caseworker", "caseworker-publiclaw", "ccd-import").collect(Collectors.toList()));
        SerenityRest.useRelaxedHTTPSValidation();
        idamAuth = idamHelper.authenticateUser("docassemblyTestUser@docassemblyTest.com");
        s2sAuth = s2sHelper.getS2sToken();
    }

    public RequestSpecification authRequest() {
        return SerenityRest
                .given()
                .header("Authorization", idamAuth)
                .header("ServiceAuthorization", s2sAuth);
    }

    public RequestSpecification cdamAuthRequest() {
        return cdamS2sAuthRequest()
            .header("Authorization", idamAuth);
    }

    public String getTestUrl() {
        return testUrl;
    }

    public RequestSpecification unAuthenticatedRequest() {
        return SerenityRest.given();
    }

    private RequestSpecification s2sAuthRequest() {
        return SerenityRest.given().header("ServiceAuthorization", s2sAuth);
    }

    public RequestSpecification cdamS2sAuthRequest() {
        return SerenityRest
            .given()
            .log().all()
            .header("ServiceAuthorization", cdamS2sHelper.getS2sToken());
    }

    public RequestSpecification emptyIdamAuthRequest() {
        return s2sAuthRequest().header("Authorization", null);
    }

    public RequestSpecification emptyIdamAuthAndEmptyS2SAuth() {
        return SerenityRest.given().header("ServiceAuthorization", null).header("Authorization", null);
    }

    public RequestSpecification randomHeadersInRequest() {
        return SerenityRest.given().header("randomHeader1", "random1").header("randomHeader2", "random2");
    }

    public RequestSpecification validAuthRequestWithEmptyS2SAuth() {
        return emptyS2sAuthRequest().header("Authorization", idamAuth);
    }

    public RequestSpecification validS2SAuthWithEmptyIdamAuth() {
        return s2sAuthRequest().header("Authorization", null);
    }

    private RequestSpecification emptyS2sAuthRequest() {
        return SerenityRest.given().header("ServiceAuthorization", null);
    }

    public RequestSpecification invalidIdamAuthrequest() {
        return s2sAuthRequest().header("Authorization", invalidAuthToken);
    }

    public RequestSpecification noHeadersInRequest() {
        return SerenityRest.given();
    }

    public RequestSpecification invalidS2SAuth() {
        return invalidS2sAuthRequest().header("Authorization", idamAuth);
    }

    private RequestSpecification invalidS2sAuthRequest() {
        return SerenityRest.given().header("ServiceAuthorization", invalidServiceAuthToken);
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
                    dmHelper.uploadAndGetId(
                            ClassLoader.getSystemResourceAsStream(fileName), mimeType, fileName))
                    .links.self.href;

            return getDmApiUrl().equals("http://localhost:4603")
                    ? url.replaceAll(getDmApiUrl(), getDmDocumentApiUrl())
                    : url;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Document getDocumentMetadata(String fileId) {
        return dmHelper.getDocumentMetadata(fileId);
    }

}
