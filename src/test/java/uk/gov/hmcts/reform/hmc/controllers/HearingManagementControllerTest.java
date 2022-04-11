package uk.gov.hmcts.reform.hmc.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.hmc.TestIdamConfiguration;
import uk.gov.hmcts.reform.hmc.config.SecurityConfiguration;
import uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.GetHearingsResponse;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.RequestDetails;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;
import uk.gov.hmcts.reform.hmc.security.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.hmc.service.HearingManagementService;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.AMEND_HEARING;
import static uk.gov.hmcts.reform.hmc.constants.Constants.REQUEST_HEARING;

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

    @Nested
    @DisplayName("saveHearing")
    class SaveHearing {

        @Test
        void shouldReturn400_whenHearing_Details_Are_NotPresent() {
            // hearing request with no hearing details
            HearingRequest hearingRequest = generateHearingRequest(false);
            hearingRequest.setHearingDetails(null);

            HearingResponse hearingResponse = generateHearingResponse();
            when(hearingManagementService.saveHearingRequest(hearingRequest)).thenReturn(hearingResponse);
            doNothing().when(hearingManagementService).sendRequestToHmiAndQueue(
                hearingResponse.getHearingRequestId(), hearingRequest, REQUEST_HEARING);

            HearingManagementController controller = new HearingManagementController(hearingManagementService);
            controller.saveHearing(hearingRequest);
            verify(hearingManagementService, times(1)).saveHearingRequest(any());
        }

        @Test
        void shouldReturn400_whenCase_Details_Are_NotPresent() {
            // hearing request with no request details
            HearingRequest hearingRequest = generateHearingRequest(false);
            hearingRequest.setCaseDetails(null);

            HearingResponse hearingResponse = generateHearingResponse();
            doNothing().when(hearingManagementService).verifyAccess(any());
            when(hearingManagementService.saveHearingRequest(hearingRequest)).thenReturn(hearingResponse);
            doNothing().when(hearingManagementService).sendRequestToHmiAndQueue(1L, hearingRequest,
                                                                                REQUEST_HEARING
            );

            HearingManagementController controller = new HearingManagementController(hearingManagementService);
            controller.saveHearing(hearingRequest);
            verify(hearingManagementService, times(1)).saveHearingRequest(any());
        }

        @Test
        void shouldReturn201_whenHearingRequestData() {
            // hearing request - valid
            HearingRequest hearingRequest = generateHearingRequest(false);

            HearingResponse hearingResponse = generateHearingResponse();
            when(hearingManagementService.saveHearingRequest(hearingRequest)).thenReturn(hearingResponse);
            doNothing().when(hearingManagementService).sendRequestToHmiAndQueue(
                hearingResponse.getHearingRequestId(), hearingRequest,
                REQUEST_HEARING
            );

            HearingManagementController controller = new HearingManagementController(hearingManagementService);
            controller.saveHearing(hearingRequest);
            verify(hearingManagementService, times(1)).saveHearingRequest(any());
            verify(hearingManagementService, times(1)).verifyAccess(any());
        }


        @Test
        void shouldReturn201_whenHearingRequestDataWithOrgIdNull() {
            // hearing request - valid
            HearingRequest hearingRequest = generateHearingRequest(true);

            HearingResponse hearingResponse = generateHearingResponse();
            when(hearingManagementService.saveHearingRequest(hearingRequest)).thenReturn(hearingResponse);
            doNothing().when(hearingManagementService).sendRequestToHmiAndQueue(
                hearingResponse.getHearingRequestId(), hearingRequest,
                REQUEST_HEARING
            );

            HearingManagementController controller = new HearingManagementController(hearingManagementService);
            controller.saveHearing(hearingRequest);
            verify(hearingManagementService, times(1)).saveHearingRequest(any());
            verify(hearingManagementService, times(1)).verifyAccess(any());
        }

        @Test
        void shouldCallSaveHearingMethods() {
            HearingRequest hearingRequest = new HearingRequest();
            CaseDetails caseDetails = new CaseDetails();
            caseDetails.setCaseRef("caseReference");
            hearingRequest.setCaseDetails(caseDetails);

            HearingResponse hearingResponse = new HearingResponse();
            hearingResponse.setHearingRequestId(1L);
            when(hearingManagementService.saveHearingRequest(hearingRequest)).thenReturn(hearingResponse);
            doNothing().when(hearingManagementService).verifyAccess(
                hearingRequest.getCaseDetails().getCaseRef());
            doNothing().when(hearingManagementService).sendRequestToHmiAndQueue(
                hearingResponse.getHearingRequestId(), hearingRequest,
                REQUEST_HEARING
            );
            HearingManagementController controller = new HearingManagementController(hearingManagementService);
            controller.saveHearing(hearingRequest);
            InOrder orderVerifier = Mockito.inOrder(hearingManagementService);
            orderVerifier.verify(hearingManagementService).verifyAccess("caseReference");
            orderVerifier.verify(hearingManagementService).saveHearingRequest(hearingRequest);
            orderVerifier.verify(hearingManagementService)
                .sendRequestToHmiAndQueue(1L, hearingRequest, REQUEST_HEARING);
            verifyNoMoreInteractions(hearingManagementService);
        }
    }

    @Nested
    @DisplayName("getHearing")
    class GetHearing {
        @Test
        void shouldReturn204_whenRequestIdIsValid() {
            HearingManagementController controller = new HearingManagementController(hearingManagementService);
            controller.getHearing(1234L, true);
            verify(hearingManagementService, times(1)).getHearingRequest(any(), anyBoolean());
        }

        @Test
        void shouldReturn200_whenRequestIdIsValid() {
            HearingManagementController controller = new HearingManagementController(hearingManagementService);
            controller.getHearing(1234L, false);
            verify(hearingManagementService, times(1)).getHearingRequest(any(), anyBoolean());
        }
    }

    @Nested
    @DisplayName("deleteHearing")
    class DeleteHearing {
        @Test
        void shouldReturn200_whenDeleteRequestIdIsValid() {
            doReturn(TestingUtil.deleteHearingResponse()).when(hearingManagementService)
                .deleteHearingRequest(any(), any());
            HearingManagementController controller = new HearingManagementController(hearingManagementService);
            controller.deleteHearing(1234L, TestingUtil.deleteHearingRequest());
            verify(hearingManagementService, times(1)).deleteHearingRequest(any(), any());
        }

        @Test
        void shouldReturn404_whenDeleteRequestIdIsInvalid() {
            DeleteHearingRequest request = TestingUtil.deleteHearingRequest();
            request.setCancellationReasonCode("");

            HearingResponse hearingResponse = generateHearingResponse();
            when(hearingManagementService.deleteHearingRequest(any(), any())).thenReturn(hearingResponse);

            HearingManagementController controller = new HearingManagementController(hearingManagementService);
            controller.deleteHearing(1234L, request);
            verify(hearingManagementService, times(1)).deleteHearingRequest(any(), any());
        }
    }

    @Nested
    @DisplayName("getHearings")
    class GetHearings {
        @Test
        void shouldReturnHearingRequest_WhenGetHearingsForValidCaseRefLuhn() {
            final String validCaseRef = "9372710950276233";
            doReturn(TestingUtil.getHearingsResponseWhenDataIsPresent(validCaseRef)).when(hearingManagementService)
                .getHearings(any(), any());
            HearingManagementController controller = new HearingManagementController(hearingManagementService);
            GetHearingsResponse hearingRequest = controller.getHearings(validCaseRef, null);
            verify(hearingManagementService, times(1)).getHearings(any(), any());
            assertEquals(hearingRequest.getCaseRef(), validCaseRef);
            assertTrue(hearingRequest.getCaseHearings().get(0).getHearingIsLinkedFlag());
        }

        @Test
        void shouldReturnHearingRequest_WhenGetHearingsForValidCaseRefLuhnAndStatus() {
            final String validCaseRef = "9372710950276233";
            final String status = "UPDATED"; // for example
            doReturn(TestingUtil.getHearingsResponseWhenDataIsPresent(validCaseRef)).when(hearingManagementService)
                .getHearings(any(), any());
            HearingManagementController controller = new HearingManagementController(hearingManagementService);
            GetHearingsResponse hearingRequest = controller.getHearings(validCaseRef, status);
            verify(hearingManagementService, times(1)).getHearings(any(), any());
            assertEquals(hearingRequest.getCaseRef(), validCaseRef);
            assertTrue(hearingRequest.getCaseHearings().get(0).getHearingIsLinkedFlag());
        }
    }

    @Nested
    @DisplayName("updateHearing")
    class UpdateHearing {
        @Test
        void shouldCallUpdateHearingRequest() {
            final long hearingId = 2000000000L;
            UpdateHearingRequest hearingRequest = generateUpdateHearingRequest(false);
            HearingResponse hearingResponse = generateHearingResponse();
            when(hearingManagementService.updateHearingRequest(hearingId, hearingRequest)).thenReturn(hearingResponse);

            doNothing().when(hearingManagementService).sendRequestToHmiAndQueue(1L, hearingRequest,
                                                                                AMEND_HEARING
            );
            HearingManagementController controller = new HearingManagementController(hearingManagementService);
            controller.updateHearing(hearingRequest, hearingId);
            InOrder orderVerifier = Mockito.inOrder(hearingManagementService);
            orderVerifier.verify(hearingManagementService).updateHearingRequest(hearingId, hearingRequest);
            orderVerifier.verify(hearingManagementService)
                .sendRequestToHmiAndQueue(hearingId, hearingRequest, AMEND_HEARING);
            verifyNoMoreInteractions(hearingManagementService);
        }

        @Test
        void shouldCallUpdateHearingRequestWithPartyWithOrgIdNull() {
            final long hearingId = 2000000000L;
            UpdateHearingRequest hearingRequest = generateUpdateHearingRequest(true);
            HearingResponse hearingResponse = generateHearingResponse();
            when(hearingManagementService.updateHearingRequest(hearingId, hearingRequest)).thenReturn(hearingResponse);

            doNothing().when(hearingManagementService).sendRequestToHmiAndQueue(1L, hearingRequest,
                                                                                AMEND_HEARING
            );
            HearingManagementController controller = new HearingManagementController(hearingManagementService);
            controller.updateHearing(hearingRequest, hearingId);
            InOrder orderVerifier = Mockito.inOrder(hearingManagementService);
            orderVerifier.verify(hearingManagementService).updateHearingRequest(hearingId, hearingRequest);
            orderVerifier.verify(hearingManagementService)
                .sendRequestToHmiAndQueue(hearingId, hearingRequest, AMEND_HEARING);
            verifyNoMoreInteractions(hearingManagementService);
        }
    }

    @Nested
    @DisplayName("updateHearingCompletion")
    class UpdateHearingCompletion {

        @Test
        void shouldInvokeHearingCompletion() {
            final long hearingId = 2000000000L;
            HearingManagementController controller = new HearingManagementController(hearingManagementService);
            controller.hearingCompletion(hearingId);
            InOrder orderVerifier = Mockito.inOrder(hearingManagementService);
            orderVerifier.verify(hearingManagementService).hearingCompletion(hearingId);
        }
    }

    private HearingResponse generateHearingResponse() {
        final long hearingId = 2000000000L;
        HearingResponse hearingResponse = new HearingResponse();
        hearingResponse.setHearingRequestId(hearingId);
        hearingResponse.setVersionNumber(1);
        hearingResponse.setStatus(PutHearingStatus.HEARING_REQUESTED.name());
        return hearingResponse;
    }

    private HearingRequest generateHearingRequest(boolean isCftOrganisationIdNull) {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        hearingRequest.setPartyDetails(TestingUtil.partyDetails());
        hearingRequest.getPartyDetails().get(0).setIndividualDetails(TestingUtil.individualDetails());
        if (isCftOrganisationIdNull) {
            hearingRequest.getPartyDetails().get(1).setOrganisationDetails(TestingUtil.organisationDetailsIdNull());
        } else {
            hearingRequest.getPartyDetails().get(1).setOrganisationDetails(TestingUtil.organisationDetails());
        }
        return hearingRequest;
    }

    /**
     * generate Create Hearing Request.
     *
     * @return hearingRequest Create Hearing Request
     */
    private UpdateHearingRequest generateUpdateHearingRequest(boolean isCftOrganisationIdNull) {
        UpdateHearingRequest hearingRequest = new UpdateHearingRequest();
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setVersionNumber(2);
        hearingRequest.setRequestDetails(requestDetails);
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        hearingRequest.setPartyDetails(TestingUtil.partyDetails());
        hearingRequest.getPartyDetails().get(0).setIndividualDetails(TestingUtil.individualDetails());
        if (isCftOrganisationIdNull) {
            hearingRequest.getPartyDetails().get(1).setOrganisationDetails(TestingUtil.organisationDetailsIdNull());
        } else {
            hearingRequest.getPartyDetails().get(1).setOrganisationDetails(TestingUtil.organisationDetails());
        }
        return hearingRequest;
    }

}
