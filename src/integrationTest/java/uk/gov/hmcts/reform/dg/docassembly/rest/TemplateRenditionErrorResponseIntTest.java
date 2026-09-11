package uk.gov.hmcts.reform.dg.docassembly.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.dg.docassembly.dto.CreateTemplateRenditionDto;
import uk.gov.hmcts.reform.dg.docassembly.dto.RenditionOutputType;
import uk.gov.hmcts.reform.dg.docassembly.service.TemplateRenditionService;

import java.util.ArrayList;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TemplateRenditionErrorResponseIntTest extends RestTestBase {

    private static final String URI = "/api/template-renditions";
    private static final String AUTHORIZATION = "Authorization";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private static final String DUMMY_AUTH = "Bearer fake-jwt-token";
    private static final String DUMMY_SERVICE_AUTH = "Bearer fake-service-auth-token";
    private static final String ERRORS_PATH = "$.errors";

    @MockitoBean
    private TemplateRenditionService templateRenditionService;

    private final ObjectMapper objectMapper;
    private final TemplateRenditionResource templateRenditionResource;

    private CreateTemplateRenditionDto requestDto;

    @Autowired
    TemplateRenditionErrorResponseIntTest(WebApplicationContext context,
                                          ObjectMapper objectMapper,
                                          TemplateRenditionResource templateRenditionResource) {
        super(context);
        this.objectMapper = objectMapper;
        this.templateRenditionResource = templateRenditionResource;
    }

    @BeforeEach
    void setUp() {
        requestDto = new CreateTemplateRenditionDto();
        requestDto.setTemplateId(String.valueOf(UUID.randomUUID()));
        requestDto.setOutputType(RenditionOutputType.PDF);
        requestDto.setOutputFilename("test-document");
        requestDto.setCaseTypeId("TEST_CASE_TYPE");
        requestDto.setJurisdictionId("TEST_JURISDICTION");
        requestDto.setErrors(new ArrayList<>());
        reset(templateRenditionService);
    }

    @Nested
    @DisplayName("CDAM validation errors — structured JSON body with error messages")
    class CdamValidationErrors {

        @BeforeEach
        void enableCdam() {
            ReflectionTestUtils.setField(templateRenditionResource, "cdamEnabled", true);
        }

        @Test
        @DisplayName("Blank caseTypeId returns 400 with CDAM error message in $.errors")
        void blankCaseTypeIdReturnsErrorMessage() throws Exception {
            requestDto.setCaseTypeId("");

            perform(requestDto)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERRORS_PATH, hasItem(TemplateRenditionResource.CDAM_VALIDATION_MSG)));
        }

        @Test
        @DisplayName("Blank jurisdictionId returns 400 with CDAM error message in $.errors")
        void blankJurisdictionIdReturnsErrorMessage() throws Exception {
            requestDto.setJurisdictionId("");

            perform(requestDto)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERRORS_PATH, hasItem(TemplateRenditionResource.CDAM_VALIDATION_MSG)));
        }

        @Test
        @DisplayName("Both caseTypeId and jurisdictionId blank produces exactly one error message")
        void bothFieldsBlankProducesExactlyOneError() throws Exception {
            requestDto.setCaseTypeId("");
            requestDto.setJurisdictionId("");

            perform(requestDto)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERRORS_PATH, hasSize(1)))
                .andExpect(jsonPath(ERRORS_PATH, hasItem(TemplateRenditionResource.CDAM_VALIDATION_MSG)));
        }
    }

    @Nested
    @DisplayName("Bean validation errors — Spring default handler, no structured response body")
    class BeanValidationErrors {

        @Test
        @DisplayName("Null outputType returns 400")
        void nullOutputTypeReturnsBadRequest() throws Exception {
            requestDto.setOutputType(null);

            perform(requestDto)
                .andDo(print())
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Null templateId returns 400")
        void nullTemplateIdReturnsBadRequest() throws Exception {
            requestDto.setTemplateId(null);

            perform(requestDto)
                .andDo(print())
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Missing required headers — Spring default handler, no structured response body")
    class MissingHeaderErrors {

        @Test
        @DisplayName("Missing Authorization header returns 400")
        void missingAuthorizationHeaderReturnsBadRequest() throws Exception {
            restLogoutMockMvc.perform(post(URI)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(SERVICE_AUTHORIZATION, DUMMY_SERVICE_AUTH)
                    .content(toJson(requestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Missing ServiceAuthorization header returns 400")
        void missingServiceAuthorizationHeaderReturnsBadRequest() throws Exception {
            restLogoutMockMvc.perform(post(URI)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(AUTHORIZATION, DUMMY_AUTH)
                    .content(toJson(requestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        }
    }

    private ResultActions perform(CreateTemplateRenditionDto dto) throws Exception {
        return restLogoutMockMvc.perform(post(URI)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, DUMMY_AUTH)
            .header(SERVICE_AUTHORIZATION, DUMMY_SERVICE_AUTH)
            .content(toJson(dto)));
    }

    private String toJson(Object obj) {
        return objectMapper.writeValueAsString(obj);
    }
}
