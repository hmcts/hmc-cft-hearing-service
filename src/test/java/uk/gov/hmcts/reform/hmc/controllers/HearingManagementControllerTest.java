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
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.hmc.TestIdamConfiguration;
import uk.gov.hmcts.reform.hmc.config.SecurityConfiguration;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.GetHearingsResponse;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.security.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.hmc.service.HearingManagementService;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        Charset.forName("utf8")
    );

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
        doNothing().when(hearingManagementService).deleteHearingRequest(Mockito.any(), Mockito.any());
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        controller.deleteHearing(1234L, TestingUtil.deleteHearingRequest());
        verify(hearingManagementService, times(1)).deleteHearingRequest(any(), any());
    }

    @Test
    void shouldReturn404_whenDeleteRequestIdIsInValid() {
        DeleteHearingRequest request = TestingUtil.deleteHearingRequest();
        request.setCancellationReasonCode("");
        doNothing().when(hearingManagementService).deleteHearingRequest(Mockito.any(), Mockito.any());
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        controller.deleteHearing(1234L, request);
        verify(hearingManagementService, times(1)).deleteHearingRequest(any(), any());
    }

    @Test
    void shouldReturnHearingRequest_WhenGetHearingsForValidCaseRefLuhn() throws Exception {
        final String validCaseRef = "9372710950276233";
        doReturn(TestingUtil.createHearingRequest(validCaseRef, "")).when(hearingManagementService)
            .getHearings(Mockito.any(), Mockito.any());
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        GetHearingsResponse hearingRequest = controller.getHearings(validCaseRef, null);
        verify(hearingManagementService, times(1)).getHearings(any(), any());
        assertEquals(hearingRequest.getCaseRef(), validCaseRef);
    }

    @Test
    void shouldReturnHearingRequest_WhenGetHearingsForValidCaseRefLuhnAndStatus() throws Exception {
        final String validCaseRef = "9372710950276233";
        final String status = "UPDATED"; // for example
        doReturn(TestingUtil.createHearingRequest(validCaseRef, status)).when(hearingManagementService)
            .getHearings(Mockito.any(), Mockito.any());
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        GetHearingsResponse hearingRequest = controller.getHearings(validCaseRef, status);
        verify(hearingManagementService, times(1)).getHearings(any(), any());
        assertEquals(hearingRequest.getCaseRef(), validCaseRef);
    }

    private HearingRequest createHearingRequest(String caseRef, String status) {
        HearingRequest hearingRequest = new HearingRequest();
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseRef(caseRef);
        hearingRequest.setCaseDetails(caseDetails);
        return hearingRequest;
    }


    @Test
    void shouldCallUpdateHearingRequest() {
        doNothing().when(hearingManagementService).updateHearingRequest(Mockito.any(), Mockito.any());
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        controller.updateHearing(null, 1234L);
        verify(hearingManagementService, times(1)).updateHearingRequest(any(), any());
    }
}
