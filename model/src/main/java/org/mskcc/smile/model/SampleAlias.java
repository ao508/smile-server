package org.mskcc.smile.model;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * Node entity representing the linked sample entity from an external system.
 * @author ochoaa
 */
@NodeEntity
public class SampleAlias implements Serializable {
    @Id @GeneratedValue
    private Long id;
    private String value;
    private String namespace;

    public SampleAlias() {}

    /**
     * Sample constructor.
     * @param value
     * @param namespace
     */
    public SampleAlias(String value, String namespace) {
        this.value = value;
        this.namespace = namespace;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
