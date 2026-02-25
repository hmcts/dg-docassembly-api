package uk.gov.hmcts.reform.dg.docassembly.service;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.dg.docassembly.appinsights.DependencyProfiler;
import uk.gov.hmcts.reform.dg.docassembly.dto.CreateTemplateRenditionDto;
import uk.gov.hmcts.reform.dg.docassembly.exception.DocumentUploaderException;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.io.File;
import java.io.IOException;

import static uk.gov.hmcts.reform.dg.docassembly.service.HttpOkResponseCloser.closeResponse;

@Service
public class DmStoreUploader {

    private final Logger logger = LoggerFactory.getLogger(DmStoreUploader.class);

    private final OkHttpClient okHttpClient;

    private final AuthTokenGenerator authTokenGenerator;

    private final IdamClient idamClient;

    private final String dmStoreAppBaseUrl;

    private static final String ENDPOINT = "/documents";

    public DmStoreUploader(
            OkHttpClient okHttpClient,
            AuthTokenGenerator authTokenGenerator,
            IdamClient idamClient,
            @Value("${document_management.base-url}") String dmStoreAppBaseUrl
    ) {
        this.okHttpClient = okHttpClient;
        this.authTokenGenerator = authTokenGenerator;
        this.idamClient = idamClient;
        this.dmStoreAppBaseUrl = dmStoreAppBaseUrl;
    }

    @DependencyProfiler(name = "dm-store", action = "upload")
    public CreateTemplateRenditionDto uploadFile(File file, CreateTemplateRenditionDto createTemplateRenditionDto) {
        if (createTemplateRenditionDto.getRenditionOutputLocation() != null) {
            logger.info("RenditionOutputLocation is {}", createTemplateRenditionDto.getRenditionOutputLocation());
            uploadNewDocumentVersion(file, createTemplateRenditionDto);
        } else {
            uploadNewDocument(file, createTemplateRenditionDto);
        }
        return createTemplateRenditionDto;
    }

    private void uploadNewDocument(File file, CreateTemplateRenditionDto createTemplateRenditionDto) {
        Response response = null;
        try {

            MultipartBody requestBody = new MultipartBody
                    .Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("classification", "PUBLIC")
                    .addFormDataPart("files", createTemplateRenditionDto.getFullOutputFilename(),
                            RequestBody.create(
                                    file,
                                    MediaType.get(createTemplateRenditionDto.getOutputType().getMediaType())
                            )
                    )
                    .build();

            Request request = new Request.Builder()
                    .addHeader("user-id", getUserId(createTemplateRenditionDto))
                    .addHeader("user-roles", "caseworker")
                    .addHeader("ServiceAuthorization", authTokenGenerator.generate())
                    .url(dmStoreAppBaseUrl + ENDPOINT)
                    .method("POST", requestBody)
                    .build();

            response = okHttpClient.newCall(request).execute();

            if (response.isSuccessful()) {

                JSONObject jsonObject = new JSONObject(response.body().string());
                String documentUri = jsonObject
                        .getJSONObject("_embedded")
                        .getJSONArray("documents")
                        .getJSONObject(0)
                        .getJSONObject("_links")
                        .getJSONObject("self")
                        .getString("href");

                createTemplateRenditionDto.setRenditionOutputLocation(documentUri);
            } else {
                throw new DocumentUploaderException(
                        "Couldn't upload the file. Response code: " + response.code(),
                        null
                );
            }

        } catch (RuntimeException | IOException e) {
            throw new DocumentUploaderException(String.format("Couldn't upload the file:  %s", e.getMessage()), e);
        } finally {
            closeResponse(response);
        }
    }

    private void uploadNewDocumentVersion(File file, CreateTemplateRenditionDto createTemplateRenditionDto) {
        Response response =  null;
        try {
            MultipartBody requestBody = new MultipartBody
                    .Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", createTemplateRenditionDto.getFullOutputFilename(),
                            RequestBody.create(
                                    file,
                                    MediaType.get(createTemplateRenditionDto.getOutputType().getMediaType())))
                    .build();

            Request request = new Request.Builder()
                    .addHeader("user-id", getUserId(createTemplateRenditionDto))
                    .addHeader("user-roles", "caseworker")
                    .addHeader("ServiceAuthorization", authTokenGenerator.generate())
                    .url(createTemplateRenditionDto.getRenditionOutputLocation())
                    .method("POST", requestBody)
                    .build();

            response = okHttpClient.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new DocumentUploaderException(
                        "Couldn't upload the file. HTTP Response code from Document Store: " + response.code(),
                        null
                );
            }

        } catch (RuntimeException | IOException e) {
            throw new DocumentUploaderException("Couldn't upload the file", e);
        } finally {
            closeResponse(response);
        }
    }

    private String getUserId(CreateTemplateRenditionDto createTemplateRenditionDto) {
        return idamClient.getUserInfo(createTemplateRenditionDto.getJwt()).getUid();
    }

}
