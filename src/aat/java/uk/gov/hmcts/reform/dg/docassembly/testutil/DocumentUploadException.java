package uk.gov.hmcts.reform.dg.docassembly.testutil;

public class DocumentUploadException extends RuntimeException {
    public DocumentUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}