package uk.gov.hmcts.reform.dg.docassembly.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dg.docassembly.appinsights.DependencyProfiler;
import uk.gov.hmcts.reform.dg.docassembly.dto.TemplateIdDto;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Service
public class TemplateManagementApiClient {

    private final String templateManagementApiUrl;

    private final String templateManagementApiAuth;

    private final OkHttpClient httpClient;

    public TemplateManagementApiClient(
            OkHttpClient httpClient,
            @Value("${template-management-api.base-url}${template-management-api.resource}")
                    String templateManagementApiUrl,
            @Value("${template-management-api.auth}")
                    String templateManagementApiAuth) {
        this.httpClient = httpClient;
        this.templateManagementApiUrl = templateManagementApiUrl;
        this.templateManagementApiAuth = templateManagementApiAuth;
    }

    @DependencyProfiler(name = "template-management", action = "get template")
    public InputStream getTemplate(TemplateIdDto templateIdDto) throws IOException {
        String filename = new String(Base64.getDecoder().decode(templateIdDto.getTemplateId()));
        final Request request = new Request.Builder()
                .addHeader("Authorization", String.format("Basic %s", templateManagementApiAuth))
                .url(templateManagementApiUrl + filename)
                .get()
                .build();


        Response response = httpClient.newCall(request).execute();

        if (!response.isSuccessful() && response.code() == 404) {
            throw new TemplateNotFoundException(
                    String.format("Template %s could not be found", templateIdDto.getTemplateId()));
        }

        if (!response.isSuccessful()) {
            throw new FormDefinitionRetrievalException(
                    String.format(
                            "Could not retrieve a template. Http code and message %d, %s",
                            response.code(),
                            response.body().string()
                    )
            );
        }

        return response.body().byteStream();

    }

}
