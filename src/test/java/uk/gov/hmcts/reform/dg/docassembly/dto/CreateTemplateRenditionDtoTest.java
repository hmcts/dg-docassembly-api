package uk.gov.hmcts.reform.dg.docassembly.dto;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CreateTemplateRenditionDtoTest {

    CreateTemplateRenditionDto dto;

    @Before
    public void setup() {
        dto = new CreateTemplateRenditionDto();
        dto.setOutputType(RenditionOutputType.PDF);
    }

    @Test
    public void filenameWithGivenOutputFilename() {
        dto.setOutputFilename("test-output-filename");

        Assert.assertEquals(dto.getFullOutputFilename(), "test-output-filename.pdf");
    }

    @Test
    public void filenameWithNoOutputFilename() {
        String filename = dto.getFullOutputFilename();
        Assert.assertTrue("Matches UUID.pdf pattern",
                filename.matches(
                        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.pdf$"
                )
        );
    }
}
