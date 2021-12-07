package uk.gov.hmcts.reform.dg.docassembly.service;

import okhttp3.Response;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dg.docassembly.dto.CreateTemplateRenditionDto;
import uk.gov.hmcts.reform.dg.docassembly.dto.RenditionOutputType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Service
public class TemplateRenditionService {

    private static Logger log = LoggerFactory.getLogger(TemplateRenditionService.class);

    private final DmStoreUploader dmStoreUploader;
    private final DocmosisApiClient docmosisApiClient;

    public TemplateRenditionService(DmStoreUploader dmStoreUploader, DocmosisApiClient docmosisApiClient) {
        this.dmStoreUploader = dmStoreUploader;
        this.docmosisApiClient = docmosisApiClient;
    }

    public CreateTemplateRenditionDto renderTemplate(CreateTemplateRenditionDto createTemplateRenditionDto)
            throws IOException {

        Response response = this.docmosisApiClient.render(createTemplateRenditionDto);

        if (!response.isSuccessful()) {
            TemplateRenditionException exceptionToThrow = new TemplateRenditionException(
                    String.format("Could not render a template %s. HTTP response and message %d, %s",
                            createTemplateRenditionDto.getTemplateId(), response.code(), response.body().string()));
            log.error(exceptionToThrow.toString(), exceptionToThrow);
            response.close();
            throw exceptionToThrow;
        }

        // Avoiding the utilisation of a user provided parameter and mapping against an enum
        // to protect against a security vulnerability SonarCloud: javasecurity:S2083 (Protect against Path Injection Attacks)
        String tempFileExtension;
        switch (createTemplateRenditionDto.getOutputType()) {
            case DOC:
                tempFileExtension = RenditionOutputType.DOC.getFileExtension();
                break;
            case DOCX:
                tempFileExtension = RenditionOutputType.DOCX.getFileExtension();
                break;
            default:
                tempFileExtension = RenditionOutputType.PDF.getFileExtension();
        }

        File file = File.createTempFile(
                "docmosis-rendition",
                tempFileExtension);

        InputStream in = response.body().byteStream();
        OutputStream out = new FileOutputStream(file);
        try {
            IOUtils.copy(in, out);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
        response.close();

        dmStoreUploader.uploadFile(file, createTemplateRenditionDto);

        return createTemplateRenditionDto;
    }
}
