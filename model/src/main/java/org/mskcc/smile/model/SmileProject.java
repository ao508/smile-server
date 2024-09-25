package org.mskcc.smile.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Project")
public class SmileProject implements Serializable {
    @Id
    private String igoProjectId;
    private String namespace;
    @Relationship(type = "HAS_REQUEST", direction = Relationship.Direction.OUTGOING)
    private List<SmileRequest> requestList;

    public SmileProject() {}

    public SmileProject(String igoProjectId) {
        this.igoProjectId = igoProjectId;
    }

    /**
     * SmileProject constructor.
     * @param igoProjectId
     * @param namespace
     * @param requestList
     */
    public SmileProject(String igoProjectId, String namespace, List<SmileRequest> requestList) {
        this.igoProjectId = igoProjectId;
        this.namespace = namespace;
        this.requestList = requestList;
    }

    public String getIgoProjectId() {
        return igoProjectId;
    }

    public void setIgoProjectId(String igoProjectId) {
        this.igoProjectId = igoProjectId;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public List<SmileRequest> getRequestList() {
        return requestList;
    }

    public void setRequestList(List<SmileRequest> requestList) {
        this.requestList = requestList;
    }

    /**
     *
     * @param smileRequest
     */
    public void addRequest(SmileRequest smileRequest) {
        if (requestList == null) {
            requestList = new ArrayList<>();
        }
        requestList.add(smileRequest);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
