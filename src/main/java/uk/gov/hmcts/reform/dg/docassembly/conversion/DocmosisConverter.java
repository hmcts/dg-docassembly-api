package uk.gov.hmcts.reform.dg.docassembly.conversion;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dg.docassembly.service.exception.DocumentProcessingException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class DocmosisConverter {
    private final Logger log = LoggerFactory.getLogger(DocmosisConverter.class);
    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private final String docmosisAccessKey;
    private final String docmosisConvertEndpoint;
    private final OkHttpClient httpClient;

    public DocmosisConverter(@Value("${docmosis.accessKey}") String docmosisAccessKey,
                             @Value("${docmosis.convert.endpoint}") String docmosisConvertEndpoint,
                             OkHttpClient httpClient) {
        this.docmosisAccessKey = docmosisAccessKey;
        this.docmosisConvertEndpoint = docmosisConvertEndpoint;
        this.httpClient = httpClient;
    }

    /**
     * Converting file to pdf.
     *
     * @param file File to be converted into PDF
     * @return the converted file
     */
    private File convert(File file) throws IOException {
        final Request request = this.createRequest(file);
        final Response response = httpClient.newCall(request).execute();

        if (!response.isSuccessful()) {

            throw new DocumentProcessingException(String.format("Docmosis can not convert file %s. HTTP response %d",
                file.getName(), response.code()));
        }
        return createConvertedFile(response);
    }

    /**
     * Calls the Docmosis api to convert the document.
     *
     * @param file File to be converted into PDF     *
     * @return Request Object
     *
     */
    private Request createRequest(final File file) {
        final String originalFileName = file.getName();
        final String convertedFileName = originalFileName + ".pdf";

        MultipartBody requestBody = new MultipartBody
            .Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("accessKey", docmosisAccessKey)
            .addFormDataPart("outputName", convertedFileName)
            .addFormDataPart("file", originalFileName, RequestBody.create(file, MediaType.get(PDF_CONTENT_TYPE)))
            .build();

        return new Request.Builder()
            .header("Accept", PDF_CONTENT_TYPE)
            .url(docmosisConvertEndpoint)
            .method("POST", requestBody)
            .build();
    }

    /**
     * Calls the Docmosis api to convert the document.
     * @return Request converted file
     *
     */
    private File createConvertedFile(Response response) throws IOException {
        final File convertedFile = File.createTempFile("converted-document", ".pdf");

        Files.write(convertedFile.toPath(), response.body().bytes());

        return convertedFile;
    }


    public File convertFileToPDF(File originalFile) throws IOException {
        log.info("Call to Convert {} with Docmosis converter ", originalFile.getName());
        return convert(originalFile);
    }
}
