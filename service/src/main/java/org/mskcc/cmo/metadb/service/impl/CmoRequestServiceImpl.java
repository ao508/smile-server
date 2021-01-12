package org.mskcc.cmo.metadb.service.impl;

import java.util.List;
import org.mskcc.cmo.metadb.model.CmoRequestEntity;
import org.mskcc.cmo.metadb.model.SampleManifestEntity;
import org.mskcc.cmo.metadb.persistence.CmoRequestRepository;
import org.mskcc.cmo.metadb.persistence.SampleManifestRepository;
import org.mskcc.cmo.metadb.service.CmoRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author ochoaa
 */
@Component
public class CmoRequestServiceImpl implements CmoRequestService {

    @Autowired
    private CmoRequestRepository cmoRequestRepository;
    
    @Autowired 
    private SampleManifestRepository sampleManifestRepository;


    @Override
    public void saveRequest(CmoRequestEntity request) {
        CmoRequestEntity savedRequest = getIgoRequest(request.getRequestId());
        if (savedRequest == null) {
            for (SampleManifestEntity s: request.getSampleManifestList()) {
                sampleManifestRepository.save(s);
            }
            cmoRequestRepository.save(request);
        } else {
            System.out.println(savedRequest.getSampleManifestList());
            for (SampleManifestEntity s: retrieveSampleManifestList(savedRequest.getRequestId())) {
                System.out.println(sampleManifestRepository.findIgoId(s.getUuid()));
                //if (savedRequest.findSampleManifest(s) == null) {
                //    savedRequest.addSampleManifest(s);
                //    cmoRequestRepository.save(request);
                //}
            }
        }
    }
    
    @Override
    public CmoRequestEntity getIgoRequest(String request_id) {
        return cmoRequestRepository.findByRequestId(request_id);
    }

    @Override
    public List<SampleManifestEntity> retrieveSampleManifestList(String requestId) {
        return cmoRequestRepository.findAllSampleManifests(requestId);
    }
}
