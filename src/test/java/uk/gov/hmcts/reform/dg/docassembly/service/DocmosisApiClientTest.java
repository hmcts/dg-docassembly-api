package uk.gov.hmcts.reform.dg.docassembly.service;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.hmcts.reform.dg.docassembly.dto.CreateTemplateRenditionDto;
import uk.gov.hmcts.reform.dg.docassembly.exception.DocmosisTimeoutException;

import java.net.SocketException;
import java.net.SocketTimeoutException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    private final JsonMapper mapper = JsonMapper.builder().build();

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

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(httpClient, times(1)).newCall(requestCaptor.capture());

        okio.Buffer buffer = new okio.Buffer();
        requestCaptor.getValue().body().writeTo(buffer);
        String multipartBody = buffer.readUtf8();

        // Docmosis multipart "data" is String.valueOf(JsonNode); must remain parseable JSON under Jackson 3.
        int dataNameIdx = multipartBody.indexOf("name=\"data\"");
        assertTrue(dataNameIdx >= 0);
        String afterHeader = multipartBody.substring(dataNameIdx);
        int payloadStart = afterHeader.indexOf('{');
        String fromBrace = afterHeader.substring(payloadStart);
        int boundaryIdx = fromBrace.indexOf("\r\n--");
        String dataJson = boundaryIdx > 0 ? fromBrace.substring(0, boundaryIdx).trim() : fromBrace.trim();
        JsonNode reparsed = mapper.readTree(dataJson);
        assertEquals("PDF", reparsed.get("outputType").asString());
        assertEquals("1", reparsed.get("templateId").asString());
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

    private CreateTemplateRenditionDto getCreateTemplateRenditionDto() {
        CreateTemplateRenditionDto createTemplateRenditionDto = new CreateTemplateRenditionDto();
        createTemplateRenditionDto.setRenditionOutputLocation("x");
        createTemplateRenditionDto.setFormPayload(mapper.readTree("{\"outputType\":\"PDF\", \"templateId\":\"1\"}"));
        createTemplateRenditionDto.setTemplateId("1234");
        createTemplateRenditionDto.setJwt("auth-xxx");
        createTemplateRenditionDto.setServiceAuth("serviceAuth-yyy");
        return createTemplateRenditionDto;
    }
}