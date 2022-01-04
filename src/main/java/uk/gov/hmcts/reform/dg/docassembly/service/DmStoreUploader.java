package uk.gov.hmcts.reform.dg.docassembly.service;

import okhttp3.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.auth.checker.core.SubjectResolver;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.dg.docassembly.appinsights.DependencyProfiler;
import uk.gov.hmcts.reform.dg.docassembly.dto.CreateTemplateRenditionDto;

import java.io.File;
import java.io.IOException;

@Service
public class DmStoreUploader {

    private static Logger log = LoggerFactory.getLogger(DmStoreUploader.class);

    private final OkHttpClient okHttpClient;

    private final AuthTokenGenerator authTokenGenerator;

    private final SubjectResolver<User> userResolver;

    private final String dmStoreAppBaseUrl;

    private static final String ENDPOINT = "/documents";

    public DmStoreUploader(OkHttpClient okHttpClient,
                           AuthTokenGenerator authTokenGenerator,
                           @Value("${document_management.base-url}") String dmStoreAppBaseUrl,
                           SubjectResolver<User> userResolver) {
        this.okHttpClient = okHttpClient;
        this.authTokenGenerator = authTokenGenerator;
        this.dmStoreAppBaseUrl = dmStoreAppBaseUrl;
        this.userResolver = userResolver;
    }

    @DependencyProfiler(name = "dm-store", action = "upload")
    public CreateTemplateRenditionDto uploadFile(File file, CreateTemplateRenditionDto createTemplateRenditionDto) {
        if (createTemplateRenditionDto.getRenditionOutputLocation() != null) {
            uploadNewDocumentVersion(file, createTemplateRenditionDto);
        } else {
            uploadNewDocument(file, createTemplateRenditionDto);
        }
        return createTemplateRenditionDto;
    }

    private void uploadNewDocument(File file, CreateTemplateRenditionDto createTemplateRenditionDto) {

        try {

            MultipartBody requestBody = new MultipartBody
                    .Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("classification", "PUBLIC")
                    .addFormDataPart("files", createTemplateRenditionDto.getFullOutputFilename(),
                            RequestBody.create(MediaType.get(createTemplateRenditionDto.getOutputType().getMediaType()), file))
                    .build();

            Request request = new Request.Builder()
                    .addHeader("user-id", getUserId(createTemplateRenditionDto))
                    .addHeader("user-roles", "caseworker")
                    .addHeader("ServiceAuthorization", authTokenGenerator.generate())
                    .url(dmStoreAppBaseUrl + ENDPOINT)
                    .method("POST", requestBody)
                    .build();

            Response response = okHttpClient.newCall(request).execute();

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
                throw new DocumentUploaderException("Couldn't upload the file. Response code: " + response.code(), null);
            }

        } catch (RuntimeException | IOException e) {
            log.error(e.getMessage(), e);
            throw new DocumentUploaderException(String.format("Couldn't upload the file:  %s", e.getMessage()), e);
        }
    }

    private void uploadNewDocumentVersion(File file, CreateTemplateRenditionDto createTemplateRenditionDto) {
        try {

            MultipartBody requestBody = new MultipartBody
                    .Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", createTemplateRenditionDto.getFullOutputFilename(),
                            RequestBody.create(MediaType.get(createTemplateRenditionDto.getOutputType().getMediaType()),
                                    file))
                    .build();

            Request request = new Request.Builder()
                    .addHeader("user-id", getUserId(createTemplateRenditionDto))
                    .addHeader("user-roles", "caseworker")
                    .addHeader("ServiceAuthorization", authTokenGenerator.generate())
                    .url(createTemplateRenditionDto.getRenditionOutputLocation())
                    .method("POST", requestBody)
                    .build();

            Response response = okHttpClient.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new DocumentUploaderException("Couldn't upload the file. HTTP Response code from Document Store: " + response.code(), null);
            }

        } catch (RuntimeException | IOException e) {
            log.error(e.getMessage(), e);
            throw new DocumentUploaderException("Couldn't upload the file", e);
        }
    }

    private String getUserId(CreateTemplateRenditionDto createTemplateRenditionDto) {
        User user = userResolver.getTokenDetails(createTemplateRenditionDto.getJwt());
        return user.getPrincipal();
    }

}
