package org.mskcc.smile.service;

//import org.assertj.core.api.Assertions;

import java.util.Map;
import junit.framework.Assert;
//import org.junit.Before;
//import org.junit.Test;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mskcc.smile.model.SmileRequest;
import org.mskcc.smile.persistence.neo4j.CohortCompleteRepository;
import org.mskcc.smile.persistence.neo4j.SmilePatientRepository;
import org.mskcc.smile.persistence.neo4j.SmileRequestRepository;
import org.mskcc.smile.persistence.neo4j.SmileSampleRepository;
import org.mskcc.smile.persistence.neo4j.TempoRepository;
import org.mskcc.smile.service.util.RequestDataFactory;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

//import org.junit.jupiter.api.Test;
//import org.mskcc.smile.model.PatientAlias;
//import org.mskcc.smile.model.SmilePatient;
//import org.mskcc.smile.model.SmileRequest;
//import org.mskcc.smile.persistence.neo4j.SmilePatientRepository;
//import org.mskcc.smile.persistence.neo4j.SmileRequestRepository;
//import org.mskcc.smile.persistence.neo4j.SmileSampleRepository;
//import org.mskcc.smile.service.util.RequestDataFactory;
//import org.neo4j.driver.Driver;
//import org.neo4j.ogm.session.SessionFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Import;
//import org.springframework.dao.IncorrectResultSizeDataAccessException;
//import org.springframework.transaction.ReactiveTransactionManager;
//import org.testcontainers.containers.Neo4jContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//import org.springframework.data.neo4j.core.ReactiveDatabaseSelectionProvider;
//import org.springframework.data.neo4j.core.transaction.ReactiveNeo4jTransactionManager;
//import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
//import org.springframework.data.neo4j.repository.config.ReactiveNeo4jRepositoryConfigurationExtension;
//import org.springframework.transaction.ReactiveTransactionManager;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//import org.testcontainers.utility.DockerImageName;
/**
 *
 * @author ochoaa
 */
@SpringBootTest(
classes = SmileTestApp.class)
@Testcontainers
//@DataNeo4jTest
@Import(MockDataUtils.class)
@TestPropertySource(properties = {"spring.neo4j.authentication.username:neo4j", "nats.keystore_path:", "nats.truststore_path:", "nats.key_password:", "nats.store_password:"})
//@Transactional(propagation = Propagation.NEVER)
public class PatientServiceTest {
//    @Autowired
    private MockDataUtils mockDataUtils = new MockDataUtils();
    public Map<String, MockJsonTestData> mockedRequestJsonDataMap = mockDataUtils.mockedRequestJsonDataMap();

    @Autowired
    private SmileRequestService requestService;
    
//
//    @Autowired
//    private SmileSampleService sampleService;
//
    @Autowired
    private SmilePatientService patientService;
//
    @Container
    private static final Neo4jContainer<?> databaseServer = new Neo4jContainer<>
    (DockerImageName.parse("neo4j:5.19.0"))
            .withEnv("NEO4J_dbms_security_procedures_unrestricted", "apoc.*,algo.*");

    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        databaseServer.start();
//        registry.add("spring.neo4j.authentication.username", databaseServer.);
        
        registry.add("spring.neo4j.authentication.password", databaseServer::getAdminPassword);
        registry.add("spring.neo4j.uri", databaseServer::getBoltUrl);
    }
    
    @TestConfiguration
    static class Config {
        @Bean
        public org.neo4j.ogm.config.Configuration configuration() {
            return new org.neo4j.ogm.config.Configuration.Builder()
                    .uri(databaseServer.getBoltUrl())
                    .credentials("neo4j", databaseServer.getAdminPassword())
                    .build();
        }

        @Bean
        public SessionFactory sessionFactory() {
            // with domain entity base package(s)
            return new SessionFactory(configuration(), "org.mskcc.smile.persistence");
        }
    }
    
//    @Before
//    public void setUp() {
//        databaseServer.start();
//    }
    
