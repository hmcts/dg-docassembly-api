package uk.gov.hmcts.reform.dg.docassembly.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.dg.docassembly.dto.CreateTemplateRenditionDto;
import uk.gov.hmcts.reform.dg.docassembly.exception.DocmosisTimeoutException;

import java.net.SocketException;
import java.net.SocketTimeoutException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocmosisApiClientTest {

    @Mock
    OkHttpClient httpClient;

    private final String docmosisUrl = "http://docmosis-url";

    private final String docmosisAccessKey = "docmosis-access-key";
    private final ObjectMapper mapper = new ObjectMapper();

    private DocmosisApiClient docmosisApiClient;

    @BeforeEach
    void setup() {
        docmosisApiClient = new DocmosisApiClient(this.httpClient, docmosisUrl, docmosisAccessKey);
    }

    @Test
    void shouldReturnResponse() throws Exception {
        CreateTemplateRenditionDto createTemplateRenditionDto = getCreateTemplateRenditionDto();

        var mockCall =  mock(Call.class);
        when(httpClient.newCall(any()))
                .thenReturn(mockCall);
        docmosisApiClient.render(createTemplateRenditionDto);
        verify(httpClient, times(1)).newCall(any());

    }

    @Test
    void shouldThrowDocmosisTimeoutException() throws Exception {
        CreateTemplateRenditionDto createTemplateRenditionDto = getCreateTemplateRenditionDto();

        var mockCall =  mock(Call.class);
        when(httpClient.newCall(any()))
                .thenReturn(mockCall);
        when(mockCall.execute())
                .thenThrow(new SocketException("Docmosis socket timeout"));
        assertThatThrownBy(() -> docmosisApiClient.render(createTemplateRenditionDto))
                .isInstanceOf(DocmosisTimeoutException.class)
                .hasMessageContaining("Docmosis Socket Timeout");
        verify(httpClient, times(1)).newCall(any());
    }

    @Test
    void socketTimeoutShouldThrowDocmosisTimeoutException() throws Exception {
        CreateTemplateRenditionDto createTemplateRenditionDto = getCreateTemplateRenditionDto();

        var mockCall =  mock(Call.class);
        when(httpClient.newCall(any()))
            .thenReturn(mockCall);
        when(mockCall.execute())
            .thenThrow(new SocketTimeoutException("Docmosis socket timeout"));
        assertThatThrownBy(() -> docmosisApiClient.render(createTemplateRenditionDto))
            .isInstanceOf(DocmosisTimeoutException.class)
            .hasMessageContaining("Docmosis Socket Timeout");
        verify(httpClient, times(1)).newCall(any());
    }

    private CreateTemplateRenditionDto getCreateTemplateRenditionDto() throws JsonProcessingException {
        CreateTemplateRenditionDto createTemplateRenditionDto = new CreateTemplateRenditionDto();
        createTemplateRenditionDto.setRenditionOutputLocation("x");
        createTemplateRenditionDto.setFormPayload(mapper.readTree("{\"outputType\":\"PDF\", \"templateId\":\"1\"}"));
        createTemplateRenditionDto.setTemplateId("1234");
        createTemplateRenditionDto.setJwt("auth-xxx");
        createTemplateRenditionDto.setServiceAuth("serviceAuth-yyy");
        return createTemplateRenditionDto;
    }
}