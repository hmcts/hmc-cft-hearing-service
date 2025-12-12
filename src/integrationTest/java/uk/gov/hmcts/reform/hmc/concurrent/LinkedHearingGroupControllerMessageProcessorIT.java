package uk.gov.hmcts.reform.hmc.concurrent;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.config.MessageProcessor;
import uk.gov.hmcts.reform.hmc.config.MessageType;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubDeleteLinkedHearingGroupsReturn400WithDelay;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubDeleteLinkedHearingGroupsReturn500WithDelay;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubPostCreateLinkHearingGroupReturn400WithDelay;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubPostCreateLinkHearingGroupReturn500WithDelay;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubPostCreateLinkHearingGroupSuccessWithDelay;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubPutUpdateLinkHearingGroupReturn400WithDelay;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubPutUpdateLinkHearingGroupReturn500WithDelay;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubPutUpdateLinkHearingGroupSuccessWithDelay;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubSuccessfullyDeleteLinkedHearingGroupsWithDelay;
import static uk.gov.hmcts.reform.hmc.data.SecurityUtils.SERVICE_AUTHORIZATION;

@Slf4j
class LinkedHearingGroupControllerMessageProcessorIT extends BaseTest {

    private static final String URL_LINKED_HEARING_GROUP = "/linkedHearingGroup";
    private static final String EXAMPLE_TOKEN = "example-token";
    private static final String TEST_SERVICE = "xui_webapp";

    private static final long HEARING_ID_ONE = 2600000000L;
    private static final long HEARING_ID_TWO = 2600000001L;
    private static final long HEARING_ID_THREE = 2600000002L;
    private static final long HEARING_ID_FOUR = 2600000003L;

    private static final String LINKED_GROUP_REQUEST_ID = "100000";

    private static final String STATUS_HEARING_REQUESTED = "HEARING_REQUESTED";
    private static final String STATUS_AWAITING_LISTING = "AWAITING_LISTING";

    private static final String ERROR_REJECTED_BY_LA = "005 rejected by List Assist";
    private static final String ERROR_LA_FAILED_TO_RESPOND = "006 List Assist failed to respond";

    private static final String SQL_SCRIPT_DELETE_HEARING_TABLES = "classpath:sql/delete-hearing-tables.sql";
    private static final String SQL_SCRIPT_CONCURRENT_DATA = "classpath:sql/concurrent-data.sql";

    @Mock
    private ServiceBusReceivedMessageContext serviceBusReceivedMessageContext;

    @Mock
    private ServiceBusReceivedMessage serviceBusReceivedMessage;

    private final HearingRepository hearingRepository;

    private final EntityManager entityManager;

    private final MockMvc mockMvc;

    private final MessageProcessor messageProcessor;

    @Autowired
    public LinkedHearingGroupControllerMessageProcessorIT(MessageProcessor messageProcessor,
                                                          MockMvc mockMvc,
                                                          HearingRepository hearingRepository,
                                                          EntityManager entityManager) {
        this.messageProcessor = messageProcessor;
        this.mockMvc = mockMvc;
        this.hearingRepository = hearingRepository;
        this.entityManager = entityManager;
    }

