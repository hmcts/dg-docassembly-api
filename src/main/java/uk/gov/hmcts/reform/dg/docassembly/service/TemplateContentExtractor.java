package uk.gov.hmcts.reform.dg.docassembly.service;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Service
public class TemplateContentExtractor {

    public Optional<String> extractTextBetweenTags(
            InputStream wordInputStream,
            String startTag,
            String endTag
    ) throws IOException {
        XWPFDocument doc = new XWPFDocument(wordInputStream);
        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
        String docText = extractor.getText();

        int startIndex = docText.indexOf(startTag);
        if (startIndex >= 0) {
            startIndex = startIndex + startTag.length();
            int endIndex = docText.indexOf(endTag, startIndex);
            return Optional.of(docText.substring(startIndex, endIndex));
        } else {
            return Optional.empty();
        }
    }

}
