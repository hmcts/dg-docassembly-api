package uk.gov.hmcts.reform.dg.docassembly.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.dg.docassembly.service.DmStoreDownloader;
import uk.gov.hmcts.reform.dg.docassembly.service.exception.DocumentTaskProcessingException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static uk.gov.hmcts.reform.dg.docassembly.service.HttpOkResponseCloser.closeResponse;


@Service
public class DmStoreDownloaderImpl implements DmStoreDownloader {

    private final Logger log = LoggerFactory.getLogger(DmStoreDownloaderImpl.class);

    private final OkHttpClient okHttpClient;

    private final AuthTokenGenerator authTokenGenerator;

    private String dmStoreAppBaseUrl;

    private static final  String DM_STORE_DOWNLOAD_ENDPOINT = "/documents/";

    private final ObjectMapper objectMapper;

    public DmStoreDownloaderImpl(
            OkHttpClient okHttpClient,
            AuthTokenGenerator authTokenGenerator,
            @Value("${document_management.base-url}") String dmStoreAppBaseUrl,
            ObjectMapper objectMapper
    ) {
        this.okHttpClient = okHttpClient;
        this.authTokenGenerator = authTokenGenerator;
        this.dmStoreAppBaseUrl = dmStoreAppBaseUrl;
        this.objectMapper = objectMapper;
    }


    @Override
    public File downloadFile(String id) throws DocumentTaskProcessingException {
        Response response = null;
        try {

            response = getDocumentStoreResponse(dmStoreAppBaseUrl + DM_STORE_DOWNLOAD_ENDPOINT + id);

            if (response.isSuccessful()) {
                JsonNode documentMetaData = objectMapper.readTree(response.body().byteStream());

                if (log.isDebugEnabled()) {
                    String docBinary = objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(documentMetaData);
                    log.debug("Accessing binary of the DM document: {}", docBinary);
                }
                String documentBinaryUrl = new StringBuffer()
                                                .append(dmStoreAppBaseUrl)
                                                    .append(DM_STORE_DOWNLOAD_ENDPOINT)
                                                    .append(id)
                                                    .append("/binary").toString();

                String originalDocumentName = documentMetaData.get("originalDocumentName").asText();
                String fileType = FilenameUtils.getExtension(originalDocumentName);

                if (log.isDebugEnabled()) {
                    log.info("Accessing documentBinaryUrl: {}", documentBinaryUrl);
                }
                Response binaryResponse = getDocumentStoreResponse(documentBinaryUrl);

                return copyResponseToFile(binaryResponse, fileType);

            } else {
                throw new DocumentTaskProcessingException(
                        "Could not access the binary. HTTP response: " + response.code()
                );
            }

        } catch (RuntimeException | IOException e) {
            throw new DocumentTaskProcessingException(
                    String.format("Could not access the binary: %s", e.getMessage()),
                    e
            );
        } finally {
            closeResponse(response);
        }
    }

    private Response getDocumentStoreResponse(String documentUri) throws IOException {

        return okHttpClient.newCall(new Request.Builder()
            .addHeader("user-roles", "caseworker")
            .addHeader("ServiceAuthorization", authTokenGenerator.generate())
            .url(documentUri)
            .build()).execute();
    }

    private File copyResponseToFile(Response response, String fileType) throws DocumentTaskProcessingException {
        try {

            File tempFile = File.createTempFile("dm-store", "." + fileType);
            Files.copy(response.body().byteStream(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            return tempFile;
        } catch (IOException e) {
            throw new DocumentTaskProcessingException("Could not copy the file to a temp location", e);
        }
    }

}

