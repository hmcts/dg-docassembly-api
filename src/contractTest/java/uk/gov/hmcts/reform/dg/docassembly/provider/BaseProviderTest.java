package uk.gov.hmcts.reform.dg.docassembly.provider;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerConsumerVersionSelectors;
import au.com.dius.pact.provider.junitsupport.loader.SelectorBuilder;
import au.com.dius.pact.provider.spring.spring7.Spring7MockMvcTestTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

@Import(ContractTestProviderConfiguration.class)
@IgnoreNoPactsToVerify
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(PactVerificationInvocationContextProvider.class)
//@PactFolder("pacts")
@PactBroker(
    url = "${PACT_BROKER_FULL_URL:http://localhost:9292}",
    providerBranch = "${pact.provider.branch}",
    enablePendingPacts = "${pactbroker.enablePending:true}"
)
public abstract class BaseProviderTest {

    protected MockMvc mockMvc;

    protected JsonMapper objectMapper;

    @Autowired
    protected BaseProviderTest(MockMvc mockMvc, JsonMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    protected abstract Object[] getControllersUnderTest();

    @BeforeEach
    void setupPactVerification(PactVerificationContext context) {
        Spring7MockMvcTestTarget testTarget = new Spring7MockMvcTestTarget(mockMvc);
        testTarget.setControllers(getControllersUnderTest());
        testTarget.setMessageConverters(new JacksonJsonHttpMessageConverter(objectMapper));
        context.setTarget(testTarget);
    }

    @TestTemplate
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @PactBrokerConsumerVersionSelectors
    public static SelectorBuilder consumerVersionSelectors() {
        return new SelectorBuilder()
            .matchingBranch()
            .mainBranch()
            .deployedOrReleased();
    }
}