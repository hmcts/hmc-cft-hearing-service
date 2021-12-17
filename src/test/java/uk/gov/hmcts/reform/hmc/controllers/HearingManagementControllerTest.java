package uk.gov.hmcts.reform.hmc.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.hmc.TestIdamConfiguration;
import uk.gov.hmcts.reform.hmc.config.SecurityConfiguration;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.CaseHearing;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingsGetResponse;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;
import uk.gov.hmcts.reform.hmc.security.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.hmc.service.HearingManagementService;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = HearingManagementController.class,
    excludeFilters = @ComponentScan.Filter(type = ASSIGNABLE_TYPE, classes =
        {SecurityConfiguration.class, JwtGrantedAuthoritiesConverter.class}))
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(TestIdamConfiguration.class)
class HearingManagementControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    WebApplicationContext webApplicationContext;

    @MockBean
    private HearingManagementService hearingManagementService;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private static final MediaType JSON_CONTENT_TYPE = new MediaType(
        MediaType.APPLICATION_JSON.getType(),
        MediaType.APPLICATION_JSON.getSubtype(),
        Charset.forName("utf8"));

    @Test
    void shouldReturn400_whenRequest_Details_Are_NotPresent() {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseRef("200000L");
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.setCaseDetails(caseDetails);
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        controller.saveHearing(hearingRequest);
        verify(hearingManagementService, times(1)).saveHearingRequest(any());

    }

    @Test
    void shouldReturn400_whenHearing_Details_Are_NotPresent() {
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        controller.saveHearing(hearingRequest);
        verify(hearingManagementService, times(1)).saveHearingRequest(any());
    }

    @Test
    void shouldReturn400_whenCase_Details_Are_NotPresent() {
        HearingRequest hearingRequest = new HearingRequest();
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseRef("200000L");
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.setCaseDetails(caseDetails);
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        controller.saveHearing(hearingRequest);
        verify(hearingManagementService, times(1)).saveHearingRequest(any());
    }

    @Test
    void shouldReturn201_whenHearingRequestData() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        controller.saveHearing(hearingRequest);
        verify(hearingManagementService, times(1)).saveHearingRequest(any());
        verify(hearingManagementService, times(1)).verifyAccess(any());
    }

    @Test
    void shouldReturn200_whenRequestIdIsValid() {
        doNothing().when(hearingManagementService).getHearingRequest(Mockito.any(), anyBoolean());
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        controller.getHearing(1234L, true);
        verify(hearingManagementService, times(1)).getHearingRequest(any(), anyBoolean());

    }

    @Test
    void shouldReturn200_whenDeleteRequestIdIsValid() {
        doReturn(generateHearingResponse()).when(hearingManagementService)
            .deleteHearingRequest(Mockito.any(), Mockito.any());
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        controller.deleteHearing(1234L, TestingUtil.deleteHearingRequest());
        verify(hearingManagementService, times(1)).deleteHearingRequest(any(), any());
    }

    @Test
    void shouldReturn404_whenDeleteRequestIdIsInValid() {
        DeleteHearingRequest request = TestingUtil.deleteHearingRequest();
        request.setCancellationReasonCode("");
        doReturn(generateHearingResponse()).when(hearingManagementService)
            .deleteHearingRequest(Mockito.any(), Mockito.any());
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        controller.deleteHearing(1234L, request);
        verify(hearingManagementService, times(1)).deleteHearingRequest(any(), any());
    }

    @Test
    void shouldReturnHearingRequest_WhenGetHearingsForValidCaseRefLuhn() throws Exception {
        final String validCaseRef = "9372710950276233";
        doReturn(createHearingRequest(validCaseRef)).when(hearingManagementService)
            .validateGetHearingsRequest(Mockito.any(), Mockito.any());
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        HearingsGetResponse hearingsGetResponse = controller.getHearingsRequest(validCaseRef, null);
        verify(hearingManagementService, times(1)).validateGetHearingsRequest(any(), any());
        Assert.isTrue(hearingsGetResponse.getCaseRef().equals(validCaseRef));
    }

    @Test
    void shouldReturnHearingRequest_WhenGetHearingsForValidCaseRefLuhnAndStatus() throws Exception {
        final String validCaseRef = "9372710950276233";
        final String status = "UPDATED"; // for example
        HearingRequest hearingRequest =  createHearingRequest(validCaseRef, status);
        doReturn(generateHearingsGetResponse(hearingRequest)).when(hearingManagementService)
            .validateGetHearingsRequest(Mockito.any(), Mockito.any());
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        HearingsGetResponse hearingsGetResponse = controller.getHearingsRequest(validCaseRef, status);
        verify(hearingManagementService, times(1)).validateGetHearingsRequest(any(), any());
        Assert.isTrue(hearingsGetResponse.getCaseRef().equals(validCaseRef));
    }

    /**
     * generate Hearings Response from Hearing Request.
     *
     * @return HearingsGetResponse hearings Get Ressponse
     */
    private HearingsGetResponse generateHearingsGetResponse(HearingRequest hearingRequest) {
        HearingsGetResponse hearingsGetResponse = new HearingsGetResponse();
        hearingsGetResponse.setCaseRef(hearingRequest.getCaseDetails().getCaseRef());
        hearingsGetResponse.setHmctsServiceCode(hearingRequest.getCaseDetails().getHmctsServiceCode());
        hearingsGetResponse.setCaseHearings(createCaseHearings());
        return hearingsGetResponse;
    }

    @Test
    void shouldCallUpdateHearingRequest() {
        doNothing().when(hearingManagementService).updateHearingRequest(Mockito.any(), Mockito.any());
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        controller.updateHearing(null, 1234L);
        verify(hearingManagementService, times(1)).updateHearingRequest(any(), any());
    }

    private HearingResponse generateHearingResponse() {
        HearingResponse hearingResponse = new HearingResponse();
        hearingResponse.setVersionNumber(3);
        hearingResponse.setHearingRequestId(200000000111L);
        hearingResponse.setStatus("LISTED");
        hearingResponse.setTimeStamp(LocalDateTime.now());
        return hearingResponse;
    }

    private HearingRequest createHearingRequest(String caseRef) {
        return createHearingRequest(caseRef, null);
    }

    private HearingRequest createHearingRequest(String caseRef, String status) {
        HearingRequest hearingRequest = new HearingRequest();
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseRef(caseRef);
        hearingRequest.setCaseDetails(caseDetails);
        return hearingRequest;
    }

    private List<CaseHearing> createCaseHearings() {
        List<CaseHearing> caseHearings = new ArrayList<>();
        IntStream.range(1, 4).forEach(i ->
                                          caseHearings.add(createCaseHearing(i))
        );
        return caseHearings;
    }

    private CaseHearing createCaseHearing(Integer id) {
        CaseHearing caseHearing = new CaseHearing();
        caseHearing.setHearingID("hearing" + id);
        caseHearing.setHearingRequestDateTime(LocalDateTime.now());
        caseHearing.setHearingType("hearingType" + id);
        caseHearing.setHmcStatus("hmcStatus" + id);
        caseHearing.setLastResponseReceivedDateTime(LocalDateTime.now());
        caseHearing.setResponseVersion("00" + id);
        caseHearing.setHearingListingStatus("liststat" + id);
        caseHearing.setListAssistCaseStatus("status" + id);
        caseHearing.setHearingDaySchedule(createHearingDaySchedule(id));
        return caseHearing;
    }

    private HearingDaySchedule createHearingDaySchedule(Integer id) {
        HearingDaySchedule hearingDaySchedule = new HearingDaySchedule();
        hearingDaySchedule.setHearingRoomId("room" + id);
        hearingDaySchedule.setHearingJudgeId("judge" + id);
        hearingDaySchedule.setHearingStartDateTime(LocalDateTime.now());
        hearingDaySchedule.setHearingEndDateTime(LocalDateTime.now());
        return hearingDaySchedule;
    }
}
