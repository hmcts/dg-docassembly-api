package uk.gov.hmcts.reform.dg.docassembly.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TemplateIdDto extends JwtDto {

    @NotNull(message = "Template Id cannot be null")
    private String templateId;

}
