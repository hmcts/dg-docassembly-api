package uk.gov.hmcts.reform.dg.docassembly.rest;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.dg.docassembly.config.Constants;
import uk.gov.hmcts.reform.dg.docassembly.dto.CreateTemplateRenditionDto;
import uk.gov.hmcts.reform.dg.docassembly.service.TemplateRenditionService;

import javax.validation.Valid;
import java.io.IOException;

@ConditionalOnProperty("endpoint-toggles.template-renditions")
@RestController
@RequestMapping("/api")
public class TemplateRenditionResource {

    private final TemplateRenditionService templateRenditionService;

    public TemplateRenditionResource(TemplateRenditionService templateRenditionService) {
        this.templateRenditionService = templateRenditionService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields(Constants.IS_ADMIN);
    }

    @ApiOperation(
        value = "Renders a templates using provided values and uploads it to Document Store"
    )
    @ApiImplicitParam(name = "ServiceAuthorization", paramType = "header", required = true, dataTypeClass = String.class)
    @PostMapping("/template-renditions")
    public ResponseEntity<CreateTemplateRenditionDto> createTemplateRendition(
            @RequestBody @Valid CreateTemplateRenditionDto createTemplateRenditionDto,
            @RequestHeader("Authorization") String jwt) throws IOException {

        createTemplateRenditionDto.setJwt(jwt);

        CreateTemplateRenditionDto templateRenditionOutputDto =
                templateRenditionService.renderTemplate(createTemplateRenditionDto);

        return ResponseEntity.ok(templateRenditionOutputDto);

    }

}
