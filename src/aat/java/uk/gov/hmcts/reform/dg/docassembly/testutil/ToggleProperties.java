package uk.gov.hmcts.reform.dg.docassembly.testutil;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "toggle")
@Data
public class ToggleProperties {

    private boolean enableFormDefinitionEndpoint;

    private boolean enableTemplateRenditionEndpoint;

    private boolean enableSecureDocumentConversionEndpoint;

    private boolean enableSecureDocumentTemplRendEndpoint;

}
