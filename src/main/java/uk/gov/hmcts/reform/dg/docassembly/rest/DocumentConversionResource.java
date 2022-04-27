package uk.gov.hmcts.reform.dg.docassembly.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.dg.docassembly.service.FileToPDFConverterService;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.UUID;


/**
 * REST controller for converting File types to PDF using Docmosis.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Document Conversion Service", description = "Endpoint for Document Conversion.")
public class DocumentConversionResource {

    private final Logger log = LoggerFactory.getLogger(DocumentConversionResource.class);
    private FileToPDFConverterService fileToPDFConverterService;

    @Value("${endpoint-toggles.enable-secure-document-conversion-endpoint}")
    boolean cdamEnabled;

    public DocumentConversionResource(FileToPDFConverterService fileToPDFConverterService) {
        this.fileToPDFConverterService = fileToPDFConverterService;
    }

    @Operation(summary = "Convert Document to PDF", description = "A POST request to convert document type to PDF and "
        + "return the converted document. secureDocStoreEnabled attribute is disabled by default.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = "authorization",
                            description = "Authorization (Idam Bearer token)", required = true,
                            schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.HEADER, name = "serviceauthorization",
                            description = "Service Authorization (S2S Bearer token)", required = true,
                            schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.PATH, name = "documentId",
                            description = "Document Id", required = true,
                            schema = @Schema(type = "string"))})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully redacted"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorised"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Server Error"),
    })
    @PostMapping("/convert/{documentId}")
    public ResponseEntity<Object> convert(HttpServletRequest request, @PathVariable UUID documentId) {
        try {
            File convertedFile;
            log.info("cdamEnabled is : {} for documentId : {} ", cdamEnabled, documentId);
            if (cdamEnabled) {
                String auth = request.getHeader("Authorization");
                String serviceAuth = request.getHeader("ServiceAuthorization");
                log.debug("REST request to get secure Document Conversion To PDF : {}", documentId);
                convertedFile = fileToPDFConverterService.convertFile(documentId, auth, serviceAuth);
            } else {
                convertedFile = fileToPDFConverterService.convertFile(documentId);
            }

            Tika tika = new Tika();

            return ResponseEntity.ok()
                    .contentLength(convertedFile.length())
                    .contentType(MediaType.parseMediaType(tika.detect(convertedFile)))
                    .body(new FileSystemResource(convertedFile));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}
