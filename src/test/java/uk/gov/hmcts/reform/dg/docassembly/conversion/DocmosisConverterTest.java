package uk.gov.hmcts.reform.dg.docassembly.conversion;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.pdfbox.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.dg.docassembly.service.exception.DocumentProcessingException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertNotEquals;

public class DocmosisConverterTest {

    private static final String PDF_FILENAME = "Test.pdf";
    private DocmosisConverter converter;

    @Before
    public void setup() {
        OkHttpClient okHttpClient = new OkHttpClient
            .Builder()
            .addInterceptor(DocmosisConverterTest::intercept)
            .build();

        converter = new DocmosisConverter("key", "http://example.org", okHttpClient);
    }

    public void failureTestSetup() {
        OkHttpClient okHttpClient = new OkHttpClient
            .Builder()
            .addInterceptor(DocmosisConverterTest::interceptForFailure)
            .build();

        converter = new DocmosisConverter("", "http://example.org", okHttpClient);
    }


    private static Response intercept(Interceptor.Chain chain) throws IOException {
        InputStream file = ClassLoader.getSystemResourceAsStream(PDF_FILENAME);

        return new Response.Builder()
            .body(ResponseBody.create(IOUtils.toByteArray(file), MediaType.get("application/pdf")))
            .request(chain.request())
            .message("")
            .code(200)
            .protocol(Protocol.HTTP_2)
            .build();
    }

    private static Response interceptForFailure(Interceptor.Chain chain) throws IOException {
        InputStream file = ClassLoader.getSystemResourceAsStream(PDF_FILENAME);

        return new Response.Builder()
            .body(ResponseBody.create(IOUtils.toByteArray(file), MediaType.get("application/pdf")))
            .request(chain.request())
            .message("")
            .code(400)
            .protocol(Protocol.HTTP_2)
            .build();
    }

    @Test
    public void convert() throws IOException {
        File input = new File(ClassLoader.getSystemResource("template1.docx").getPath());
        File output = converter.convertFileToPDF(input);

        assertNotEquals(input.getName(), output.getName());
    }

    @Test
    public void convertExcelTest() throws IOException {
        File input = new File(ClassLoader.getSystemResource("TestExcel.xlsx").getPath());
        File output = converter.convertFileToPDF(input);

        assertNotEquals(input.getName(), output.getName());
    }


    @Test
    public void convertPptTest() throws IOException {
        File input = new File(ClassLoader.getSystemResource("potential_and_kinetic.ppt").getPath());
        File output = converter.convertFileToPDF(input);

        assertNotEquals(input.getName(), output.getName());
    }


    @Test
    public void convertPptxTest() throws IOException {
        File input = new File(ClassLoader.getSystemResource("Performance_Out.pptx").getPath());
        File output = converter.convertFileToPDF(input);

        assertNotEquals(input.getName(), output.getName());
    }

    @Test(expected = DocumentProcessingException.class)
    public void convertFailureTest() throws IOException {
        failureTestSetup();
        File input = new File(ClassLoader.getSystemResource("potential_and_kinetic.ppt").getPath());
        converter.convertFileToPDF(input);

    }

}
