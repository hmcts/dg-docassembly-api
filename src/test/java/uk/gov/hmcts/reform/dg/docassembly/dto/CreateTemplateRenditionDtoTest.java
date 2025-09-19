package uk.gov.hmcts.reform.dg.docassembly.dto;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateTemplateRenditionDtoTest {

    CreateTemplateRenditionDto dto;

    @BeforeEach
    void setup() {
        dto = new CreateTemplateRenditionDto();
        dto.setOutputType(RenditionOutputType.PDF);
    }

    @Test
    void filenameWithGivenOutputFilename() {
        dto.setOutputFilename("test-output-filename");
        assertEquals("test-output-filename.pdf", dto.getFullOutputFilename());
    }

    @Test
    void filenameWithNoOutputFilename() {
        String filename = dto.getFullOutputFilename();
        assertTrue(
                filename.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.pdf$"),
                "Filename should match UUID.pdf pattern"
        );
    }
}
