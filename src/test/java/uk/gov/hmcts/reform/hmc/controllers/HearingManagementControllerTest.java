package uk.gov.hmcts.reform.hmc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.hmc.model.CaseHearing;
import uk.gov.hmcts.reform.hmc.model.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingsGetResponse;
import uk.gov.hmcts.reform.hmc.service.HearingManagementService;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = HearingManagementController.class)
class HearingManagementControllerTest {

    @Autowired
    protected ObjectMapper objectMapper;

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

    private static final Logger logger = LoggerFactory.getLogger(HearingManagementControllerTest.class);


    private static final MediaType JSON_CONTENT_TYPE = new MediaType(
        MediaType.APPLICATION_JSON.getType(),
        MediaType.APPLICATION_JSON.getSubtype(),
        Charset.forName("utf8"));

    @Test
    void shouldReturn400_whenRequest_Details_Are_NotPresent() {
        doNothing().when(hearingManagementService).validateHearingRequest(Mockito.any());
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        controller.invokeHearing(hearingRequest);
        verify(hearingManagementService, times(1)).validateHearingRequest(any());

    }

    @Test
    void shouldReturn400_whenHearing_Details_Are_NotPresent() {
        doNothing().when(hearingManagementService).validateHearingRequest(Mockito.any());
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        controller.invokeHearing(hearingRequest);
        verify(hearingManagementService, times(1)).validateHearingRequest(any());
    }

    @Test
    void shouldReturn400_whenCase_Details_Are_NotPresent() {
        doNothing().when(hearingManagementService).validateHearingRequest(Mockito.any());
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        controller.invokeHearing(hearingRequest);
        verify(hearingManagementService, times(1)).validateHearingRequest(any());
    }

    @Test
    void shouldReturn202_whenHearingRequestDeta() {
        doNothing().when(hearingManagementService).validateHearingRequest(Mockito.any());
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        controller.invokeHearing(hearingRequest);
        verify(hearingManagementService, times(1)).validateHearingRequest(any());
    }

    @Test
    void shouldReturnHearingsGetResponse_WhenGetHearingsForValidCaseRefLuhn() throws Exception {
        final String validCaseRef = "9372710950276233";
        doReturn(createHearingsGetResponse(validCaseRef)).when(hearingManagementService)
            .validateGetHearingsRequest(Mockito.any(), Mockito.any());
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        HearingsGetResponse hearingsGetResponse = controller.getHearingsRequest(validCaseRef, null);
        verify(hearingManagementService, times(1)).validateGetHearingsRequest(any(), any());
        Assert.isTrue(hearingsGetResponse.getCaseRef().equals(validCaseRef));
    }

    @Test
    void shouldReturnHearingsGetResponse_WhenGetHearingsForValidCaseRefLuhnAndStatus() throws Exception {
        final String validCaseRef = "9372710950276233";
        final String status = "status1"; // for example
        doReturn(createHearingsGetResponse(validCaseRef, status)).when(hearingManagementService)
            .validateGetHearingsRequest(Mockito.any(), Mockito.any());
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        HearingsGetResponse hearingsGetResponse = controller.getHearingsRequest(validCaseRef, status);
        verify(hearingManagementService, times(1)).validateGetHearingsRequest(any(), any());
        Assert.isTrue(hearingsGetResponse.getCaseRef().equals(validCaseRef));
    }

    private HearingsGetResponse createHearingsGetResponse(String caseRef) {
        return createHearingsGetResponse(caseRef, null);
    }

    private HearingsGetResponse createHearingsGetResponse(String caseRef, String status) {
        HearingsGetResponse hearingsGetResponse = new HearingsGetResponse();
        hearingsGetResponse.setHmctsServiceCode("hmc1");
        hearingsGetResponse.setCaseRef(caseRef);
        hearingsGetResponse.setCaseHearings(createCaseHearings(status));
        try {
            logger.info(objectMapper.writeValueAsString(hearingsGetResponse));
        } catch (Exception e) {
            logger.error("Unable to write log for mapper value");
        }
        return hearingsGetResponse;
    }

    private List<CaseHearing> createCaseHearings(String status) {
        List<CaseHearing> caseHearings = new ArrayList<>();
        if (null != status) {
            CaseHearing caseHearing = createCaseHearing(1, status);
            caseHearings.add(caseHearing);
        } else {
            CaseHearing caseHearing1 = createCaseHearing(1, "NEW");
            caseHearings.add(caseHearing1);
            CaseHearing caseHearing2 = createCaseHearing(2, "UPDATED");
            caseHearings.add(caseHearing2);
            CaseHearing caseHearing3 = createCaseHearing(3, "PROGRESS");
            caseHearings.add(caseHearing3);
            CaseHearing caseHearing4 = createCaseHearing(4, "ARCHIVE");
            caseHearings.add(caseHearing4);
        }
        return caseHearings;
    }

    private CaseHearing createCaseHearing(Integer id, String status) {
        CaseHearing caseHearing = new CaseHearing();
        caseHearing.setHearingID("hearing" + id);
        caseHearing.setHearingRequestDateTime(LocalDateTime.now());
        caseHearing.setHearingType("hearingType" + id);
        caseHearing.setHmcStatus("hmcStatus" + id);
        caseHearing.setLastResponseReceivedDateTime(LocalDateTime.now());
        caseHearing.setResponseVersion("00" + id);
        caseHearing.setHearingListingStatus("liststat" + id);
        caseHearing.setLstAssistCaseStatus(status);
        caseHearing.setHearingDaySchedule(createHearingDaySchedule(id));
        return caseHearing;
    }

    private HearingDaySchedule createHearingDaySchedule(Integer id) {
        HearingDaySchedule hearingDaySchedule = new HearingDaySchedule();
        hearingDaySchedule.setHearingRoomId("room" + id);
        hearingDaySchedule.setHearingJudgeId("judge" + id);
        hearingDaySchedule.setHearingStartDateTime(LocalDateTime.now());
        hearingDaySchedule.setHearingEndDateTime(LocalDateTime.now());
        hearingDaySchedule.setListAssistSessionID("session" + id);
        return hearingDaySchedule;
    }

}
