package uk.gov.hmcts.reform.dg.docassembly.service;

import com.microsoft.applicationinsights.TelemetryClient;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dg.docassembly.dto.CreateTemplateRenditionDto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Service
public class TemplateRenditionService {

    private final TelemetryClient telemetryClient;

    private final String docmosisUrl;

    private final String docmosisAccessKey;

    private final OkHttpClient httpClient;

    private final DmStoreUploader dmStoreUploader;

    public TemplateRenditionService(OkHttpClient httpClient,
                                    DmStoreUploader dmStoreUploader,
                                    @Value("${docmosis.convert.endpoint}") String docmosisUrl,
                                    @Value("${docmosis.accessKey}")String docmosisAccessKey,
                                    TelemetryClient telemetryClient) {
        this.httpClient = httpClient;
        this.dmStoreUploader = dmStoreUploader;
        this.docmosisUrl = docmosisUrl;
        this.docmosisAccessKey = docmosisAccessKey;
        this.telemetryClient = telemetryClient;
    }

    public CreateTemplateRenditionDto renderTemplate(CreateTemplateRenditionDto createTemplateRenditionDto)
            throws IOException {

        String tempFileName = String.format("%s%s",
                UUID.randomUUID().toString(),
                createTemplateRenditionDto.getOutputType().getFileExtension());

        MultipartBody requestBody = new MultipartBody
                .Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                        "templateName",
                        new String(Base64.getDecoder().decode(createTemplateRenditionDto.getTemplateId())))
                .addFormDataPart(
                        "accessKey",
                        docmosisAccessKey)
                .addFormDataPart(
                        "outputName",
                        tempFileName)
                .addFormDataPart(
                        "data",
                        String.valueOf(createTemplateRenditionDto.getFormPayload()))
                .build();

        Request request = new Request.Builder()
                .url(docmosisUrl)
                .method("POST", requestBody)
                .build();

        final long startTime = System.currentTimeMillis();
        Response response = httpClient.newCall(request).execute();
        final long endTime = System.currentTimeMillis();
        telemetryClient.trackMetric("Render template time", endTime - startTime);

        if (!response.isSuccessful()) {
            throw new TemplateRenditionException(
                    String.format("Could not render a template %s. HTTP response and message %d, %s",
                            createTemplateRenditionDto.getTemplateId(), response.code(), response.body().string()));
        }

        File file = File.createTempFile(
            "docmosis-rendition",
            createTemplateRenditionDto.getOutputType().getFileExtension());

        IOUtils.copy(response.body().byteStream(), new FileOutputStream(file));

        final long startTime2 = System.currentTimeMillis();
        dmStoreUploader.uploadFile(file, createTemplateRenditionDto);
        final long endTime2 = System.currentTimeMillis();
        telemetryClient.trackMetric("DM store upload time", endTime2 - startTime2);

        return createTemplateRenditionDto;
    }

}
