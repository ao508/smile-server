package org.mskcc.smile.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import javax.persistence.Convert;

/**
 *
 * @author ochoaa
 */

@Node("Patient")
public class SmilePatient implements Serializable {
    @Id @GeneratedValue(UUIDStringGenerator.class)
    // @Convert(UuidStringConverter.class)
    private String smilePatientId;
    @Relationship(type = "HAS_SAMPLE", direction = Relationship.Direction.OUTGOING)
    private List<SmileSample> smileSampleList;
    @Relationship(type = "IS_ALIAS", direction = Relationship.Direction.INCOMING)
    private List<PatientAlias>  patientAliases;

    public SmilePatient() {}

    public SmilePatient(String aliasValue, String aliasNamespace) {
        this.patientAliases = new ArrayList<>();
        patientAliases.add(new PatientAlias(aliasValue, aliasNamespace));
    }

    public String getSmilePatientId() {
        return smilePatientId;
    }

    public void setSmilePatientId(String smilePatientId) {
        this.smilePatientId = smilePatientId;
    }

    /**
     * Returns CMO PatientAlias.
     * @return
     */
    public PatientAlias getCmoPatientId() {
        if (patientAliases == null) {
            this.patientAliases = new ArrayList<>();
        }
        for (PatientAlias p : patientAliases) {
            if (p.getNamespace().equalsIgnoreCase("cmoId")) {
                return p;
            }
        }
        return null;
    }

    public List<SmileSample> getSmileSampleList() {
        return smileSampleList;
    }

    public void setSmileSampleList(List<SmileSample> smileSampleList) {
        this.smileSampleList = smileSampleList;
    }

    /**
     * Add sample to array list.
     * @param smileSample
     */
    public void addSmileSample(SmileSample smileSample) {
        if (smileSampleList == null) {
            smileSampleList = new ArrayList<>();
        }
        smileSampleList.add(smileSample);
    }

    /**
     * Returns patient aliases list.
     * @return List
     */
    public List<PatientAlias> getPatientAliases() {
        if (patientAliases == null) {
            patientAliases = new ArrayList<>();
        }
        return patientAliases;
    }

    public void setPatientAliases(List<PatientAlias> patientAliases) {
        this.patientAliases = patientAliases;
    }

    /**
     * Add patient to array list.
     * @param patientAlias
     */
    public void addPatientAlias(PatientAlias patientAlias) {
        if (patientAliases == null) {
            patientAliases = new ArrayList<>();
        }
        patientAliases.add(patientAlias);
    }

    /**
     * Determines whether Patient has a patient alias matching the namespace provided.
     * @param patientAlias
     * @return Boolean
     */
    public Boolean hasPatientAlias(PatientAlias patientAlias) {
        if (patientAliases == null) {
            patientAliases = new ArrayList<>();
            return Boolean.FALSE;
        }
        for (PatientAlias alias : patientAliases) {
            if (alias.getNamespace().equalsIgnoreCase(patientAlias.getNamespace())) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    /**
     * Determines whether Patient has a patient alias matching the namespace provided.
     * @param patientAliasNamespace
     * @return Boolean
     */
    public Boolean hasPatientAlias(String patientAliasNamespace) {
        if (patientAliases == null) {
            patientAliases = new ArrayList<>();
            return Boolean.FALSE;
        }
        for (PatientAlias alias : patientAliases) {
            if (alias.getNamespace().equalsIgnoreCase(patientAliasNamespace)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
