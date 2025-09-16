package uk.gov.hmcts.reform.dg.docassembly.service;

import okhttp3.Response;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dg.docassembly.dto.CreateTemplateRenditionDto;
import uk.gov.hmcts.reform.dg.docassembly.dto.RenditionOutputType;
import uk.gov.hmcts.reform.dg.docassembly.exception.DocumentTaskProcessingException;
import uk.gov.hmcts.reform.dg.docassembly.exception.TemplateRenditionException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static uk.gov.hmcts.reform.dg.docassembly.service.HttpOkResponseCloser.closeResponse;

@Service
public class TemplateRenditionService {

    private static Logger log = LoggerFactory.getLogger(TemplateRenditionService.class);

    @Value("${endpoint-toggles.enable-secure-document-templ-rend-endpoint}")
    private boolean cdamEnabled;

    private final DmStoreUploader dmStoreUploader;
    private final DocmosisApiClient docmosisApiClient;
    private final CdamService cdamService;

    public TemplateRenditionService(DmStoreUploader dmStoreUploader, DocmosisApiClient docmosisApiClient,
                                    CdamService cdamService) {
        this.dmStoreUploader = dmStoreUploader;
        this.docmosisApiClient = docmosisApiClient;
        this.cdamService = cdamService;
    }

    public CreateTemplateRenditionDto renderTemplate(
            CreateTemplateRenditionDto createTemplateRenditionDto
    ) throws IOException, DocumentTaskProcessingException {
        Response response = null;
        try {
            response = this.docmosisApiClient.render(createTemplateRenditionDto);

            if (!response.isSuccessful()) {
                TemplateRenditionException exceptionToThrow = new TemplateRenditionException(
                        String.format("Could not render a template %s. HTTP response and message %d, %s",
                                createTemplateRenditionDto.getTemplateId(), response.code(), response.body().string()));
                log.error(exceptionToThrow.toString(), exceptionToThrow);
                throw exceptionToThrow;
            }

            File file = createFile(createTemplateRenditionDto.getOutputType(), response);

            if (cdamEnabled || createTemplateRenditionDto.isSecureDocStoreEnabled()) {
                cdamService.uploadDocuments(file, createTemplateRenditionDto);
            } else {
                dmStoreUploader.uploadFile(file, createTemplateRenditionDto);
            }

            return createTemplateRenditionDto;
        } finally {
            closeResponse(response);
        }
    }

    private static File createFile(RenditionOutputType renditionOutputType, Response response) throws IOException {
        // Avoiding the utilisation of a user provided parameter and mapping against an enum
        // to protect against a security vulnerability SonarCloud:
        // javasecurity:S2083 (Protect against Path Injection Attacks)
        String tempFileExtension = switch (renditionOutputType) {
            case DOC -> RenditionOutputType.DOC.getFileExtension();
            case DOCX -> RenditionOutputType.DOCX.getFileExtension();
            default -> RenditionOutputType.PDF.getFileExtension();
        };

        File file = File.createTempFile(
                "docmosis-rendition",
                tempFileExtension);

        try (InputStream in = response.body().byteStream();OutputStream out = new FileOutputStream(file)) {
            IOUtils.copy(in, out);
        }
        return file;
    }
}
