package uk.gov.hmcts.reform.dg.docassembly.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FileTypeException extends RuntimeException {

    public FileTypeException(String message) {
        super(message);
    }
}
