package uk.gov.hmcts.reform.dg.docassembly.service.impl;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dg.docassembly.conversion.DocmosisConverter;
import uk.gov.hmcts.reform.dg.docassembly.exception.DocumentProcessingException;
import uk.gov.hmcts.reform.dg.docassembly.exception.DocumentTaskProcessingException;
import uk.gov.hmcts.reform.dg.docassembly.exception.FileTypeException;
import uk.gov.hmcts.reform.dg.docassembly.service.CdamService;
import uk.gov.hmcts.reform.dg.docassembly.service.DmStoreDownloader;
import uk.gov.hmcts.reform.dg.docassembly.service.FileToPDFConverterService;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class FileToPDFConverterServiceImpl implements FileToPDFConverterService {

    private final Logger log = LoggerFactory.getLogger(FileToPDFConverterServiceImpl.class);

    private DmStoreDownloader dmStoreDownloader;
    private DocmosisConverter docmosisConverter;
    private CdamService cdamService;

    @Value("#{'${docmosis-conversion.multipart.covered-ext}'.split(',')}")
    public List<String> fileExtensionsList;

    public FileToPDFConverterServiceImpl(DmStoreDownloader dmStoreDownloader, DocmosisConverter docmosisConverter,
                                         CdamService cdamService) {
        this.dmStoreDownloader = dmStoreDownloader;
        this.docmosisConverter = docmosisConverter;
        this.cdamService = cdamService;
    }

    @Override
    public File convertFile(UUID documentId) {
        try {
            File originalFile = dmStoreDownloader.downloadFile(documentId.toString());

            return transformFile(originalFile);

        } catch (DocumentTaskProcessingException e) {
            log.error("documentId {}, error message: {}, error: ", documentId, e.getMessage(), e);
            throw new DocumentProcessingException("Error processing PDF Conversion Task");
        } catch (IOException e) {
            log.error("documentId {}, error message: {}, error: ", documentId, e.getMessage(), e);
            throw new DocumentProcessingException("File processing error encountered");
        }
    }

    @Override
    public File convertFile(UUID documentId, String auth, String serviceAuth) {
        try {
            File originalFile = cdamService.downloadFile(auth, serviceAuth, documentId);
            return transformFile(originalFile);
        } catch (DocumentTaskProcessingException e) {
            log.error(e.getMessage(), e);
            throw new DocumentProcessingException("Error processing PDF Conversion Task");
        } catch (Exception e) {
            throw new DocumentProcessingException(e.getMessage());
        }
    }

    private File transformFile(File originalFile) throws IOException {
        String fileType = FilenameUtils.getExtension(originalFile.getName());

        File updatedFile;
        if (fileExtensionsList.stream().anyMatch(ext -> ext.equalsIgnoreCase(fileType))) {
            log.debug("Converting Document to PDF");
            updatedFile = docmosisConverter.convertFileToPDF(originalFile);
            log.info("File {} successfully converted to PDF", originalFile.getName());
        } else {
            throw new FileTypeException("Document Type not eligible for Conversion");
        }
        return updatedFile;
    }
}