//    // see: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.4.0-M2-Release-Notes#neo4j-1
////    @Bean(ReactiveNeo4jRepositoryConfigurationExtension.DEFAULT_TRANSACTION_MANAGER_BEAN_NAME)
////    public ReactiveTransactionManager reactiveTransactionManager(
////            Driver driver,
////            ReactiveDatabaseSelectionProvider databaseNameProvider) {
////        return new ReactiveNeo4jTransactionManager(driver, databaseNameProvider);
////    }
//    }
//
    private final SmileRequestRepository requestRepository;
    private final SmileSampleRepository sampleRepository;
    private final SmilePatientRepository patientRepository;
    private final TempoRepository tempoRepository;
    private final CohortCompleteRepository cohortCompleteRepository;

    
    /**
     * Initializes the Neo4j repositories.
     * @param requestRepository
     * @param sampleRepository
     * @param patientRepository
     */
    @Autowired
    public PatientServiceTest(SmileRequestRepository requestRepository,
            SmileSampleRepository sampleRepository, SmilePatientRepository patientRepository, 
            TempoRepository tempoRepository, CohortCompleteRepository cohortCompleteRepository) {
        this.requestRepository = requestRepository;
        this.sampleRepository = sampleRepository;
        this.patientRepository = patientRepository;
        this.tempoRepository = tempoRepository;
        this.cohortCompleteRepository = cohortCompleteRepository;
    }

   
    
    /**
     * Persists the Mock Request data to the test database.
     * @throws Exception
     */
    @Autowired
    public void initializeMockDatabase() throws Exception {
        System.out.println("\n\n\n CHECKING REQUEST REPOSITORY");
        System.out.println(requestRepository);
        
        
        // mock request id: MOCKREQUEST1_B
        MockJsonTestData request1Data = mockedRequestJsonDataMap
                .get("mockIncomingRequest1JsonDataWith2T2N");
        SmileRequest request1 =
    RequestDataFactory.buildNewLimsRequestFromJson(request1Data.getJsonString());
        requestService.saveRequest(request1);

        // mock request id: 33344_Z
        MockJsonTestData request3Data = mockedRequestJsonDataMap
                .get("mockIncomingRequest3JsonDataPooledNormals");
        SmileRequest request3 =
    RequestDataFactory.buildNewLimsRequestFromJson(request3Data.getJsonString());
        requestService.saveRequest(request3);

        // mock request id: 145145_IM
        MockJsonTestData request5Data = mockedRequestJsonDataMap
                .get("mockIncomingRequest5JsonPtMultiSamples");
        SmileRequest request5 =
    RequestDataFactory.buildNewLimsRequestFromJson(request5Data.getJsonString());
        requestService.saveRequest(request5);
    }

    /**
     * Tests if patient service retrieves SmilePatient by cmoPatientId.
     * @throws Exception
     */
    @Test
    public void testFindPatientByPatientAlias() throws Exception {
        String cmoPatientId = "C-1MP6YY";
        Assert.assertNotNull(patientService.getPatientByCmoPatientId(cmoPatientId));
    }
//
//    /**
//     * Tests if patientRepo throws an exception when duplicates
//     * are attempted to be saved.
//     */
//    @Test
//    public void testFindPatientByPatientAliasWithExpectedFailure() {
//        String cmoPatientId = "C-1MP6YY";
//        SmilePatient patient = new SmilePatient();
//        patient.addPatientAlias(new PatientAlias(cmoPatientId, "cmoId"));
//        // this should create a duplicate patient node that will throw the exception
//        // below when queried
//        patientRepository.save(patient);
//
//        Assertions.assertThatExceptionOfType(IncorrectResultSizeDataAccessException.class)
//            .isThrownBy(() -> {
//                patientService.getPatientByCmoPatientId(cmoPatientId);
//            });
//    }
//
//    /**
//     * Tests if Patient Alias node is properly updated to the new cmoPatientId.
//     * @throws Exception
//     */
//    @Test
//    public void testUpdateCmoPatientId() throws Exception {
//        String oldCmoPatientId = "C-1MP6YY";
//        String newCmoPatientId = "NewCmoPatientId";
//
//        int numOfSampleBeforeUpdate = sampleService.getSamplesByCmoPatientId(
//                oldCmoPatientId).size();
//        patientService.updateCmoPatientId(oldCmoPatientId, newCmoPatientId);
//        int numOfSampleAfterUpdate = sampleService.getSamplesByCmoPatientId(
//                newCmoPatientId).size();
//
//        Assertions.assertThat(numOfSampleBeforeUpdate)
//            .isEqualTo(numOfSampleAfterUpdate)
//            .isNotEqualTo(0);
//    }
}
