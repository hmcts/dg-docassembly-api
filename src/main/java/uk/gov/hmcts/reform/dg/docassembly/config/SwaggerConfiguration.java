package uk.gov.hmcts.reform.dg.docassembly.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(
                        new Info().title("Document Assembly API")
                                .description("API to generate documents from given templates and metadata, "
                                        + "and convert documents to PDF")
                                .version("v0.0.1")
                );
    }
}
