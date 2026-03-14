package uk.gov.hmcts.reform.dg.docassembly.service;

import java.io.File;
import java.util.UUID;

public interface FileToPDFConverterService {
    File convertFile(UUID documentId, String userToken);

    File convertFile(UUID documentId, String auth, String serviceAuth);
}
