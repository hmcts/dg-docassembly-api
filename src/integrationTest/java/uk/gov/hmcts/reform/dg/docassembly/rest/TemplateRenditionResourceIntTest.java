package uk.gov.hmcts.reform.dg.docassembly.rest;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.dg.docassembly.config.Constants;
import uk.gov.hmcts.reform.dg.docassembly.dto.CreateTemplateRenditionDto;
import uk.gov.hmcts.reform.dg.docassembly.dto.RenditionOutputType;
import uk.gov.hmcts.reform.dg.docassembly.service.TemplateRenditionService;

import java.util.ArrayList;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class TemplateRenditionResourceIntTest extends RestTestBase {

    private static final String URI_TEMPLATE = "/api/template-renditions";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private static final String AUTHORIZATION = "Authorization";
    private static final String ERROR_PATH = "$.errors";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TemplateRenditionResource templateRenditionResource;

    @MockitoBean
    private TemplateRenditionService templateRenditionService;

    @Captor
    private ArgumentCaptor<CreateTemplateRenditionDto> dtoCaptor;

    private CreateTemplateRenditionDto requestDto;
    private CreateTemplateRenditionDto serviceResultDto;
    private static final String DUMMY_AUTH_TOKEN = "Bearer fake-jwt-token";
    private static final String DUMMY_SERVICE_AUTH_TOKEN = "Bearer fake-service-auth-token";

    TemplateRenditionResourceIntTest(WebApplicationContext webApplicationContext) {
        super(webApplicationContext);
    }


    @BeforeEach
    void setUpTestData() {
        requestDto = new CreateTemplateRenditionDto();
        requestDto.setTemplateId(String.valueOf(UUID.randomUUID()));
        requestDto.setOutputFilename("test-document");
        requestDto.setOutputType(RenditionOutputType.PDF);
        requestDto.setCaseTypeId("TEST_CASE_TYPE");
        requestDto.setJurisdictionId("TEST_JURISDICTION");
        requestDto.setSecureDocStoreEnabled(false);
        requestDto.setErrors(new ArrayList<>());

        serviceResultDto = new CreateTemplateRenditionDto();
        serviceResultDto.setTemplateId(String.valueOf(UUID.randomUUID()));
        serviceResultDto.setOutputFilename("test-document");
        serviceResultDto.setOutputType(RenditionOutputType.PDF);
        serviceResultDto.setCaseTypeId("TEST_CASE_TYPE");
        serviceResultDto.setJurisdictionId("TEST_JURISDICTION");
        serviceResultDto.setSecureDocStoreEnabled(false);
        serviceResultDto.setErrors(new ArrayList<>());

        reset(templateRenditionService);

    }

    @Nested
    @DisplayName("When CDAM Endpoint is Disabled")
    class CdamDisabledTest {

        @BeforeEach
        void disableCdam() {
            ReflectionTestUtils.setField(templateRenditionResource, "cdamEnabled", false);
        }

        @Test
        @DisplayName("POST /template-renditions - Success")
        void createTemplateRenditionSuccess() throws Exception {
            given(templateRenditionService.renderTemplate(any(CreateTemplateRenditionDto.class)))
                .willReturn(serviceResultDto);

            ResultActions response = restLogoutMockMvc.perform(post(URI_TEMPLATE)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, DUMMY_AUTH_TOKEN)
                .header(SERVICE_AUTHORIZATION, DUMMY_SERVICE_AUTH_TOKEN)
                .content(asJsonString(requestDto)));

            response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt", is(nullValue())))
                .andExpect(jsonPath("$.serviceAuth", is(nullValue())))
                .andExpect(jsonPath(ERROR_PATH, is(empty())));

            verify(templateRenditionService).renderTemplate(dtoCaptor.capture());
            CreateTemplateRenditionDto capturedDto = dtoCaptor.getValue();
            assertEquals(DUMMY_AUTH_TOKEN, capturedDto.getJwt());
            assertEquals(DUMMY_SERVICE_AUTH_TOKEN, capturedDto.getServiceAuth());
        }

    }

    @Nested
    @DisplayName("When CDAM Endpoint is Enabled")
    class CdamEnabledTest {

        @BeforeEach
        void enableCdam() {
            ReflectionTestUtils.setField(templateRenditionResource, "cdamEnabled", true);
        }

        @Test
        @DisplayName("POST /template-renditions - Success (Valid Input)")
        void createTemplateRenditionSuccessValidInput() throws Exception {
            given(templateRenditionService.renderTemplate(any(CreateTemplateRenditionDto.class)))
                .willReturn(serviceResultDto);

            ResultActions response = restLogoutMockMvc.perform(post(URI_TEMPLATE)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, DUMMY_AUTH_TOKEN)
                .header(SERVICE_AUTHORIZATION, DUMMY_SERVICE_AUTH_TOKEN)
                .content(asJsonString(requestDto)));

            response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt", is(nullValue())))
                .andExpect(jsonPath("$.serviceAuth", is(nullValue())))
                .andExpect(jsonPath(ERROR_PATH, is(empty())));

            verify(templateRenditionService).renderTemplate(any(CreateTemplateRenditionDto.class));
        }

        @Test
        @DisplayName("POST /template-renditions - Bad Request (Missing CaseTypeId)")
        void createTemplateRenditionBadRequestMissingCaseTypeId() throws Exception {

            requestDto.setCaseTypeId("");

            ResultActions response = restLogoutMockMvc.perform(post(URI_TEMPLATE)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, DUMMY_AUTH_TOKEN)
                .header(SERVICE_AUTHORIZATION, DUMMY_SERVICE_AUTH_TOKEN)
                .content(asJsonString(requestDto)));

            response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(ERROR_PATH, hasItem(Constants.CDAM_VALIDATION_MSG)));

            verify(templateRenditionService, never()).renderTemplate(any(CreateTemplateRenditionDto.class));
        }

        @Test
        @DisplayName("POST /template-renditions - Bad Request (Missing JurisdictionId)")
        void createTemplateRenditionBadRequestMissingJurisdictionId() throws Exception {
            requestDto.setJurisdictionId("");

            ResultActions response = restLogoutMockMvc.perform(post(URI_TEMPLATE)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, DUMMY_AUTH_TOKEN)
                .header(SERVICE_AUTHORIZATION, DUMMY_SERVICE_AUTH_TOKEN)
                .content(asJsonString(requestDto)));

            response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(ERROR_PATH, hasItem(Constants.CDAM_VALIDATION_MSG)));

            verify(templateRenditionService, never()).renderTemplate(any(CreateTemplateRenditionDto.class));
        }

    }

    @Test
    @DisplayName("POST /template-renditions - Bad Request (Validation Failure)")
    void createTemplateRenditionBadRequestBeanValidationFailure() throws Exception {
        requestDto.setOutputType(null);

        ResultActions response = restLogoutMockMvc.perform(post(URI_TEMPLATE)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, DUMMY_AUTH_TOKEN)
            .header(SERVICE_AUTHORIZATION, DUMMY_SERVICE_AUTH_TOKEN)
            .content(asJsonString(requestDto)));

        response.andDo(print())
            .andExpect(status().isBadRequest());

        verify(templateRenditionService, never()).renderTemplate(any(CreateTemplateRenditionDto.class));
    }

    @Test
    @DisplayName("POST /template-renditions - Bad Request (Missing Authorization Header)")
    void createTemplateRenditionBadRequestMissingAuthHeader() throws Exception {

        ResultActions response = restLogoutMockMvc.perform(post(URI_TEMPLATE)
            .contentType(MediaType.APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, DUMMY_SERVICE_AUTH_TOKEN)
            .content(asJsonString(requestDto)));

        response.andDo(print())
            .andExpect(status().isBadRequest());

        verify(templateRenditionService, never()).renderTemplate(any(CreateTemplateRenditionDto.class));
    }

    @Test
    @DisplayName("POST /template-renditions - Bad Request (Missing ServiceAuthorization Header)")
    void createTemplateRenditionBadRequestMissingServiceAuthHeader() throws Exception {

        ResultActions response = restLogoutMockMvc.perform(post(URI_TEMPLATE)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, DUMMY_AUTH_TOKEN)
            .content(asJsonString(requestDto)));

        response.andDo(print())
            .andExpect(status().isBadRequest());

        verify(templateRenditionService, never()).renderTemplate(any(CreateTemplateRenditionDto.class));
    }


    private String asJsonString(final Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

}