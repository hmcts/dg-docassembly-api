package uk.gov.hmcts.reform.dg.docassembly.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.GATEWAY_TIMEOUT)
public class TimeoutException extends RuntimeException {

    public TimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
