package uk.gov.hmcts.reform.dg.docassembly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import uk.gov.hmcts.reform.idam.client.IdamApi;

@SpringBootApplication(
    scanBasePackages = {
        "uk.gov.hmcts.reform.ccd.document.am",
        "uk.gov.hmcts.reform.dg.docassembly",
        "uk.gov.hmcts.reform.auth"}
)
@EnableFeignClients(basePackageClasses = {IdamApi.class})
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