    @Test
    @Sql(scripts = {SQL_SCRIPT_DELETE_HEARING_TABLES, SQL_SCRIPT_CONCURRENT_DATA})
    void concurrentCreateLinkedHearingGroupAndSyncResponse() throws Exception {
        setupSyncResponseMockBehaviour(HEARING_ID_ONE);

        // Store current request attributes and security context for use in new thread
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        SecurityContext securityContext = SecurityContextHolder.getContext();

        CountDownLatch latch = new CountDownLatch(2);
        try (ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2)) {
            Future<String> futureRequestId = scheduledExecutorService.submit(
                () -> {
                    String requestId = null;
                    try {
                        RequestContextHolder.setRequestAttributes(requestAttributes);
                        SecurityContextHolder.setContext(securityContext);

                        stubPostCreateLinkHearingGroupSuccessWithDelay(EXAMPLE_TOKEN, 5000);
                        requestId = createLinkedHearingGroup();

                        latch.countDown();
                    } catch (Throwable e) {
                        log.error("validateLinkHearing thread error: {}", e.getMessage(), e);
                    }
                    return requestId;
                }
            );

            scheduledExecutorService.schedule(
                () -> {
                    try {
                        processMessage(serviceBusReceivedMessageContext);
                        latch.countDown();
                    } catch (Throwable e) {
                        log.error("processMessage thread error: {} ", e.getMessage(), e);
                    }
                },
                2,
                TimeUnit.SECONDS
            );

            boolean result = latch.await(10, TimeUnit.SECONDS);
            assertTrue(result, "Both threads did not complete successfully in allotted time - check log for errors");

            String newRequestId = futureRequestId.get();
            assertNotNull(newRequestId, "New linked hearing group request id should not be null");
            assertFalse(newRequestId.isEmpty(), "New linked hearing group request id should not be empty");

            HearingEntity syncResponseHearing = getHearingAndLinkedGroup(HEARING_ID_ONE);
            assertHearing(syncResponseHearing,
                          new ExpectedHearingDetails(STATUS_AWAITING_LISTING,
                                                     STATUS_AWAITING_LISTING,
                                                     newRequestId,
                                                     1L));

            HearingEntity otherLinkedHearing = getHearingAndLinkedGroup(HEARING_ID_TWO);
            assertHearing(otherLinkedHearing,
                          new ExpectedHearingDetails(STATUS_HEARING_REQUESTED,
                                                     null,
                                                     newRequestId,
                                                     2L));
            verifySyncResponseMockBehaviour();
        } catch (InterruptedException e) {
            log.error("Latch timeout", e);
            throw e;
        }
    }

    @Test
    @Sql(scripts = {SQL_SCRIPT_DELETE_HEARING_TABLES, SQL_SCRIPT_CONCURRENT_DATA})
    void concurrentCreateLinkedHearingGroup400ErrorAndSyncResponse() throws InterruptedException {
        setupSyncResponseMockBehaviour(HEARING_ID_ONE);

        // Store current request attributes and security context for use in new thread
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        SecurityContext securityContext = SecurityContextHolder.getContext();

        CountDownLatch latch = new CountDownLatch(2);
        try (ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2)) {
            scheduledExecutorService.execute(
                () -> {
                    try {
                        RequestContextHolder.setRequestAttributes(requestAttributes);
                        SecurityContextHolder.setContext(securityContext);

                        stubPostCreateLinkHearingGroupReturn400WithDelay(EXAMPLE_TOKEN, 5000);
                        createLinkedHearingGroupExpectingBadRequest(ERROR_REJECTED_BY_LA);

                        latch.countDown();
                    } catch (Throwable e) {
                        log.error("validateLinkHearing thread error: {}", e.getMessage(), e);
                    }
                }
            );

            scheduledExecutorService.schedule(
                () -> {
                    try {
                        processMessage(serviceBusReceivedMessageContext);
                        latch.countDown();
                    } catch (Throwable e) {
                        log.error("processMessage thread error: {} ", e.getMessage(), e);
                    }
                },
                2,
                TimeUnit.SECONDS
            );

            boolean result = latch.await(10, TimeUnit.SECONDS);
            assertTrue(result, "Both threads did not complete successfully in allotted time - check log for errors");

            HearingEntity syncResponseHearing = getHearing(HEARING_ID_ONE);
            assertHearing(syncResponseHearing,
                          new ExpectedHearingDetails(STATUS_AWAITING_LISTING, STATUS_AWAITING_LISTING, null, null));

            HearingEntity otherLinkedHearing = getHearing(HEARING_ID_TWO);
            assertHearing(otherLinkedHearing, new ExpectedHearingDetails(STATUS_HEARING_REQUESTED, null, null, null));

            verifySyncResponseMockBehaviour();
        } catch (InterruptedException e) {
            log.error("Latch timeout", e);
            throw e;
        }
    }

    @Test
    @Sql(scripts = {SQL_SCRIPT_DELETE_HEARING_TABLES, SQL_SCRIPT_CONCURRENT_DATA})
    void concurrentCreateLinkedHearingGroup500ErrorAndSyncResponse() throws InterruptedException {
        setupSyncResponseMockBehaviour(HEARING_ID_ONE);

        // Store current request attributes and security context for use in new thread
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        SecurityContext securityContext = SecurityContextHolder.getContext();

        CountDownLatch latch = new CountDownLatch(2);
        try (ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2)) {
            scheduledExecutorService.execute(
                () -> {
                    try {
                        RequestContextHolder.setRequestAttributes(requestAttributes);
                        SecurityContextHolder.setContext(securityContext);

                        stubPostCreateLinkHearingGroupReturn500WithDelay(EXAMPLE_TOKEN, 5000);
                        createLinkedHearingGroupExpectingBadRequest(ERROR_LA_FAILED_TO_RESPOND);

                        latch.countDown();
                    } catch (Throwable e) {
                        log.error("validateLinkHearing thread error: {}", e.getMessage(), e);
                    }
                }
            );

            scheduledExecutorService.schedule(
                () -> {
                    try {
                        processMessage(serviceBusReceivedMessageContext);
                        latch.countDown();
                    } catch (Throwable e) {
                        log.error("processMessage thread error: {} ", e.getMessage(), e);
                    }
                },
                2,
                TimeUnit.SECONDS
            );

            boolean result = latch.await(10, TimeUnit.SECONDS);
            assertTrue(result, "Both threads did not complete successfully in allotted time - check log for errors");

            HearingEntity syncResponseHearing = getHearing(HEARING_ID_ONE);
            assertHearing(syncResponseHearing,
                          new ExpectedHearingDetails(STATUS_AWAITING_LISTING, STATUS_AWAITING_LISTING, null, null));

            HearingEntity otherLinkedHearing = getHearing(HEARING_ID_TWO);
            assertHearing(otherLinkedHearing, new ExpectedHearingDetails(STATUS_HEARING_REQUESTED, null, null, null));

            verifySyncResponseMockBehaviour();
        } catch (InterruptedException e) {
            log.error("Latch timeout", e);
            throw e;
        }
    }

    @Test
    @Sql(scripts = {SQL_SCRIPT_DELETE_HEARING_TABLES, SQL_SCRIPT_CONCURRENT_DATA})
    void concurrentUpdateLinkedHearingGroupAndSyncResponse() throws InterruptedException {
        setupSyncResponseMockBehaviour(HEARING_ID_THREE);

        // Store current request attributes and security context for use in new thread
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        SecurityContext securityContext = SecurityContextHolder.getContext();

        CountDownLatch latch = new CountDownLatch(2);
        try (ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2)) {
            scheduledExecutorService.execute(
                () -> {
                    try {
                        RequestContextHolder.setRequestAttributes(requestAttributes);
                        SecurityContextHolder.setContext(securityContext);

                        stubPutUpdateLinkHearingGroupSuccessWithDelay(LINKED_GROUP_REQUEST_ID, EXAMPLE_TOKEN, 5000);
                        updateLinkedHearingGroup();

                        latch.countDown();
                    } catch (Throwable e) {
                        log.error("validateLinkHearing thread error: {}", e.getMessage(), e);
                    }
                }
            );

            scheduledExecutorService.schedule(
                () -> {
                    try {
                        processMessage(serviceBusReceivedMessageContext);
                        latch.countDown();
                    } catch (Throwable e) {
                        log.error("processMessage thread error: {} ", e.getMessage(), e);
                    }
                },
                2,
                TimeUnit.SECONDS
            );

            boolean result = latch.await(10, TimeUnit.SECONDS);
            assertTrue(result, "Both threads did not complete successfully in allotted time - check log for errors");

            HearingEntity hearingOne = getHearingAndLinkedGroup(HEARING_ID_ONE);
            assertHearing(hearingOne,
                          new ExpectedHearingDetails(STATUS_HEARING_REQUESTED, null, LINKED_GROUP_REQUEST_ID, 1L));

            HearingEntity hearingTwo = getHearingAndLinkedGroup(HEARING_ID_TWO);
            assertHearing(hearingTwo,
                          new ExpectedHearingDetails(STATUS_HEARING_REQUESTED, null, LINKED_GROUP_REQUEST_ID, 2L));

            HearingEntity hearingThree = getHearing(HEARING_ID_THREE);
            assertHearing(hearingThree,
                          new ExpectedHearingDetails(STATUS_AWAITING_LISTING, STATUS_AWAITING_LISTING, null, null));

            HearingEntity hearingFour = getHearing(HEARING_ID_FOUR);
            assertHearing(hearingFour, new ExpectedHearingDetails(STATUS_HEARING_REQUESTED, null, null, null));

            verifySyncResponseMockBehaviour();
        } catch (InterruptedException e) {
            log.error("Latch timeout", e);
            throw e;
        }
    }

    @Test
    @Sql(scripts = {SQL_SCRIPT_DELETE_HEARING_TABLES, SQL_SCRIPT_CONCURRENT_DATA})
    void concurrentUpdateLinkedHearingGroup400ErrorAndSyncResponse() throws InterruptedException {
        setupSyncResponseMockBehaviour(HEARING_ID_THREE);

        // Store current request attributes and security context for use in new thread
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        SecurityContext securityContext = SecurityContextHolder.getContext();

        CountDownLatch latch = new CountDownLatch(2);
        try (ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2)) {
            scheduledExecutorService.execute(
                () -> {
                    try {
                        RequestContextHolder.setRequestAttributes(requestAttributes);
                        SecurityContextHolder.setContext(securityContext);

                        stubPutUpdateLinkHearingGroupReturn400WithDelay(LINKED_GROUP_REQUEST_ID, EXAMPLE_TOKEN, 5000);
                        updateLinkedHearingGroupExpectingBadRequest(ERROR_REJECTED_BY_LA);

                        latch.countDown();
                    } catch (Throwable e) {
                        log.error("validateLinkHearing thread error: {}", e.getMessage(), e);
                    }
                }
            );

            scheduledExecutorService.schedule(
                () -> {
                    try {
                        processMessage(serviceBusReceivedMessageContext);
                        latch.countDown();
                    } catch (Throwable e) {
                        log.error("processMessage thread error: {} ", e.getMessage(), e);
                    }
                },
                2,
                TimeUnit.SECONDS
            );

            boolean result = latch.await(10, TimeUnit.SECONDS);
            assertTrue(result, "Both threads did not complete successfully in allotted time - check log for errors");

            HearingEntity hearingOne = getHearing(HEARING_ID_ONE);
            assertHearing(hearingOne, new ExpectedHearingDetails(STATUS_HEARING_REQUESTED, null, null, null));

            HearingEntity hearingTwo = getHearing(HEARING_ID_TWO);
            assertHearing(hearingTwo, new ExpectedHearingDetails(STATUS_HEARING_REQUESTED, null, null, null));

            HearingEntity hearingThree = getHearingAndLinkedGroup(HEARING_ID_THREE);
            assertHearing(hearingThree,
                          new ExpectedHearingDetails(STATUS_AWAITING_LISTING,
                                                     STATUS_AWAITING_LISTING,
                                                     LINKED_GROUP_REQUEST_ID,
                                                     1L));

            HearingEntity hearingFour = getHearingAndLinkedGroup(HEARING_ID_FOUR);
            assertHearing(hearingFour,
                          new ExpectedHearingDetails(STATUS_HEARING_REQUESTED,
                                                     null,
                                                     LINKED_GROUP_REQUEST_ID,
                                                     2L));

            verifySyncResponseMockBehaviour();
        } catch (InterruptedException e) {
            log.error("Latch timeout", e);
            throw e;
        }
    }

    @Test
    @Sql(scripts = {SQL_SCRIPT_DELETE_HEARING_TABLES, SQL_SCRIPT_CONCURRENT_DATA})
    void concurrentUpdateLinkedHearingGroup500ErrorAndSyncResponse() throws InterruptedException {
        setupSyncResponseMockBehaviour(HEARING_ID_THREE);

        // Store current request attributes and security context for use in new thread
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        SecurityContext securityContext = SecurityContextHolder.getContext();

        CountDownLatch latch = new CountDownLatch(2);
        try (ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2)) {
            scheduledExecutorService.execute(
                () -> {
                    try {
                        RequestContextHolder.setRequestAttributes(requestAttributes);
                        SecurityContextHolder.setContext(securityContext);

                        stubPutUpdateLinkHearingGroupReturn500WithDelay(LINKED_GROUP_REQUEST_ID, EXAMPLE_TOKEN, 5000);
                        updateLinkedHearingGroupExpectingBadRequest(ERROR_LA_FAILED_TO_RESPOND);

                        latch.countDown();
                    } catch (Throwable e) {
                        log.error("validateLinkHearing thread error: {}", e.getMessage(), e);
                    }
                }
            );

            scheduledExecutorService.schedule(
                () -> {
                    try {
                        processMessage(serviceBusReceivedMessageContext);
                        latch.countDown();
                    } catch (Throwable e) {
                        log.error("processMessage thread error: {} ", e.getMessage(), e);
                    }
                },
                2,
                TimeUnit.SECONDS
            );

            boolean result = latch.await(10, TimeUnit.SECONDS);
            assertTrue(result, "Both threads did not complete successfully in allotted time - check log for errors");

            HearingEntity hearingOne = getHearing(HEARING_ID_ONE);
            assertHearing(hearingOne, new ExpectedHearingDetails(STATUS_HEARING_REQUESTED, null, null, null));

            HearingEntity hearingTwo = getHearing(HEARING_ID_TWO);
            assertHearing(hearingTwo, new ExpectedHearingDetails(STATUS_HEARING_REQUESTED, null, null, null));

            HearingEntity hearingThree = getHearingAndLinkedGroup(HEARING_ID_THREE);
            assertHearing(hearingThree,
                          new ExpectedHearingDetails(STATUS_AWAITING_LISTING,
                                                     STATUS_AWAITING_LISTING,
                                                     LINKED_GROUP_REQUEST_ID,
                                                     1L));

            HearingEntity hearingFour = getHearingAndLinkedGroup(HEARING_ID_FOUR);
            assertHearing(hearingFour,
                          new ExpectedHearingDetails(STATUS_HEARING_REQUESTED,
                                                     null,
                                                     LINKED_GROUP_REQUEST_ID,
                                                     2L));

            verifySyncResponseMockBehaviour();
        } catch (InterruptedException e) {
            log.error("Latch timeout", e);
            throw e;
        }
    }

    @Test
    @Sql(scripts = {SQL_SCRIPT_DELETE_HEARING_TABLES, SQL_SCRIPT_CONCURRENT_DATA})
    void concurrentDeleteLinkedHearingGroupAndSyncResponse() throws InterruptedException {
        setupSyncResponseMockBehaviour(HEARING_ID_THREE);

        // Store current request attributes and security context for use in new thread
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        SecurityContext securityContext = SecurityContextHolder.getContext();

        CountDownLatch latch = new CountDownLatch(2);
        try (ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2)) {
            scheduledExecutorService.execute(
                () -> {
                    try {
                        RequestContextHolder.setRequestAttributes(requestAttributes);
                        SecurityContextHolder.setContext(securityContext);

                        stubSuccessfullyDeleteLinkedHearingGroupsWithDelay(
                            EXAMPLE_TOKEN,
                            LINKED_GROUP_REQUEST_ID,
                            5000);
                        deleteLinkedHearingGroup();

                        latch.countDown();
                    } catch (Throwable e) {
                        log.error("validateLinkHearing thread error: {}", e.getMessage(), e);
                    }
                }
            );

            scheduledExecutorService.schedule(
                () -> {
                    try {
                        processMessage(serviceBusReceivedMessageContext);
                        latch.countDown();
                    } catch (Throwable e) {
                        log.error("processMessage thread error: {} ", e.getMessage(), e);
                    }
                },
                2,
                TimeUnit.SECONDS
            );

            boolean result = latch.await(10, TimeUnit.SECONDS);
            assertTrue(result, "Both threads did not complete successfully in allotted time - check log for errors");

            HearingEntity syncResponseHearing = getHearing(HEARING_ID_THREE);
            assertHearing(syncResponseHearing,
                          new ExpectedHearingDetails(STATUS_AWAITING_LISTING, STATUS_AWAITING_LISTING, null, null));

            HearingEntity otherLinkedHearing = getHearing(HEARING_ID_FOUR);
            assertHearing(otherLinkedHearing, new ExpectedHearingDetails(STATUS_HEARING_REQUESTED, null, null, null));

            verifySyncResponseMockBehaviour();
        } catch (InterruptedException e) {
            log.error("Latch timeout", e);
            throw e;
        }
    }

    @Test
    @Sql(scripts = {SQL_SCRIPT_DELETE_HEARING_TABLES, SQL_SCRIPT_CONCURRENT_DATA})
    void concurrentDeleteLinkedHearingGroup400ErrorAndSyncResponse() throws InterruptedException {
        setupSyncResponseMockBehaviour(HEARING_ID_THREE);

        // Store current request attributes and security context for use in new thread
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        SecurityContext securityContext = SecurityContextHolder.getContext();

        CountDownLatch latch = new CountDownLatch(2);
        try (ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2)) {
            scheduledExecutorService.execute(
                () -> {
                    try {
                        RequestContextHolder.setRequestAttributes(requestAttributes);
                        SecurityContextHolder.setContext(securityContext);

                        stubDeleteLinkedHearingGroupsReturn400WithDelay(EXAMPLE_TOKEN, LINKED_GROUP_REQUEST_ID, 5000);
                        deleteLinkedHearingGroupExpectingBadRequest(ERROR_REJECTED_BY_LA);

                        latch.countDown();
                    } catch (Throwable e) {
                        log.error("validateLinkHearing thread error: {}", e.getMessage(), e);
                    }
                }
            );

            scheduledExecutorService.schedule(
                () -> {
                    try {
                        processMessage(serviceBusReceivedMessageContext);
                        latch.countDown();
                    } catch (Throwable e) {
                        log.error("processMessage thread error: {} ", e.getMessage(), e);
                    }
                },
                2,
                TimeUnit.SECONDS
            );

            boolean result = latch.await(10, TimeUnit.SECONDS);
            assertTrue(result, "Both threads did not complete successfully in allotted time - check log for errors");

            HearingEntity syncResponseHearing = getHearingAndLinkedGroup(HEARING_ID_THREE);
            assertHearing(syncResponseHearing,
                          new ExpectedHearingDetails(STATUS_AWAITING_LISTING,
                                                     STATUS_AWAITING_LISTING,
                                                     LINKED_GROUP_REQUEST_ID,
                                                     1L));

            HearingEntity otherLinkedHearing = getHearingAndLinkedGroup(HEARING_ID_FOUR);
            assertHearing(otherLinkedHearing,
                          new ExpectedHearingDetails(STATUS_HEARING_REQUESTED, null, LINKED_GROUP_REQUEST_ID, 2L));

            verifySyncResponseMockBehaviour();
        } catch (InterruptedException e) {
            log.error("Latch timeout", e);
            throw e;
        }
    }

    @Test
    @Sql(scripts = {SQL_SCRIPT_DELETE_HEARING_TABLES, SQL_SCRIPT_CONCURRENT_DATA})
    void concurrentDeleteLinkedHearingGroup500ErrorAndSyncResponse() throws InterruptedException {
        setupSyncResponseMockBehaviour(HEARING_ID_THREE);

        // Store current request attributes and security context for use in new thread
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        SecurityContext securityContext = SecurityContextHolder.getContext();

        CountDownLatch latch = new CountDownLatch(2);
        try (ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2)) {
            scheduledExecutorService.execute(
                () -> {
                    try {
                        RequestContextHolder.setRequestAttributes(requestAttributes);
                        SecurityContextHolder.setContext(securityContext);

                        stubDeleteLinkedHearingGroupsReturn500WithDelay(EXAMPLE_TOKEN, LINKED_GROUP_REQUEST_ID, 5000);
                        deleteLinkedHearingGroupExpectingBadRequest(ERROR_LA_FAILED_TO_RESPOND);

                        latch.countDown();
                    } catch (Throwable e) {
                        log.error("validateLinkHearing thread error: {}", e.getMessage(), e);
                    }
                }
            );

            scheduledExecutorService.schedule(
                () -> {
                    try {
                        processMessage(serviceBusReceivedMessageContext);
                        latch.countDown();
                    } catch (Throwable e) {
                        log.error("processMessage thread error: {} ", e.getMessage(), e);
                    }
                },
                2,
                TimeUnit.SECONDS
            );

            boolean result = latch.await(10, TimeUnit.SECONDS);
            assertTrue(result, "Both threads did not complete successfully in allotted time - check log for errors");

            HearingEntity syncResponseHearing = getHearingAndLinkedGroup(HEARING_ID_THREE);
            assertHearing(syncResponseHearing,
                          new ExpectedHearingDetails(STATUS_AWAITING_LISTING,
                                                     STATUS_AWAITING_LISTING,
                                                     LINKED_GROUP_REQUEST_ID,
                                                     1L));

            HearingEntity otherLinkedHearing = getHearingAndLinkedGroup(HEARING_ID_FOUR);
            assertHearing(otherLinkedHearing,
                          new ExpectedHearingDetails(STATUS_HEARING_REQUESTED, null, LINKED_GROUP_REQUEST_ID, 2L));

            verifySyncResponseMockBehaviour();
        } catch (InterruptedException e) {
            log.error("Latch timeout", e);
            throw e;
        }
    }

    private String createLinkedHearingGroup() throws Exception {
        String dummyS2SToken = generateDummyS2SToken(TEST_SERVICE);
        String hearingLinkedGroupRequest =
            createOrderedHearingLinkedGroupRequestJson("Group name", "GR", "Group comments");

        MvcResult result = mockMvc.perform(post(URL_LINKED_HEARING_GROUP)
                                               .header(SERVICE_AUTHORIZATION, dummyS2SToken)
                                               .contentType(MediaType.APPLICATION_JSON_VALUE)
                                               .content(hearingLinkedGroupRequest))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.hearingGroupRequestId", Matchers.notNullValue()))
            .andReturn();

        Pattern requestIdPattern = Pattern.compile("\"hearingGroupRequestId\"\\s*:\\s*\"([0-9]+)\"");
        Matcher matcher = requestIdPattern.matcher(result.getResponse().getContentAsString());
        return matcher.find() ? matcher.group(1) : "";
    }

    private void createLinkedHearingGroupExpectingBadRequest(String expectedError) throws Exception {
        String dummyS2SToken = generateDummyS2SToken(TEST_SERVICE);
        String hearingLinkedGroupRequest =
            createOrderedHearingLinkedGroupRequestJson("Group name", "GR", "Group comments");

        mockMvc.perform(post(URL_LINKED_HEARING_GROUP)
                            .header(SERVICE_AUTHORIZATION, dummyS2SToken)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(hearingLinkedGroupRequest))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.errors", Matchers.hasItem(expectedError)));
    }

    private void updateLinkedHearingGroup() throws Exception {
        String dummyS2SToken = generateDummyS2SToken(TEST_SERVICE);
        String hearingLinkedGroupRequest =
            createOrderedHearingLinkedGroupRequestJson("New group name", "NGR", "New group comments");

        mockMvc.perform(put(URL_LINKED_HEARING_GROUP + "?id=100000")
                            .header(SERVICE_AUTHORIZATION, dummyS2SToken)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(hearingLinkedGroupRequest))
            .andExpect(status().isOk());
    }

    private void updateLinkedHearingGroupExpectingBadRequest(String expectedError) throws Exception {
        String dummyS2SToken = generateDummyS2SToken(TEST_SERVICE);
        String hearingLinkedGroupRequest =
            createOrderedHearingLinkedGroupRequestJson("New group name", "NGR", "New group comments");

        mockMvc.perform(put(URL_LINKED_HEARING_GROUP + "?id=100000")
                            .header(SERVICE_AUTHORIZATION, dummyS2SToken)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(hearingLinkedGroupRequest))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.errors", Matchers.hasItem(expectedError)));
    }

    private void deleteLinkedHearingGroup() throws Exception {
        String dummyS2SToken = generateDummyS2SToken(TEST_SERVICE);

        mockMvc.perform(delete(URL_LINKED_HEARING_GROUP + "/100000")
                            .header(SERVICE_AUTHORIZATION, dummyS2SToken))
            .andExpect(status().isOk());
    }

    private void deleteLinkedHearingGroupExpectingBadRequest(String expectedError) throws Exception {
        String dummyS2SToken = generateDummyS2SToken(TEST_SERVICE);

        mockMvc.perform(delete(URL_LINKED_HEARING_GROUP + "/100000")
                            .header(SERVICE_AUTHORIZATION, dummyS2SToken))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.errors", Matchers.hasItem(expectedError)));
    }

    private void processMessage(ServiceBusReceivedMessageContext serviceBusReceivedMessageContext) {
        messageProcessor.processMessage(serviceBusReceivedMessageContext);
    }

    private void setupSyncResponseMockBehaviour(Long hearingId) {
        BinaryData syncResponseSuccessPayload = createSyncResponseSuccess();
        Map<String, Object> appProperties = createAppProperties(hearingId);

        when(serviceBusReceivedMessage.getBody()).thenReturn(syncResponseSuccessPayload);
        when(serviceBusReceivedMessage.getApplicationProperties()).thenReturn(appProperties);
        when(serviceBusReceivedMessage.getMessageId()).thenReturn("test-message-id");
        when(serviceBusReceivedMessageContext.getMessage()).thenReturn(serviceBusReceivedMessage);
    }

    private void verifySyncResponseMockBehaviour() {
        verify(serviceBusReceivedMessageContext, times(4)).getMessage();
        verify(serviceBusReceivedMessage).getMessageId();
        verify(serviceBusReceivedMessage, times(2)).getApplicationProperties();
        verify(serviceBusReceivedMessage).getBody();
    }

    private Map<String, Object> createAppProperties(Long hearingId) {
        Map<String, Object> appProperties = new HashMap<>();

        appProperties.put("hearing_id", String.valueOf(hearingId));
        appProperties.put("message_type", MessageType.LA_SYNC_HEARING_RESPONSE);

        return appProperties;
    }

    private BinaryData createSyncResponseSuccess() {
        String syncResponseSuccess = """
            {
                "listAssistHttpStatus": 202
            }""";
        return BinaryData.fromString(syncResponseSuccess);
    }

    private String createOrderedHearingLinkedGroupRequestJson(String name, String reason, String comments) {
        return """
            {
                "groupDetails": {
                    "groupName": "%s",
                    "groupReason": "%s",
                    "groupLinkType": "Ordered",
                    "groupComments": "%s"
                },
                "hearingsInGroup": [
                    {"hearingId": "2600000000", "hearingOrder": "1"},
                    {"hearingId": "2600000001", "hearingOrder": "2"}
                ]
            }""".formatted(name, reason, comments);
    }

    private HearingEntity getHearing(Long hearingId) {
        Optional<HearingEntity> hearing = hearingRepository.findById(hearingId);
        assertTrue(hearing.isPresent(), "Hearing " + hearingId + " should be present");

        return hearing.get();
    }

    private HearingEntity getHearingAndLinkedGroup(Long hearingId) {
        String query = "SELECT h FROM HearingEntity h JOIN FETCH h.linkedGroupDetails WHERE h.id = " + hearingId;
        return entityManager.createQuery(query, HearingEntity.class).getSingleResult();
    }

    private void assertHearing(HearingEntity hearing, ExpectedHearingDetails expectedHearingDetails) {
        String prefix = "Hearing " + hearing.getId() + " ";

        assertEquals(expectedHearingDetails.status(), hearing.getStatus(), prefix + "has unexpected status");
        assertNull(hearing.getErrorCode(), prefix + "error code should be null");
        assertNull(hearing.getErrorDescription(), prefix + "error description should be null");

        if (expectedHearingDetails.requestId() == null) {
            assertNull(hearing.getLinkedGroupDetails(), prefix + "linked group details should be null");
        } else {
            assertEquals(expectedHearingDetails.requestId(),
                         hearing.getLinkedGroupDetails().getRequestId(),
                         prefix + "linked group has unexpected request id");
        }

        if (expectedHearingDetails.linkedOrder() == null) {
            assertNull(hearing.getLinkedOrder(), prefix + "linked order should be null");
        } else {
            assertEquals(expectedHearingDetails.linkedOrder(),
                         hearing.getLinkedOrder(),
                         prefix + "has unexpected linked order");
        }

        assertTrue(hearing.getIsLinkedFlag(), prefix + "has unexpected linked flag value");
        assertNull(hearing.getDeploymentId(), prefix + "deployment id should be null");

        if (expectedHearingDetails.goodStatus() == null) {
            assertNull(hearing.getLastGoodStatus(), prefix + "last good status should be null");
        } else {
            assertEquals(expectedHearingDetails.goodStatus(),
                         hearing.getLastGoodStatus(),
                         prefix + "has unexpected last good status");
        }
    }

    private record ExpectedHearingDetails(String status, String goodStatus, String requestId, Long linkedOrder) {}
}
