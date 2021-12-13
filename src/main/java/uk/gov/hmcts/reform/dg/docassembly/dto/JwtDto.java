package uk.gov.hmcts.reform.dg.docassembly.dto;

import lombok.Data;

@Data
public class JwtDto {

    private String jwt;

    private String serviceAuth;
}
