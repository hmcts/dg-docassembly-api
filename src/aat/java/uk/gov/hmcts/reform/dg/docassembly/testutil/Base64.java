package uk.gov.hmcts.reform.dg.docassembly.testutil;

public class Base64 {

    private Base64() {
    }

    public static String base64(String file) {
        return java.util.Base64.getEncoder().encodeToString(file.getBytes());
    }

}
