package uk.gov.hmcts.reform.dg.docassembly.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
public class CreateTemplateRenditionDto extends TemplateIdDto {

    private JsonNode formPayload;

    @NotNull
    private RenditionOutputType outputType = RenditionOutputType.PDF;

    private String renditionOutputLocation;

    private String outputFilename;

    @Getter
    @Setter
    private boolean secureDocStoreEnabled;

    @Getter
    @Setter
    private String caseTypeId;

    @Getter
    @Setter
    private String jurisdictionId;

    @Getter
    @Setter
    private String hashToken;

    @Getter
    @Setter
    private List<String> errors = new ArrayList<>();

    public JsonNode getFormPayload() {
        return formPayload;
    }

    public void setFormPayload(JsonNode formPayload) {
        this.formPayload = formPayload;
    }

    public RenditionOutputType getOutputType() {
        return outputType;
    }

    public void setOutputType(RenditionOutputType outputType) {
        this.outputType = outputType;
    }

    public String getRenditionOutputLocation() {
        return renditionOutputLocation;
    }

    public void setRenditionOutputLocation(String renditionOutputLocation) {
        this.renditionOutputLocation = renditionOutputLocation;
    }

    public void setOutputFilename(String outputFilename) {
        this.outputFilename = outputFilename;
    }

    public String getOutputFilename() {
        if (this.outputFilename == null) {
            this.outputFilename = UUID.randomUUID().toString();
        }
        return this.outputFilename;
    }

    /**
     * Returns the full filename to be used when outputting.
     * A client supplied output name will be used if provided, else a UUID
     * This is then appended with the outputTypes file extension
     * @return String
     */
    @JsonIgnore
    public String getFullOutputFilename() {

        return this.getOutputFilename() + this.outputType.getFileExtension();
    }



}
