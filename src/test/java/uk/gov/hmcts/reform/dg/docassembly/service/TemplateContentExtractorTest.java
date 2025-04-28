package uk.gov.hmcts.reform.dg.docassembly.service;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplateContentExtractorTest {

    TemplateContentExtractor templateContentExtractor = new TemplateContentExtractor();

    @Test
    void testExtraction() throws Exception {
        Optional<String> extractTextBetweenTags = templateContentExtractor.extractTextBetweenTags(
                getClass().getClassLoader().getResourceAsStream("template1.docx"),
                "<<cs_{displaycode=‘1’}>>", "<<es_>>");

        assertNotNull(extractTextBetweenTags);
        assertEquals("[]", extractTextBetweenTags.get().trim());
    }

    @Test
    void testExtractionEmpty() throws Exception {
        Optional<String> extractTextBetweenTags = templateContentExtractor.extractTextBetweenTags(
            getClass().getClassLoader().getResourceAsStream("template1.docx"),
            "<<cs_{displaycode=‘0’}>>", "<<es_>>");

        assertTrue(extractTextBetweenTags.isEmpty());
    }

}
