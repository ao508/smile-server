package org.mskcc.smile.service;

import org.mskcc.cmo.messaging.Gateway;

public interface RequestReplyHandlingService {
    void initialize(Gateway gateway) throws Exception;
    void patientSamplesHandler(String patientId, String replyTo) throws Exception;
    void samplesByCmoLabelHandler(String cmoLabel, String replyTo) throws Exception;
    void samplesByAltIdHandler(String cmoLabel, String replyTo) throws Exception;
    void patientIdMappingHandler(String inputId, String replyTo) throws Exception;
    void shutdown() throws Exception;
}
