package uk.gov.hmcts.reform.dg.docassembly.dto;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Jackson 3 ownership proof for the MVC template-rendition DTO.
 * Guards silent field drop / ignoreUnknown surprises after Boot 4.
 */
class CreateTemplateRenditionDtoJackson3Test {

    private static final String REQUEST_JSON = """
            {
              "templateId": "dGVtcGxhdGU=",
              "formPayload": {"a": 1, "nested": {"b": "x"}},
              "outputType": "DOCX",
              "outputFilename": "client-name",
              "secureDocStoreEnabled": true,
              "caseTypeId": "CASE_TYPE",
              "jurisdictionId": "PUBLICLAW",
              "fullOutputFilename": "should-be-ignored"
            }
            """;

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void jackson3RoundTripsFormPayloadAndIgnoresFullOutputFilenameProperty() {
        CreateTemplateRenditionDto dto = mapper.readValue(REQUEST_JSON, CreateTemplateRenditionDto.class);

        assertEquals("dGVtcGxhdGU=", dto.getTemplateId());
        assertEquals(RenditionOutputType.DOCX, dto.getOutputType());
        assertEquals("client-name", dto.getOutputFilename());
        assertTrue(dto.isSecureDocStoreEnabled());
        assertEquals("CASE_TYPE", dto.getCaseTypeId());
        assertEquals("PUBLICLAW", dto.getJurisdictionId());
        assertEquals(1, dto.getFormPayload().get("a").asInt());
        assertEquals("x", dto.getFormPayload().get("nested").get("b").asString());
        assertEquals("client-name.docx", dto.getFullOutputFilename());

        String written = mapper.writeValueAsString(dto);
        JsonNode tree = mapper.readTree(written);

        assertFalse(tree.has("fullOutputFilename"),
            "@JsonIgnore getFullOutputFilename must not appear in Jackson 3 output");
        assertEquals(1, tree.get("formPayload").get("a").asInt());
        assertEquals("x", tree.get("formPayload").get("nested").get("b").asString());
        assertEquals("DOCX", tree.get("outputType").asString());
        assertEquals("client-name", tree.get("outputFilename").asString());
    }

    @Test
    void formPayloadToStringRemainsValidJsonForDocmosis() {
        CreateTemplateRenditionDto dto = mapper.readValue(REQUEST_JSON, CreateTemplateRenditionDto.class);

        String docmosisData = String.valueOf(dto.getFormPayload());
        JsonNode reparsed = mapper.readTree(docmosisData);

        assertEquals(1, reparsed.get("a").asInt());
        assertEquals("x", reparsed.get("nested").get("b").asString());
    }
}
