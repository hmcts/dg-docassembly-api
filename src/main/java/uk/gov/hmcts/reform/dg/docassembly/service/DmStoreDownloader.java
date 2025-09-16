package uk.gov.hmcts.reform.dg.docassembly.service;

import uk.gov.hmcts.reform.dg.docassembly.exception.DocumentTaskProcessingException;

import java.io.File;

public interface DmStoreDownloader {

    File downloadFile(String id) throws DocumentTaskProcessingException;

}
