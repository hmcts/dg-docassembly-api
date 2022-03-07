package uk.gov.hmcts.reform.dg.docassembly.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("uk.gov.hmcts.reform.dg.docassembly.rest")
public class SwaggerConfiguration {

    public OpenAPI api() {
        return new OpenAPI()
                .info(
                        new Info().title("Document Assembly API")
                                .description("API to retrieve UI definitions from templates and "
                                        + "generate documents based those definitions")
                                .version("v0.0.1")
                );
    }
}
