package org.mskcc.smile.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Message;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mskcc.cmo.messaging.Gateway;
import org.mskcc.cmo.messaging.MessageConsumer;
import org.mskcc.smile.model.SampleMetadata;
import org.mskcc.smile.model.SmileSample;
import org.mskcc.smile.model.internal.PatientIdTriplet;
import org.mskcc.smile.service.PatientIdMappingService;
import org.mskcc.smile.service.RequestReplyHandlingService;
import org.mskcc.smile.service.SmileSampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RequestReplyHandlingServiceImpl implements RequestReplyHandlingService {

    @Value("${request_reply.patient_samples_topic}")
    private String PATIENT_SAMPLES_REQREPLY_TOPIC;

    @Value("${request_reply.samples_by_cmo_label_topic}")
    private String SAMPLES_BY_CMO_LABEL_REQREPLY_TOPIC;

    @Value("${request_reply.samples_by_alt_id_topic}")
    private String SAMPLES_BY_ALT_ID_REQREPLY_TOPIC;

    @Value("${request_reply.patient_mapping_topic}")
    private String PATIENT_MAPPING_REQREPLY_TOPIC;

    @Value("${num.new_request_handler_threads}")
    private int NUM_NEW_REQUEST_HANDLERS;

    @Autowired
    private SmileSampleService sampleService;

    @Autowired
    private PatientIdMappingService patientIdMappingService;

    private final ObjectMapper mapper = new ObjectMapper();
    private static boolean initialized = false;
    private static volatile boolean shutdownInitiated;
    private static final ExecutorService exec = Executors.newCachedThreadPool();
    private static final BlockingQueue<ReplyInfo> patientSamplesReqReplyQueue =
        new LinkedBlockingQueue<ReplyInfo>();
    private static final BlockingQueue<ReplyInfo> samplesByCmoLabelReqReplyQueue =
        new LinkedBlockingQueue<ReplyInfo>();
    private static final BlockingQueue<ReplyInfo> samplesByAltIdReqReplyQueue =
        new LinkedBlockingQueue<ReplyInfo>();
    private static final BlockingQueue<ReplyInfo> patientIdMappingReqReplyQueue =
        new LinkedBlockingQueue<ReplyInfo>();
    private static CountDownLatch patientSamplesHandlerShutdownLatch;
    private static CountDownLatch samplesByCmoLabelHandlerShutdownLatch;
    private static CountDownLatch samplesByAltIdHandlerShutdownLatch;
    private static CountDownLatch patientIdMappingHandlerShutdownLatch;
    private static Gateway messagingGateway;

    private static final Log LOG = LogFactory.getLog(RequestReplyHandlingServiceImpl.class);

    private class ReplyInfo {
        String requestMessage;
        String replyTo;

        ReplyInfo(String requestMessage, String replyTo) {
            this.requestMessage = requestMessage;
            this.replyTo = replyTo;
        }

        String getRequestMessage() {
            return requestMessage;
        }

        String getReplyTo() {
            return replyTo;
        }
    }

    private class PatientSamplesReqReplyHandler implements Runnable {

        final Phaser phaser;
        boolean interrupted = false;

        PatientSamplesReqReplyHandler(Phaser phaser) {
            this.phaser = phaser;
        }

        @Override
        public void run() {
            phaser.arrive();
            while (true) {
                try {
                    // reply info request message contains cmo patient id
                    ReplyInfo replyInfo = patientSamplesReqReplyQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (replyInfo != null) {
                        List<SmileSample> researchSamples =
                                sampleService.getSamplesByCategoryAndCmoPatientId(
                                        replyInfo.getRequestMessage(), "research");
                        List<SampleMetadata> sampleMetadataList = new ArrayList<>();
                        for (SmileSample sample : researchSamples) {
                            sampleMetadataList.add(sample.getLatestSampleMetadata());
                        }
                        messagingGateway.replyPublish(replyInfo.getReplyTo(),
                                mapper.writeValueAsString(sampleMetadataList));
                    }
                    if (interrupted && patientSamplesReqReplyQueue.isEmpty()) {
                        break;
                    }
                } catch (InterruptedException e) {
                    interrupted = true;
                } catch (Exception e) {
                    LOG.error("Error during request handling", e);
                }
            }
            patientSamplesHandlerShutdownLatch.countDown();
        }
    }

    private class SamplesByCmoLabelReqReplyHandler implements Runnable {

        final Phaser phaser;
        boolean interrupted = false;

        SamplesByCmoLabelReqReplyHandler(Phaser phaser) {
            this.phaser = phaser;
        }

        @Override
        public void run() {
            phaser.arrive();
            while (true) {
                try {
                    // reply info request message contains cmo sample label
                    ReplyInfo replyInfo = samplesByCmoLabelReqReplyQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (replyInfo != null) {
                        List<SmileSample> matchingSamples =
                                sampleService.getSamplesByCmoSampleName(replyInfo.getRequestMessage());
                        List<SampleMetadata> sampleMetadataList = new ArrayList<>();
                        for (SmileSample sample : matchingSamples) {
                            sampleMetadataList.add(sample.getLatestSampleMetadata());
                        }
                        messagingGateway.replyPublish(replyInfo.getReplyTo(),
                                mapper.writeValueAsString(sampleMetadataList));
                    }
                    if (interrupted && samplesByCmoLabelReqReplyQueue.isEmpty()) {
                        break;
                    }
                } catch (InterruptedException e) {
                    interrupted = true;
                } catch (Exception e) {
                    LOG.error("Error during request handling", e);
                }
            }
            samplesByCmoLabelHandlerShutdownLatch.countDown();
        }
    }

    private class SamplesByAltIdReqReplyHandler implements Runnable {

        final Phaser phaser;
        boolean interrupted = false;

        SamplesByAltIdReqReplyHandler(Phaser phaser) {
            this.phaser = phaser;
        }

        @Override
        public void run() {
            phaser.arrive();
            while (true) {
                try {
                    // reply info request message contains cmo sample label
                    ReplyInfo replyInfo = samplesByAltIdReqReplyQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (replyInfo != null) {
                        List<SmileSample> matchingSamples =
                                sampleService.getSamplesByAltId(replyInfo.getRequestMessage());
                        List<SampleMetadata> sampleMetadataList = new ArrayList<>();
                        for (SmileSample sample : matchingSamples) {
                            sampleMetadataList.add(sample.getLatestSampleMetadata());
                        }
                        messagingGateway.replyPublish(replyInfo.getReplyTo(),
                                mapper.writeValueAsString(sampleMetadataList));
                    }
                    if (interrupted && samplesByAltIdReqReplyQueue.isEmpty()) {
                        break;
                    }
                } catch (InterruptedException e) {
                    interrupted = true;
                } catch (Exception e) {
                    LOG.error("Error during request handling", e);
                }
            }
            samplesByAltIdHandlerShutdownLatch.countDown();
        }
    }

    private class PatientIdMappingReqReplyHandler implements Runnable {

        final Phaser phaser;
        boolean interrupted = false;

        PatientIdMappingReqReplyHandler(Phaser phaser) {
            this.phaser = phaser;
        }

        @Override
        public void run() {
            phaser.arrive();
            while (true) {
                try {
                    ReplyInfo replyInfo = patientIdMappingReqReplyQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (replyInfo != null) {
                        try {
                            PatientIdTriplet patientIdTriplet =
                                    patientIdMappingService.getPatientIdTripletByInputId(
                                            replyInfo.getRequestMessage());
                            messagingGateway.replyPublish(replyInfo.getReplyTo(),
                                    mapper.writeValueAsString(patientIdTriplet));
                        } catch (NullPointerException e) {
                            LOG.error("Patient ID Mapping (databricks) service returned null for input id: "
                                    + replyInfo.getRequestMessage());
                        }
                    }
                    if (interrupted && patientIdMappingReqReplyQueue.isEmpty()) {
                        break;
                    }
                } catch (InterruptedException e) {
                    interrupted = true;
                } catch (Exception e) {
                    LOG.error("Error during request handling", e);
                }
            }
            patientIdMappingHandlerShutdownLatch.countDown();
        }
    }

    @Override
    public void initialize(Gateway gateway) throws Exception {
        if (!initialized) {
            messagingGateway = gateway;
            setupPatientSamplesHandler(messagingGateway, this);
            setupSamplesByCmoLabelHandler(messagingGateway, this);
            setupSamplesByAltIdHandler(messagingGateway, this);
            setupPatientIdMappingHandler(messagingGateway, this);
            initializeRequestReplyHandlers();
            initialized = true;
        } else {
            LOG.error("Messaging Handler Service has already been initialized, ignoring request.");
        }
    }

    @Override
    public void patientSamplesHandler(String patientId, String replyTo) throws Exception {
        if (!initialized) {
            throw new IllegalStateException("Message Handling Service has not been initialized");
        }
        if (!shutdownInitiated) {
            patientSamplesReqReplyQueue.put(new ReplyInfo(patientId, replyTo));
        } else {
            LOG.error("Shutdown initiated, not accepting PatientIds: " + patientId);
            throw new IllegalStateException("Shutdown initiated, not handling any more patientIds");
        }
    }

    @Override
    public void samplesByCmoLabelHandler(String cmoLabel, String replyTo) throws Exception {
        if (!initialized) {
            throw new IllegalStateException("Message Handling Service has not been initialized");
        }
        if (!shutdownInitiated) {
            samplesByCmoLabelReqReplyQueue.put(new ReplyInfo(cmoLabel, replyTo));
        } else {
            LOG.error("Shutdown initiated, not accepting CMO label req-reply: " + cmoLabel);
            throw new IllegalStateException("Shutdown initiated, not handling any more CMO labels");
        }
    }

    @Override
    public void samplesByAltIdHandler(String altId, String replyTo) throws Exception {
        if (!initialized) {
            throw new IllegalStateException("Message Handling Service has not been initialized");
        }
        if (!shutdownInitiated) {
            samplesByAltIdReqReplyQueue.put(new ReplyInfo(altId, replyTo));
        } else {
            LOG.error("Shutdown initiated, not accepting alt ID req-reply: " + altId);
            throw new IllegalStateException("Shutdown initiated, not handling any more alt IDs");
        }
    }

    @Override
    public void patientIdMappingHandler(String inputId, String replyTo) throws Exception {
        if (!initialized) {
            throw new IllegalStateException("Message Handling Service has not been initialized");
        }
        if (!shutdownInitiated) {
            patientIdMappingReqReplyQueue.put(new ReplyInfo(inputId, replyTo));
        } else {
            LOG.error("Shutdown initiated, not accepting Patient ID Mapping Service request");
            throw new IllegalStateException("Shutdown initiated, not handling any more patientIds");
        }
    }

    @Override
    public void shutdown() throws Exception {
        if (!initialized) {
            throw new IllegalStateException("Message Handling Service has not been initialized");
        }
        exec.shutdownNow();
        patientSamplesHandlerShutdownLatch.await();
        samplesByCmoLabelHandlerShutdownLatch.await();
        patientIdMappingHandlerShutdownLatch.await();
        shutdownInitiated = true;
    }

    private void setupPatientSamplesHandler(Gateway gateway,
            RequestReplyHandlingServiceImpl requestReplyHandlingServiceImpl)
            throws Exception {
        gateway.replySub(PATIENT_SAMPLES_REQREPLY_TOPIC, new MessageConsumer() {
            @Override
            public void onMessage(Message msg, Object message) {
                LOG.info("Received message on topic: " + PATIENT_SAMPLES_REQREPLY_TOPIC);
                try {
                    if (StringUtils.isBlank(new String(msg.getData()))) {
                        LOG.error("Expected a patient ID but message received is empty: " + msg
                                + " - message will not be added to request-reply queue");
                    } else {
                        requestReplyHandlingServiceImpl.patientSamplesHandler(
                                new String(msg.getData()), msg.getReplyTo());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setupSamplesByCmoLabelHandler(Gateway gateway,
            RequestReplyHandlingServiceImpl requestReplyHandlingServiceImpl)
            throws Exception {
        gateway.replySub(SAMPLES_BY_CMO_LABEL_REQREPLY_TOPIC, new MessageConsumer() {
            @Override
            public void onMessage(Message msg, Object message) {
                LOG.info("Received message on topic: " + SAMPLES_BY_CMO_LABEL_REQREPLY_TOPIC);
                try {
                    if (StringUtils.isBlank(new String(msg.getData()))) {
                        LOG.error("Expected a CMO label but message received is empty: " + msg
                                + " - message will not be added to request-reply queue");
                    } else {
                        requestReplyHandlingServiceImpl.samplesByCmoLabelHandler(
                                new String(msg.getData()), msg.getReplyTo());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setupSamplesByAltIdHandler(Gateway gateway,
            RequestReplyHandlingServiceImpl requestReplyHandlingServiceImpl)
            throws Exception {
        gateway.replySub(SAMPLES_BY_ALT_ID_REQREPLY_TOPIC, new MessageConsumer() {
            @Override
            public void onMessage(Message msg, Object message) {
                LOG.info("Received message on topic: " + SAMPLES_BY_ALT_ID_REQREPLY_TOPIC);
                try {
                    if (StringUtils.isBlank(new String(msg.getData()))) {
                        LOG.error("Expected an alt ID but message received is empty: " + msg
                                + " - message will not be added to request-reply queue");
                    } else {
                        requestReplyHandlingServiceImpl.samplesByAltIdHandler(
                                new String(msg.getData()), msg.getReplyTo());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setupPatientIdMappingHandler(Gateway gateway,
            RequestReplyHandlingServiceImpl requestReplyHandlingServiceImpl)
            throws Exception {
        gateway.replySub(PATIENT_MAPPING_REQREPLY_TOPIC, new MessageConsumer() {
            @Override
            public void onMessage(Message msg, Object message) {
                LOG.info("Received message on topic: " + PATIENT_MAPPING_REQREPLY_TOPIC);
                try {
                    requestReplyHandlingServiceImpl.patientIdMappingHandler(
                            new String(msg.getData()), msg.getReplyTo());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initializeRequestReplyHandlers() throws Exception {
        patientSamplesHandlerShutdownLatch = new CountDownLatch(NUM_NEW_REQUEST_HANDLERS);
        final Phaser patientSamplesPhaser = new Phaser();
        patientSamplesPhaser.register();
        for (int lc = 0; lc < NUM_NEW_REQUEST_HANDLERS; lc++) {
            patientSamplesPhaser.register();
            exec.execute(new PatientSamplesReqReplyHandler(patientSamplesPhaser));
        }
        patientSamplesPhaser.arriveAndAwaitAdvance();

        samplesByCmoLabelHandlerShutdownLatch = new CountDownLatch(NUM_NEW_REQUEST_HANDLERS);
        final Phaser samplesByCmoLabelPhaser = new Phaser();
        samplesByCmoLabelPhaser.register();
        for (int lc = 0; lc < NUM_NEW_REQUEST_HANDLERS; lc++) {
            samplesByCmoLabelPhaser.register();
            exec.execute(new SamplesByCmoLabelReqReplyHandler(samplesByCmoLabelPhaser));
        }
        samplesByCmoLabelPhaser.arriveAndAwaitAdvance();

        samplesByAltIdHandlerShutdownLatch = new CountDownLatch(NUM_NEW_REQUEST_HANDLERS);
        final Phaser samplesByAltIdPhaser = new Phaser();
        samplesByAltIdPhaser.register();
        for (int lc = 0; lc < NUM_NEW_REQUEST_HANDLERS; lc++) {
            samplesByAltIdPhaser.register();
            exec.execute(new SamplesByAltIdReqReplyHandler(samplesByAltIdPhaser));
        }
        samplesByAltIdPhaser.arriveAndAwaitAdvance();

        patientIdMappingHandlerShutdownLatch = new CountDownLatch(NUM_NEW_REQUEST_HANDLERS);
        final Phaser patientIdMappingPhaser = new Phaser();
        patientIdMappingPhaser.register();
        for (int lc = 0; lc < NUM_NEW_REQUEST_HANDLERS; lc++) {
            patientIdMappingPhaser.register();
            exec.execute(new PatientIdMappingReqReplyHandler(patientIdMappingPhaser));
        }
        patientIdMappingPhaser.arriveAndAwaitAdvance();
    }
}
