package uk.gov.hmcts.reform.dg.docassembly.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class TemplateRenditionException extends RuntimeException {

    public TemplateRenditionException(String message) {
        super(message);
    }

}
