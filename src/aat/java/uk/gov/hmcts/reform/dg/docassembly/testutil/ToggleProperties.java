package uk.gov.hmcts.reform.dg.docassembly.testutil;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "toggle")
@Data
public class ToggleProperties {

    private boolean enableTemplateRenditionEndpoint;

    private boolean enableDocumentConversionEndpoint;

    private boolean enableSecureDocumentConversionEndpoint;

    private boolean enableSecureDocumentTemplRendEndpoint;

}
