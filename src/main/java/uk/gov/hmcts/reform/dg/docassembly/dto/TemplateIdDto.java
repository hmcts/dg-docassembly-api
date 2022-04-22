package uk.gov.hmcts.reform.dg.docassembly.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class TemplateIdDto extends JwtDto {

    @NotNull(message = "Template Id cannot be null")
    private String templateId;

}
