package uk.gov.hmcts.reform.dg.docassembly.service;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dg.docassembly.appinsights.DependencyProfiler;
import uk.gov.hmcts.reform.dg.docassembly.dto.CreateTemplateRenditionDto;
import uk.gov.hmcts.reform.dg.docassembly.exception.DocmosisTimeoutException;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Base64;

@Service
public class DocmosisApiClient {

    private final Logger log = LoggerFactory.getLogger(DocmosisApiClient.class);

    private final String docmosisUrl;

    private final String docmosisAccessKey;

    private final OkHttpClient httpClient;

    public DocmosisApiClient(OkHttpClient httpClient,
                             @Value("${docmosis.render.endpoint}") String docmosisUrl,
                             @Value("${docmosis.accessKey}") String docmosisAccessKey) {
        this.httpClient = httpClient;
        this.docmosisUrl = docmosisUrl;
        this.docmosisAccessKey = docmosisAccessKey;
    }

    @DependencyProfiler(name = "docmosis", action = "render")
    public Response render(CreateTemplateRenditionDto createTemplateRenditionDto) throws IOException {
        StopWatch stopwatch = new StopWatch();
        stopwatch.start();

        try {
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
                            createTemplateRenditionDto.getFullOutputFilename())
                    .addFormDataPart(
                            "data",
                            String.valueOf(createTemplateRenditionDto.getFormPayload()))
                    .addFormDataPart("pdfTagged",  String.valueOf(true))
                    .build();

            Request request = new Request.Builder()
                    .url(docmosisUrl)
                    .method("POST", requestBody)
                    .build();

            Response response = httpClient.newCall(request).execute();

            stopwatch.stop();
            long timeElapsed = stopwatch.getTime();

            log.debug("Time taken for Docmosis call : {} milliseconds for Template Id: {} for jurisdictionId {}",
                    timeElapsed, createTemplateRenditionDto.getTemplateId(),
                    createTemplateRenditionDto.getJurisdictionId());
            return response;
        } catch (SocketException se) {
            stopwatch.stop();
            long timeElapsed = stopwatch.getTime();
            log.info("SocketException for Template Id: {} Time taken {} milliseconds for jurisdictionId {}",
                    createTemplateRenditionDto.getTemplateId(), timeElapsed,
                    createTemplateRenditionDto.getJurisdictionId());
            throw new DocmosisTimeoutException("Docmosis Socket Timeout", se);
        } catch (SocketTimeoutException se) {
            stopwatch.stop();
            long timeElapsed = stopwatch.getTime();
            log.info("SocketTimeoutException for Template Id: {} Time taken {} milliseconds for jurisdictionId {}",
                    createTemplateRenditionDto.getTemplateId(), timeElapsed,
                    createTemplateRenditionDto.getJurisdictionId());
            throw new DocmosisTimeoutException("Docmosis Socket Timeout", se);
        }
    }
}
