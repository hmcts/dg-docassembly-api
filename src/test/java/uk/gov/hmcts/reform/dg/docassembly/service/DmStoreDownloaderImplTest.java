package uk.gov.hmcts.reform.dg.docassembly.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.mock.MockInterceptor;
import okhttp3.mock.Rule;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.dg.docassembly.service.exception.DocumentTaskProcessingException;
import uk.gov.hmcts.reform.dg.docassembly.service.impl.DmStoreDownloaderImpl;

import java.io.IOException;
import java.util.UUID;

public class DmStoreDownloaderImplTest {

    @Autowired
    DmStoreDownloader dmStoreDownloader;

    AuthTokenGenerator authTokenGenerator;

    MockInterceptor interceptor;

    @Before
    public void setup() {

        interceptor = new MockInterceptor();

        OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .build();

        authTokenGenerator = Mockito.mock(AuthTokenGenerator.class);

        dmStoreDownloader = new DmStoreDownloaderImpl(client,
            authTokenGenerator,
            "http://localhost:4603",
            new ObjectMapper());

        Logger logger = (Logger) LoggerFactory.getLogger(DmStoreDownloaderImpl.class);
        logger.setLevel(Level.DEBUG);
    }

    @Test(expected = DocumentTaskProcessingException.class)
    public void invalidDocumentId() throws DocumentTaskProcessingException {
        dmStoreDownloader.downloadFile("abc");
    }

    @Test (expected = DocumentTaskProcessingException.class)
    public void testRuntimeExceptionThrown() throws DocumentTaskProcessingException {

        UUID dmStoreDocId = UUID.randomUUID();
        Mockito.when(dmStoreDownloader.downloadFile(dmStoreDocId.toString())).thenThrow(RuntimeException.class);
        dmStoreDownloader.downloadFile(dmStoreDocId.toString());

    }

    @Test (expected = DocumentTaskProcessingException.class)
    public void testIOExceptionThrown() throws DocumentTaskProcessingException {

        UUID dmStoreDocId = UUID.randomUUID();
        Mockito.when(dmStoreDownloader.downloadFile(dmStoreDocId.toString())).thenThrow(IOException.class);
        dmStoreDownloader.downloadFile(dmStoreDocId.toString());

    }

    @Test(expected = DocumentTaskProcessingException.class)
    public void downloadFile() throws Exception {
        dmStoreDownloader.downloadFile("xxx");
    }

    @Test(expected = DocumentTaskProcessingException.class)
    public void testDownloadAFile() throws Exception {
        UUID dmStoreDocId = UUID.randomUUID();
        Mockito.when(authTokenGenerator.generate()).thenReturn("x");

        interceptor.addRule(new Rule.Builder()
            .get()
            .respond("{\"_embedded\":{\"documents\":[{\"_links\":{\"self\":{\"href\":\"http://success.com/1\"}}}]}}"));

        dmStoreDownloader.downloadFile(dmStoreDocId.toString());
    }

    @Test(expected = DocumentTaskProcessingException.class)
    public void testThrowNewDocumentTaskProcessingException() throws Exception {
        UUID dmStoreDocId = UUID.randomUUID();
        Mockito.when(authTokenGenerator.generate()).thenReturn("x");

        interceptor.addRule(new Rule.Builder()
            .get()
            .respond("").code(500));

        dmStoreDownloader.downloadFile(dmStoreDocId.toString());
    }
}
