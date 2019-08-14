package uk.gov.hmcts.reform.dg.docassembly.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.core.service.ServiceRequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.core.user.UserRequestAuthorizer;
import uk.gov.hmcts.reform.dg.docassembly.dto.TemplateIdDto;
import uk.gov.hmcts.reform.dg.docassembly.service.FormDefinitionRetrievalException;
import uk.gov.hmcts.reform.dg.docassembly.service.FormDefinitionService;
import uk.gov.hmcts.reform.dg.docassembly.service.TemplateNotFoundException;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class FormDefinitionResourceTest {

    @MockBean
    FormDefinitionService formDefinitionService;

    @MockBean
    private ServiceRequestAuthorizer serviceRequestAuthorizer;

    @MockBean
    private UserRequestAuthorizer userRequestAuthorizer;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldCallFormDefinitionService() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        when(serviceRequestAuthorizer.authorise(Mockito.any(HttpServletRequest.class)))
                .thenReturn(new Service("ccd"));

        when(userRequestAuthorizer.authorise(Mockito.any(HttpServletRequest.class)))
                .thenReturn(new User("john", Stream.of("caseworker").collect(Collectors.toSet())));


        when(formDefinitionService.getFormDefinition(Mockito.any(TemplateIdDto.class)))
                .thenReturn(Optional.of(objectMapper.readTree("{}")));

        this.mockMvc
                .perform(get("/api/form-definitions/123")
                        .header("Authorization", "x.y.z")
                        .header("ServiceAuthorization", "xxx"))
                .andDo(print()).andExpect(status().isOk());

        verify(formDefinitionService, Mockito.times(1))
                .getFormDefinition(Mockito.any(TemplateIdDto.class));
    }

    @Test
    public void testTemplateNotFoundErrorCode() throws Exception {

        when(serviceRequestAuthorizer.authorise(Mockito.any(HttpServletRequest.class)))
                .thenReturn(new Service("ccd"));

        when(userRequestAuthorizer.authorise(Mockito.any(HttpServletRequest.class)))
                .thenReturn(new User("john", Stream.of("caseworker").collect(Collectors.toSet())));

        when(formDefinitionService.getFormDefinition(Mockito.any(TemplateIdDto.class)))
                .thenThrow(new TemplateNotFoundException("xxx"));

        this.mockMvc
                .perform(get("/api/form-definitions/123")
                        .header("Authorization", "x.y.z")
                        .header("ServiceAuthorization", "xxx"))
                .andDo(print()).andExpect(status().is4xxClientError());

        verify(formDefinitionService, Mockito.times(1))
                .getFormDefinition(Mockito.any(TemplateIdDto.class));
    }

    @Test
    public void testTemplateRetrievalException() throws Exception {

        when(serviceRequestAuthorizer.authorise(Mockito.any(HttpServletRequest.class)))
                .thenReturn(new Service("ccd"));

        when(userRequestAuthorizer.authorise(Mockito.any(HttpServletRequest.class)))
                .thenReturn(new User("john", Stream.of("caseworker").collect(Collectors.toSet())));


        when(formDefinitionService.getFormDefinition(Mockito.any(TemplateIdDto.class)))
                .thenThrow(new FormDefinitionRetrievalException("xxx"));

        this.mockMvc
                .perform(get("/api/form-definitions/123")
                        .header("Authorization", "x.y.z")
                        .header("ServiceAuthorization", "xxx"))
                .andDo(print()).andExpect(status().is5xxServerError());

        verify(formDefinitionService, Mockito.times(1))
                .getFormDefinition(Mockito.any(TemplateIdDto.class));
    }
}
