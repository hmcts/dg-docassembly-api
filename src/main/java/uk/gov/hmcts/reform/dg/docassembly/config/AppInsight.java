package uk.gov.hmcts.reform.dg.docassembly.config;

import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppInsight {

    @Bean
    public TelemetryClient getTelemetryClient() {
        return new TelemetryClient();
    }
}
