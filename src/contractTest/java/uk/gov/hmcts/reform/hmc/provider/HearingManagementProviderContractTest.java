package uk.gov.hmcts.reform.hmc.provider;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.VersionSelector;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.BasePactTesting;
import uk.gov.hmcts.reform.hmc.controllers.HearingManagementController;
import uk.gov.hmcts.reform.hmc.controllers.PartiesNotifiedController;
import uk.gov.hmcts.reform.hmc.controllers.UnNotifiedHearingsController;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.CaseHearing;
import uk.gov.hmcts.reform.hmc.model.GetHearingResponse;
import uk.gov.hmcts.reform.hmc.model.GetHearingsResponse;
import uk.gov.hmcts.reform.hmc.model.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.UnNotifiedHearingsResponse;
import uk.gov.hmcts.reform.hmc.model.hmi.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.hmi.RequestDetails;
import uk.gov.hmcts.reform.hmc.model.partiesnotified.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.hmc.model.partiesnotified.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.hmc.service.AccessControlService;
import uk.gov.hmcts.reform.hmc.service.HearingManagementService;
import uk.gov.hmcts.reform.hmc.service.PartiesNotifiedService;
import uk.gov.hmcts.reform.hmc.service.UnNotifiedHearingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.HEARING_VIEWER;

@Provider(BasePactTesting.PROVIDER_NAME)
@PactBroker(
    scheme = "${PACT_BROKER_SCHEME:http}",
    host = "${PACT_BROKER_URL:localhost}",
    port = "${PACT_BROKER_PORT:}",
    consumers = {"civil_service"},
    consumerVersionSelectors = {@VersionSelector(tag = "master", consumer = "civil_service")}
)
@IgnoreNoPactsToVerify
class HearingManagementProviderContractTest {

    private static final long HEARING_ID = 2000000000000000L;
    private static final String CASE_REFERENCE = "1671000000000018";
    private static final String SERVICE_CODE = "AAA7";
    private static final LocalDateTime HEARING_RECEIVED = LocalDateTime.of(2024, 10, 3, 11, 15);
    private static final LocalDateTime HEARING_START = LocalDateTime.of(2024, 10, 20, 9, 30);
    private static final LocalDateTime HEARING_END = HEARING_START.plusHours(2);
    private static final LocalDateTime PARTIES_NOTIFIED_DATE = LocalDateTime.of(2024, 10, 5, 14, 45);

