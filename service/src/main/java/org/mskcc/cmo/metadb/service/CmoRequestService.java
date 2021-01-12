package org.mskcc.cmo.metadb.service;

import java.util.List;
import org.mskcc.cmo.metadb.model.CmoRequestEntity;
import org.mskcc.cmo.metadb.model.SampleManifestEntity;

/**
 *
 * @author ochoaa
 */
public interface CmoRequestService {
    void saveRequest(CmoRequestEntity request);
    CmoRequestEntity getIgoRequest(String request_id);
    List<SampleManifestEntity> retrieveSampleManifestList(String requestId);
}
