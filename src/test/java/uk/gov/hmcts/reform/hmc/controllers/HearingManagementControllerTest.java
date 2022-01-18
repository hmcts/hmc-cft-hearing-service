package uk.gov.hmcts.reform.hmc.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
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
import uk.gov.hmcts.reform.hmc.model.CreateHearingRequest;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.GetHearingsResponse;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;
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
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
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
    void shouldCallSaveHearingMethods() {
        CreateHearingRequest createHearingRequest = new CreateHearingRequest();
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseRef("caseReference");
        createHearingRequest.setCaseDetails(caseDetails);
        HearingResponse hearingResponse = new HearingResponse();
        hearingResponse.setHearingRequestId(1L);
        when(hearingManagementService.saveHearingRequest(createHearingRequest)).thenReturn(hearingResponse);
        doNothing().when(hearingManagementService).verifyAccess(createHearingRequest.getCaseDetails().getCaseRef());
        doNothing().when(hearingManagementService).sendRequestToHmi(1L, createHearingRequest);
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        controller.saveHearing(createHearingRequest);
        InOrder orderVerifier = Mockito.inOrder(hearingManagementService);
        orderVerifier.verify(hearingManagementService).verifyAccess("caseReference");
        orderVerifier.verify(hearingManagementService).saveHearingRequest(createHearingRequest);
        orderVerifier.verify(hearingManagementService).sendRequestToHmi(1L, createHearingRequest);
        verifyNoMoreInteractions(hearingManagementService);
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
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        doReturn(TestingUtil.deleteHearingResponse()).when(hearingManagementService)
            .deleteHearingRequest(Mockito.any(), Mockito.any());
        controller.deleteHearing(1234L, TestingUtil.deleteHearingRequest());
        verify(hearingManagementService, times(1)).deleteHearingRequest(any(), any());
    }

    @Test
    void shouldReturn404_whenDeleteRequestIdIsInValid() {
        DeleteHearingRequest request = TestingUtil.deleteHearingRequest();
        request.setCancellationReasonCode("");
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        controller.deleteHearing(1234L, request);
        verify(hearingManagementService, times(1)).deleteHearingRequest(any(), any());
    }

    @Test
    void shouldReturnHearingRequest_WhenGetHearingsForValidCaseRefLuhn() {
        final String validCaseRef = "9372710950276233";
        doReturn(TestingUtil.getHearingsResponseWhenDataIsPresent(validCaseRef)).when(hearingManagementService)
            .getHearings(Mockito.any(), Mockito.any());
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        GetHearingsResponse hearingRequest = controller.getHearings(validCaseRef, null);
        verify(hearingManagementService, times(1)).getHearings(any(), any());
        assertEquals(hearingRequest.getCaseRef(), validCaseRef);
    }

    @Test
    void shouldReturnHearingRequest_WhenGetHearingsForValidCaseRefLuhnAndStatus() {
        final String validCaseRef = "9372710950276233";
        final String status = "UPDATED"; // for example
        doReturn(TestingUtil.getHearingsResponseWhenDataIsPresent(validCaseRef)).when(hearingManagementService)
            .getHearings(Mockito.any(), Mockito.any());
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        GetHearingsResponse hearingRequest = controller.getHearings(validCaseRef, status);
        verify(hearingManagementService, times(1)).getHearings(any(), any());
        assertEquals(hearingRequest.getCaseRef(), validCaseRef);
    }

    @Test
    void shouldCallUpdateHearingRequest() {
        UpdateHearingRequest hearingRequest = new UpdateHearingRequest();
        HearingResponse hearingResponse = new HearingResponse();
        hearingResponse.setHearingRequestId(1L);
        doNothing().when(hearingManagementService).updateHearingRequest(1L, hearingRequest);
        doNothing().when(hearingManagementService).sendRequestToHmi(1L, hearingRequest);
        HearingManagementController controller = new HearingManagementController(hearingManagementService);
        controller.updateHearing(hearingRequest, 1L);
        InOrder orderVerifier = Mockito.inOrder(hearingManagementService);
        orderVerifier.verify(hearingManagementService).updateHearingRequest(1L, hearingRequest);
        orderVerifier.verify(hearingManagementService).sendRequestToHmi(1L, hearingRequest);
        verifyNoMoreInteractions(hearingManagementService);
    }
}