    @Mock
    private HearingManagementService hearingManagementService;
    @Mock
    private PartiesNotifiedService partiesNotifiedService;
    @Mock
    private UnNotifiedHearingService unNotifiedHearingService;
    @Mock
    private AccessControlService accessControlService;
    @Mock
    private ApplicationParams applicationParams;
    @Mock
    private SecurityUtils securityUtils;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp(PactVerificationContext context) {
        mocks = MockitoAnnotations.openMocks(this);

        HearingManagementController hearingController = new HearingManagementController(
            hearingManagementService,
            accessControlService,
            applicationParams,
            securityUtils
        );
        PartiesNotifiedController partiesNotifiedController = new PartiesNotifiedController(
            partiesNotifiedService,
            accessControlService,
            securityUtils
        );
        UnNotifiedHearingsController unNotifiedHearingsController = new UnNotifiedHearingsController(
            unNotifiedHearingService
        );

        ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
            hearingController,
            partiesNotifiedController,
            unNotifiedHearingsController
        ).setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper)).build();

        if (context != null) {
            MockMvcTestTarget target = new MockMvcTestTarget();
            target.setMockMvc(mockMvc);
            context.setTarget(target);
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        reset(
            hearingManagementService,
            partiesNotifiedService,
            unNotifiedHearingService,
            accessControlService,
            securityUtils
        );
        mocks.close();
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPactInteractions(PactVerificationContext context) {
        if (context != null) {
            context.verifyInteraction();
        }
    }

    @State("Hearing exists for supplied id")
    void hearingExists() {
        when(hearingManagementService.getStatus(HEARING_ID)).thenReturn("LISTED");
        when(hearingManagementService.getHearingRequest(HEARING_ID, false))
            .thenReturn(ResponseEntity.ok(getHearingResponse()));
    }

    @State("Parties notified exist for supplied hearing id")
    void partiesNotifiedExist() {
        when(partiesNotifiedService.getPartiesNotified(HEARING_ID)).thenReturn(partiesNotifiedResponses());
    }

    @State("Parties notified payload can be updated")
    void partiesNotifiedCanBeUpdated() {
        when(securityUtils.getServiceNameFromS2SToken(anyString())).thenReturn("civil_service");
        doNothing().when(partiesNotifiedService).getPartiesNotified(
            eq(HEARING_ID),
            eq(1),
            eq(PARTIES_NOTIFIED_DATE),
            any(),
            eq("civil_service")
        );
    }

    @State("Unnotified hearings exist for service code")
    void unnotifiedHearingsExist() {
        UnNotifiedHearingsResponse response = new UnNotifiedHearingsResponse();
        response.setHearingIds(List.of(String.valueOf(HEARING_ID)));
        response.setTotalFound(1L);
        when(unNotifiedHearingService.getUnNotifiedHearings(eq(SERVICE_CODE), any(), any(), any()))
            .thenReturn(response);
    }

    @State("Hearings exist for case id")
    void hearingsExist() {
        when(accessControlService.verifyCaseAccess(eq(CASE_REFERENCE), anyList(), any()))
            .thenReturn(List.of(HEARING_VIEWER));
        when(hearingManagementService.getHearings(CASE_REFERENCE, "LISTED"))
            .thenReturn(getHearingsResponse());
    }

    private GetHearingResponse getHearingResponse() {
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setHearingRequestId(String.valueOf(HEARING_ID));
        requestDetails.setVersionNumber(1);
        requestDetails.setStatus("LISTED");
        requestDetails.setTimestamp(LocalDateTime.of(2024, 10, 1, 10, 0));

        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseRef(CASE_REFERENCE);
        caseDetails.setHmctsServiceCode(SERVICE_CODE);

        HearingResponse hearingResponse = new HearingResponse();
        hearingResponse.setListAssistTransactionID("TRANSACTION-123");
        hearingResponse.setReceivedDateTime(HEARING_RECEIVED);
        hearingResponse.setLaCaseStatus("LISTED");
        hearingResponse.setListingStatus("FIXED");
        hearingResponse.setHearingDaySchedule(List.of(hearingDaySchedule(true)));

        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID("P1");
        partyDetails.setPartyType("IND");
        partyDetails.setPartyRole("CLAIMANT");

        GetHearingResponse response = new GetHearingResponse();
        response.setRequestDetails(requestDetails);
        response.setCaseDetails(caseDetails);
        response.setHearingResponse(hearingResponse);
        response.setPartyDetails(List.of(partyDetails));
        return response;
    }

    private PartiesNotifiedResponses partiesNotifiedResponses() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode serviceData = objectMapper.createObjectNode();
        serviceData.put("hearingNoticeGenerated", true);
        serviceData.put("hearingLocation", "0001");
        serviceData.put("hearingDate", HEARING_START.toString());
        ObjectNode day = serviceData.putArray("days").addObject();
        day.put("hearingStartDateTime", HEARING_START.toString());
        day.put("hearingEndDateTime", HEARING_END.toString());

        PartiesNotifiedResponse notifiedResponse = new PartiesNotifiedResponse();
        notifiedResponse.setResponseReceivedDateTime(PARTIES_NOTIFIED_DATE);
        notifiedResponse.setRequestVersion(1);
        notifiedResponse.setPartiesNotified(PARTIES_NOTIFIED_DATE.minusHours(1));
        notifiedResponse.setServiceData(serviceData);

        PartiesNotifiedResponses responses = new PartiesNotifiedResponses();
        responses.setHearingID(String.valueOf(HEARING_ID));
        responses.setResponses(List.of(notifiedResponse));
        return responses;
    }

    private GetHearingsResponse getHearingsResponse() {
        CaseHearing caseHearing = new CaseHearing();
        caseHearing.setHearingId(1000000000000000L);
        caseHearing.setHearingRequestDateTime(LocalDateTime.of(2024, 10, 2, 8, 30));
        caseHearing.setHmcStatus("LISTED");
        caseHearing.setRequestVersion(1);
        caseHearing.setHearingDaySchedule(List.of(hearingDaySchedule(false)));

        GetHearingsResponse response = new GetHearingsResponse();
        response.setHmctsServiceCode(SERVICE_CODE);
        response.setCaseRef(CASE_REFERENCE);
        response.setCaseHearings(List.of(caseHearing));
        return response;
    }

    private HearingDaySchedule hearingDaySchedule(boolean includeVenue) {
        HearingDaySchedule schedule = new HearingDaySchedule();
        schedule.setHearingStartDateTime(HEARING_START);
        schedule.setHearingEndDateTime(HEARING_END);
        if (includeVenue) {
            schedule.setHearingVenueId("0001");
            schedule.setHearingRoomId("Room A");
        }
        return schedule;
    }
}
