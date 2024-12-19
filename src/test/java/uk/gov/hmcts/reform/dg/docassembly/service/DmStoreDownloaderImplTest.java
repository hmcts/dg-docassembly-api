package uk.gov.hmcts.reform.dg.docassembly.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.mock.MockInterceptor;
import okhttp3.mock.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.dg.docassembly.service.exception.DocumentTaskProcessingException;
import uk.gov.hmcts.reform.dg.docassembly.service.impl.DmStoreDownloaderImpl;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DmStoreDownloaderImplTest {

    @Autowired
    DmStoreDownloader dmStoreDownloader;

    AuthTokenGenerator authTokenGenerator;

    MockInterceptor interceptor;
    OkHttpClient client = mock(OkHttpClient.class);

    @BeforeEach
    public void setup() {

        interceptor = new MockInterceptor();

        authTokenGenerator = Mockito.mock(AuthTokenGenerator.class);

        dmStoreDownloader = new DmStoreDownloaderImpl(client,
            authTokenGenerator,
            "http://localhost:4603",
            new ObjectMapper());

        Logger logger = (Logger) LoggerFactory.getLogger(DmStoreDownloaderImpl.class);
        logger.setLevel(Level.DEBUG);
    }

    @Test
    void invalidDocumentId() {
        assertThrows(DocumentTaskProcessingException.class, () -> {
            dmStoreDownloader.downloadFile("abc");
        });
    }

    @Test
    void testRuntimeExceptionThrown() {

        UUID dmStoreDocId = UUID.randomUUID();
        Mockito.when(authTokenGenerator.generate()).thenThrow(RuntimeException.class);
        assertThrows(DocumentTaskProcessingException.class, () ->
            dmStoreDownloader.downloadFile(dmStoreDocId.toString())
        );
    }

    @Test
    void testIOExceptionThrown() throws IOException {
        UUID dmStoreDocId = UUID.randomUUID();
        var mockCall = mock(Call.class);
        when(client.newCall(Mockito.any())).thenReturn(mockCall);
        when(mockCall.execute()).thenThrow(new IOException());
        assertThrows(DocumentTaskProcessingException.class, () ->
            dmStoreDownloader.downloadFile(dmStoreDocId.toString())
        );
    }

    @Test
    void downloadFile() {
        assertThrows(DocumentTaskProcessingException.class, () ->
            dmStoreDownloader.downloadFile("xxx")
        );
    }

    @Test
    void testDownloadAFile() {
        UUID dmStoreDocId = UUID.randomUUID();
        when(authTokenGenerator.generate()).thenReturn("x");

        interceptor.addRule(new Rule.Builder()
            .get()
            .respond("{\"_embedded\":{\"documents\":[{\"_links\":{\"self\":{\"href\":\"http://success.com/1\"}}}]}}"));
        assertThrows(DocumentTaskProcessingException.class, () ->
                dmStoreDownloader.downloadFile(dmStoreDocId.toString())
        );
    }

    @Test
    void testThrowNewDocumentTaskProcessingException() throws Exception {
        UUID dmStoreDocId = UUID.randomUUID();
        when(authTokenGenerator.generate()).thenReturn("x");

        interceptor.addRule(new Rule.Builder()
            .get()
            .respond("").code(500));
        assertThrows(DocumentTaskProcessingException.class, () ->
            dmStoreDownloader.downloadFile(dmStoreDocId.toString())
        );
    }
}
