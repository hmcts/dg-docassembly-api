package uk.gov.hmcts.reform.dg.docassembly.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.touk.throwing.ThrowingFunction;
import uk.gov.hmcts.reform.dg.docassembly.dto.TemplateIdDto;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Service
public class FormDefinitionService {

    private static Logger log = LoggerFactory.getLogger(FormDefinitionService.class);

    private final TemplateManagementApiClient templateManagementApiClient;
    private final TemplateContentExtractor templateContentExtractor;
    private final ObjectMapper objectMapper;

    private static final String START_TAG = "<<cs_{displaycode=‘1’}>>";
    private static final String END_TAG = "<<es_>>";

    public FormDefinitionService(TemplateManagementApiClient templateManagementApiClient,
                                 TemplateContentExtractor templateContentExtractor,
                                 ObjectMapper objectMapper) {
        this.templateManagementApiClient = templateManagementApiClient;
        this.templateContentExtractor = templateContentExtractor;
        this.objectMapper = objectMapper;
    }

    public Optional<JsonNode> getFormDefinition(TemplateIdDto templateIdDto) {

        try (InputStream inputStream = templateManagementApiClient.getTemplate(templateIdDto)) {

            return templateContentExtractor
                    .extractTextBetweenTags(inputStream, START_TAG, END_TAG)
                    .map(ThrowingFunction.unchecked(objectMapper::readTree));


        } catch (IOException e) {
            FormDefinitionRetrievalException exceptionToThrow = new FormDefinitionRetrievalException(
                    String.format("Could not retrieve form definition for template %s: '%s'",
                            templateIdDto.getTemplateId(),
                            e.getMessage()), e);
            log.error(exceptionToThrow.getMessage(), exceptionToThrow);
            throw exceptionToThrow;
        }

    }

}
