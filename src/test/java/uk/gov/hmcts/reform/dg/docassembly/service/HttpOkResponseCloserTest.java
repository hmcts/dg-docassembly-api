package uk.gov.hmcts.reform.dg.docassembly.service;

import okhttp3.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HttpOkResponseCloserTest {

    @Mock
    private Response mockResponse;

    @Test
    @DisplayName("closeResponse should call close() on a non-null response")
    void testCloseResponse_NonNull() {

        assertDoesNotThrow(() -> HttpOkResponseCloser.closeResponse(mockResponse));
        verify(mockResponse, times(1)).close();
    }

    @Test
    @DisplayName("closeResponse should do nothing when response is null")
    void testCloseResponse_Null() {
        assertDoesNotThrow(() -> HttpOkResponseCloser.closeResponse(null));
    }

    @Test
    @DisplayName("closeResponse should swallow exception thrown by response.close()")
    void testCloseResponse_CloseThrowsException() {

        RuntimeException simulatedException = new RuntimeException("Simulated close error");
        doThrow(simulatedException).when(mockResponse).close();

        assertDoesNotThrow(() -> HttpOkResponseCloser.closeResponse(mockResponse));

        verify(mockResponse, times(1)).close();

    }

    @Test
    @DisplayName("closeResponse should handle different exception types from response.close()")
    void testCloseResponse_CloseThrowsIOException() {
        doThrow(new IOException("Simulated IO close error")).when(mockResponse).close();

        assertDoesNotThrow(() -> HttpOkResponseCloser.closeResponse(mockResponse));

        verify(mockResponse, times(1)).close();
    }
}