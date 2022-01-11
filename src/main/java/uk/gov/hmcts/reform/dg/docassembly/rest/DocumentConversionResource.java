package uk.gov.hmcts.reform.dg.docassembly.rest;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
public class DocumentConversionResource {

    private final Logger log = LoggerFactory.getLogger(DocumentConversionResource.class);
    private FileToPDFConverterService fileToPDFConverterService;

    @Value("${endpoint-toggles.enable-secure-document-conversion-endpoint}")
    boolean cdamEnabled;

    public DocumentConversionResource(FileToPDFConverterService fileToPDFConverterService) {
        this.fileToPDFConverterService = fileToPDFConverterService;
    }

    @ApiOperation(value = "Convert Document to PDF", notes = "A POST request to convert document type to PDF and "
        + "return the converted document. secureDocStoreEnabled attribute is disabled by default.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully redacted"),
            @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 500, message = "Server Error"),
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
