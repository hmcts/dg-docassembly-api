package uk.gov.hmcts.reform.dg.docassembly.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import uk.gov.hmcts.reform.dg.docassembly.exception.DocumentTaskProcessingException;
import uk.gov.hmcts.reform.dg.docassembly.service.TemplateRenditionService;

import java.io.IOException;

@RestController
@RequestMapping("/api")
@Tag(name = "Template Rendition Service", description = "Endpoint for Template Rendition.")
public class TemplateRenditionResource {

    private final TemplateRenditionService templateRenditionService;

    private final Logger logger = LoggerFactory.getLogger(TemplateRenditionResource.class);

    @Value("${endpoint-toggles.enable-secure-document-templ-rend-endpoint}")
    private boolean cdamEnabled;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields(Constants.IS_ADMIN);
    }

    @Autowired
    public TemplateRenditionResource(TemplateRenditionService templateRenditionService) {
        this.templateRenditionService = templateRenditionService;
    }

    @Operation(
        summary = "Renders a templates using provided values and uploads it to Document Store."
            + " secureDocStoreEnabled attribute is disabled by default.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(schema = @Schema(implementation = CreateTemplateRenditionDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Access Denied")
    })
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
        if (cdamEnabled && (StringUtils.isBlank(createTemplateRenditionDto.getCaseTypeId())
                || StringUtils.isBlank(createTemplateRenditionDto.getJurisdictionId()))) {
            createTemplateRenditionDto.getErrors().add(Constants.CDAM_VALIDATION_MSG);
            return ResponseEntity
                .badRequest()
                .body(createTemplateRenditionDto);
        }
        createTemplateRenditionDto.setJwt(jwt);
        createTemplateRenditionDto.setServiceAuth(serviceAuth);
        CreateTemplateRenditionDto templateRenditionOutputDto =
                templateRenditionService.renderTemplate(createTemplateRenditionDto);

        templateRenditionOutputDto.setJwt(null);
        templateRenditionOutputDto.setServiceAuth(null);

        return ResponseEntity.ok(templateRenditionOutputDto);
    }
}
