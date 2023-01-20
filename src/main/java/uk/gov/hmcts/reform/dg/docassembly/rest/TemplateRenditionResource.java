package uk.gov.hmcts.reform.dg.docassembly.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.dg.docassembly.config.Constants;
import uk.gov.hmcts.reform.dg.docassembly.dto.CreateTemplateRenditionDto;
import uk.gov.hmcts.reform.dg.docassembly.service.TemplateRenditionService;
import uk.gov.hmcts.reform.dg.docassembly.service.exception.DocumentTaskProcessingException;

import java.io.IOException;
import javax.validation.Valid;

@ConditionalOnProperty("endpoint-toggles.template-renditions")
@RestController
@RequestMapping("/api")
@Tag(name = "Template Rendition Service", description = "Endpoint for Template Rendition.")
public class TemplateRenditionResource {

    @Autowired
    private TemplateRenditionService templateRenditionService;

    private final Logger logger = LoggerFactory.getLogger(TemplateRenditionResource.class);

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields(Constants.IS_ADMIN);
    }

    @Operation(
        summary = "Renders a templates using provided values and uploads it to Document Store."
            + " secureDocStoreEnabled attribute is disabled by default.")
    @PostMapping("/template-renditions")
    public ResponseEntity<CreateTemplateRenditionDto> createTemplateRendition(
            @RequestBody @Valid CreateTemplateRenditionDto createTemplateRenditionDto,
            @RequestHeader("Authorization") String jwt, @RequestHeader("ServiceAuthorization") String serviceAuth)
        throws IOException, DocumentTaskProcessingException {
        logger.info(
                "template-renditions request document name : {}  "
                        + "with JurisdictionId : {} and caseTypeId :{}, isSecureDocStoreEnabled {}",
                createTemplateRenditionDto.getFullOutputFilename(),
                createTemplateRenditionDto.getJurisdictionId(),
                createTemplateRenditionDto.getCaseTypeId(),
                createTemplateRenditionDto.isSecureDocStoreEnabled()
        );
        createTemplateRenditionDto.setJwt(jwt);
        createTemplateRenditionDto.setServiceAuth(serviceAuth);
        CreateTemplateRenditionDto templateRenditionOutputDto =
                templateRenditionService.renderTemplate(createTemplateRenditionDto);

        templateRenditionOutputDto.setJwt(null);
        templateRenditionOutputDto.setServiceAuth(null);

        return ResponseEntity.ok(templateRenditionOutputDto);

    }

}
