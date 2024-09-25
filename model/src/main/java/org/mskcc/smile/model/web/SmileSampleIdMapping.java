package org.mskcc.smile.model.web;

import java.util.UUID;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.mskcc.smile.model.SampleMetadata;

/**
 *
 * @author ochoaa
 */
public class SmileSampleIdMapping {
    // @Convert(UuidStringConverter.class)
    private String smileSampleId;
    private String importDate;
    private String primaryId;
    private String cmoSampleName;

    /**
     * SmileSampleIdMapping constructor.
     */
    public SmileSampleIdMapping() {}

    /**
     * SmileSampleIdMapping constructor.
     * @param smileSampleId
     * @param sampleMetadata
     */
    public SmileSampleIdMapping(String smileSampleId, SampleMetadata sampleMetadata) {
        this.smileSampleId = smileSampleId;
        this.importDate = sampleMetadata.getImportDate();
        this.primaryId = sampleMetadata.getPrimaryId();
        this.cmoSampleName = sampleMetadata.getCmoSampleName();
    }

    public String getSmileSampleId() {
        return smileSampleId;
    }

    public void setSmileSampleId(String smileSampleId) {
        this.smileSampleId = smileSampleId;
    }

    public String getImportDate() {
        return importDate;
    }

    public void setImportDate(String importDate) {
        this.importDate = importDate;
    }

    public String getPrimaryId() {
        return primaryId;
    }

    public void setPrimaryId(String primaryId) {
        this.primaryId = primaryId;
    }

    public String getCmoSampleName() {
        return cmoSampleName;
    }

    public void setCmoSampleName(String cmoSampleName) {
        this.cmoSampleName = cmoSampleName;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
