package uk.gov.hmcts.reform.dg.docassembly.service;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.dg.docassembly.service.exception.DocumentTaskProcessingException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.EnumSet;
import java.util.Objects;
import java.util.UUID;

@Service
public class CdamService {

    @Autowired
    private CaseDocumentClientApi caseDocumentClientApi;


    public File downloadFile(String auth, String serviceAuth, UUID documentId) throws IOException, DocumentTaskProcessingException {

        ResponseEntity<Resource> response =  caseDocumentClientApi.getDocumentBinary(auth, serviceAuth, documentId);

        if (Objects.nonNull(response.getBody())) {

            InputStream inputStream = ((ByteArrayResource) response.getBody()).getInputStream();

            Document document = caseDocumentClientApi.getMetadataForDocument(auth, serviceAuth, documentId);
            String originalDocumentName = document.originalDocumentName;
            String fileType = FilenameUtils.getExtension(originalDocumentName);

            String fileName = "document." + fileType;
            return copyResponseToFile(inputStream, fileName);
        }

        throw new DocumentTaskProcessingException("Could not access the binary. HTTP response: " + response.getStatusCode());
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

}
