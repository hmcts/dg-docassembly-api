package uk.gov.hmcts.reform.dg.docassembly.functional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.em.test.ccddata.CcdDataHelper;
import uk.gov.hmcts.reform.em.test.cdam.CdamHelper;
import uk.gov.hmcts.reform.em.test.idam.IdamHelper;
import uk.gov.hmcts.reform.em.test.s2s.S2sHelper;

@Configuration
public class TestConfig {

    @Bean
    public CdamHelper cdamHelper(
            CaseDocumentClientApi caseDocumentClientApi,
            @Qualifier("xuiS2sHelper") S2sHelper xuiS2sHelper,
            IdamHelper idamHelper) {
        return new CdamHelper(caseDocumentClientApi, xuiS2sHelper, idamHelper);
    }

    @Bean
    public CcdDataHelper ccdDataHelper(
            IdamHelper idamHelper,
            @Qualifier("xuiS2sHelper") S2sHelper xuiS2sHelper,
            CoreCaseDataApi coreCaseDataApi) {
        return new CcdDataHelper(idamHelper, xuiS2sHelper, coreCaseDataApi);
    }
}
