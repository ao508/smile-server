package org.mskcc.smile.service.msg;

import org.mskcc.cmo.messaging.Gateway;

public interface LabelGeneratorReqReplyService {
    void initialize(Gateway gateway) throws Exception;
    void newCmoSampleLabelGeneratorHandler(String sampleJson, String replyTo) throws Exception;
    void shutdown() throws Exception;
}
