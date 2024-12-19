package uk.gov.hmcts.reform.dg.docassembly.functional;

import net.serenitybdd.annotations.WithTag;
import net.serenitybdd.annotations.WithTags;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.dg.docassembly.testutil.ExtendedCcdHelper;
import uk.gov.hmcts.reform.dg.docassembly.testutil.TestUtil;
import uk.gov.hmcts.reform.dg.docassembly.testutil.ToggleProperties;
import uk.gov.hmcts.reform.em.EmTestConfig;

@SpringBootTest(classes = {TestUtil.class, EmTestConfig.class, ExtendedCcdHelper.class})
@TestPropertySource(value = "classpath:application.yml")
@EnableConfigurationProperties(ToggleProperties.class)
@ExtendWith(SerenityJUnit5Extension.class)
@ComponentScan({ "uk.gov.hmcts.reform" })
@WithTags({@WithTag("testType:Functional")})
public abstract class BaseTest {

    @Autowired
    TestUtil testUtil;

    @Autowired
    ToggleProperties toggleProperties;

    @Autowired
    ExtendedCcdHelper extendedCcdHelper;
}
