package uk.gov.hmcts.reform.dg.docassembly.service;

import uk.gov.hmcts.reform.dg.docassembly.service.exception.DocumentTaskProcessingException;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public interface DmStoreDownloader {

    File downloadFile(String id) throws DocumentTaskProcessingException;

    File downloadFile(String auth, String serviceAuth, UUID documentId) throws IOException, DocumentTaskProcessingException;
}
