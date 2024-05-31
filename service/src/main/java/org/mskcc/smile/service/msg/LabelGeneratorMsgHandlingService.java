package org.mskcc.smile.service.msg;

import org.mskcc.cmo.messaging.Gateway;
import org.mskcc.smile.model.SampleMetadata;

public interface LabelGeneratorMsgHandlingService {
    void initialize(Gateway gateway) throws Exception;
    void cmoLabelGeneratorHandler(String requestJson) throws Exception;
    void cmoPromotedLabelHandler(String requestJson) throws Exception;
    void cmoSampleLabelUpdateHandler(SampleMetadata sampleMetadata) throws Exception;
    void shutdown() throws Exception;
}
