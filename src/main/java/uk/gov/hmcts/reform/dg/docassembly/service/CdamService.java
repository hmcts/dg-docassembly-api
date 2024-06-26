package uk.gov.hmcts.reform.dg.docassembly.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.dg.docassembly.dto.ByteArrayMultipartFile;
import uk.gov.hmcts.reform.dg.docassembly.dto.CreateTemplateRenditionDto;
import uk.gov.hmcts.reform.dg.docassembly.service.exception.DocumentTaskProcessingException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.UUID;

@Service
public class CdamService {

    private static Logger logger = LoggerFactory.getLogger(CdamService.class);

    private final CaseDocumentClientApi caseDocumentClientApi;

    @Autowired
    public CdamService(CaseDocumentClientApi caseDocumentClientApi) {
        this.caseDocumentClientApi = caseDocumentClientApi;
    }


    public File downloadFile(String auth, String serviceAuth, UUID documentId) throws
            IOException, DocumentTaskProcessingException {

        logger.debug("Downloading document from CDAM with documentId : {} ", documentId);

        ResponseEntity<Resource> response =  caseDocumentClientApi.getDocumentBinary(auth, serviceAuth, documentId);
        HttpStatusCode status = null;

        if (Objects.nonNull(response)) {
            status = response.getStatusCode();
            var byteArrayResource = (ByteArrayResource) response.getBody();
            if (Objects.nonNull(byteArrayResource)) {
                try (var inputStream = byteArrayResource.getInputStream()) {
                    var document = caseDocumentClientApi.getMetadataForDocument(auth, serviceAuth, documentId);
                    var originalDocumentName = document.originalDocumentName;
                    var fileType = FilenameUtils.getExtension(originalDocumentName);
                    var fileName = "document." + fileType;
                    return copyResponseToFile(inputStream, fileName);
                }
            }
        }
        logger.debug("Document download completed from CDAM with documentId : {} ", documentId);
        throw new DocumentTaskProcessingException(String.format("Could not access the binary. HTTP response: %s",
                status));
    }

    private File copyResponseToFile(InputStream inputStream, String fileName) throws DocumentTaskProcessingException {
        try {

            var tempDir = Files.createTempDirectory("pg",
                PosixFilePermissions.asFileAttribute(EnumSet.allOf(PosixFilePermission.class)));
            var tempFile = new File(tempDir.toAbsolutePath().toFile(), fileName);

            Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            return tempFile;
        } catch (IOException e) {
            throw new DocumentTaskProcessingException("Could not copy the file to a temp location", e);
        }
    }

    public void uploadDocuments(
            File file,
            CreateTemplateRenditionDto createTemplateRenditionDto
    ) throws DocumentTaskProcessingException {

        logger.info(
                "Uploading document to CDAM with document name : {}  "
                        + "with JurisdictionId : {} and caseTypeId :{}, templateId {}",
                createTemplateRenditionDto.getFullOutputFilename(),
                createTemplateRenditionDto.getJurisdictionId(),
                createTemplateRenditionDto.getCaseTypeId(),
                createTemplateRenditionDto.getTemplateId()
        );

        try {
            ByteArrayMultipartFile multipartFile =
                ByteArrayMultipartFile.builder()
                    .content(FileUtils.readFileToByteArray(file))
                    .name(createTemplateRenditionDto.getFullOutputFilename())
                    .contentType(MediaType.valueOf(createTemplateRenditionDto.getOutputType().getMediaType()))
                .build();

            DocumentUploadRequest documentUploadRequest = new DocumentUploadRequest(Classification.PUBLIC.toString(),
                createTemplateRenditionDto.getCaseTypeId(), createTemplateRenditionDto.getJurisdictionId(),
                Arrays.asList(multipartFile));

            UploadResponse uploadResponse = caseDocumentClientApi.uploadDocuments(createTemplateRenditionDto.getJwt(),
                createTemplateRenditionDto.getServiceAuth(),
                documentUploadRequest);
            Document document = uploadResponse.getDocuments().get(0);

            createTemplateRenditionDto.setRenditionOutputLocation(document.links.self.href);
            createTemplateRenditionDto.setHashToken(document.hashToken);

            logger.info(
                    "Document upload completed to CDAM with document name : {}  with JurisdictionId : {} "
                            + "and caseTypeId : {} templateId {}",
                    createTemplateRenditionDto.getFullOutputFilename(),
                    createTemplateRenditionDto.getJurisdictionId(),
                    createTemplateRenditionDto.getCaseTypeId(),
                    createTemplateRenditionDto.getTemplateId()
            );
        } catch (IOException e) {
            throw new DocumentTaskProcessingException("Could not download the file from CDAM", e);
        } catch (Exception e) {
            throw new DocumentTaskProcessingException(e.getMessage(), e);
        }
    }
}
