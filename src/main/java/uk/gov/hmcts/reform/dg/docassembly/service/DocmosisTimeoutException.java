package uk.gov.hmcts.reform.dg.docassembly.service;

public class DocmosisTimeoutException extends TimeoutException {

    public DocmosisTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
