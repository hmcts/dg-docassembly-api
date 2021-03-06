package uk.gov.hmcts.reform.dg.docassembly.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class TemplateRenditionException extends RuntimeException {

    public TemplateRenditionException(String message) {
        super(message);
    }

}
