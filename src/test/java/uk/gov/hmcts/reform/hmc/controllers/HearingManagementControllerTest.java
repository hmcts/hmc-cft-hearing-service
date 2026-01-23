package uk.gov.hmcts.reform.hmc.controllers;

import com.microsoft.applicationinsights.core.dependencies.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.client.datastore.model.DataStoreCaseDetails;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus;
import uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.GetHearingsResponse;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.RequestDetails;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;
import uk.gov.hmcts.reform.hmc.service.AccessControlService;
import uk.gov.hmcts.reform.hmc.service.HearingManagementService;
import uk.gov.hmcts.reform.hmc.service.common.HearingStatusAuditService;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.HEARING_VIEWER;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.LISTED_HEARING_VIEWER;

@ExtendWith(MockitoExtension.class)
class HearingManagementControllerTest {

    private static final String CLIENT_S2S_TOKEN = "xui_webapp";

    @Mock
    private HearingManagementService hearingManagementService;

    @Mock
    private AccessControlService accessControlService;

    @Mock
    private ApplicationParams applicationParams;

    @Mock
    HearingStatusAuditService hearingStatusAuditService;

    @Mock
    SecurityUtils securityUtils;

    private HearingManagementController controller;

    @BeforeEach
    void setUp() {
        controller = new HearingManagementController(hearingManagementService,
                                                     accessControlService,
                                                     applicationParams,
                                                     securityUtils);

        hearingStatusAuditService.saveAuditTriageDetailsWithCreatedDate(any(),any(),any(),any(),any(),any(),any());
        hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDate(any(),any(),any(),any(),any(),any(),any());
    }

    @Nested
    @DisplayName("saveHearing")
    class SaveHearing {

        @BeforeEach
        void setUp() {
            when(securityUtils.getServiceNameFromS2SToken(any())).thenReturn(CLIENT_S2S_TOKEN);
            when(applicationParams.isHmctsDeploymentIdEnabled()).thenReturn(false);
        }

        @Test
        void shouldReturn400_whenHearing_Details_Are_NotPresent() {
            // hearing request with no hearing details
            HearingRequest hearingRequest = generateHearingRequest(false);
            hearingRequest.setHearingDetails(null);

            HearingResponse hearingResponse = generateHearingResponse();
            when(hearingManagementService.saveHearingRequest(hearingRequest, null, CLIENT_S2S_TOKEN))
                .thenReturn(hearingResponse);

            controller.saveHearing(null, CLIENT_S2S_TOKEN, hearingRequest);
            verify(hearingManagementService).saveHearingRequest(any(), any(), any());
        }

        @Test
        void shouldReturn400_whenCase_Details_Are_NotPresent() {
            // hearing request with no request details
            HearingRequest hearingRequest = generateHearingRequest(false);
            hearingRequest.setCaseDetails(null);

            HearingResponse hearingResponse = generateHearingResponse();
            when(hearingManagementService.saveHearingRequest(hearingRequest, null, CLIENT_S2S_TOKEN))
                .thenReturn(hearingResponse);

            controller.saveHearing(null, CLIENT_S2S_TOKEN,hearingRequest);
            verify(hearingManagementService).saveHearingRequest(any(), any(), any());
        }

        @Test
        void shouldReturn201_whenHearingRequestData() {
            // hearing request - valid
            HearingRequest hearingRequest = generateHearingRequest(false);

            HearingResponse hearingResponse = generateHearingResponse();
            when(hearingManagementService.saveHearingRequest(hearingRequest, null, CLIENT_S2S_TOKEN))
                .thenReturn(hearingResponse);

            controller.saveHearing(null, CLIENT_S2S_TOKEN, hearingRequest);
            verify(hearingManagementService).saveHearingRequest(any(), any(), any());
        }

        @Test
        void shouldReturn201_whenHearingRequestDataWithOrgIdNull() {
            // hearing request - valid
            HearingRequest hearingRequest = generateHearingRequest(true);

            HearingResponse hearingResponse = generateHearingResponse();
            when(hearingManagementService.saveHearingRequest(hearingRequest, null, CLIENT_S2S_TOKEN))
                .thenReturn(hearingResponse);

            controller.saveHearing(null, CLIENT_S2S_TOKEN, hearingRequest);
            verify(hearingManagementService).saveHearingRequest(any(), any(), any());
        }

        @Test
        void shouldCallSaveHearingMethods() {
            HearingRequest hearingRequest = new HearingRequest();
            CaseDetails caseDetails = new CaseDetails();
            caseDetails.setCaseRef("caseReference");
            hearingRequest.setCaseDetails(caseDetails);

            HearingResponse hearingResponse = new HearingResponse();
            hearingResponse.setHearingRequestId(1L);
            when(hearingManagementService.saveHearingRequest(hearingRequest, null, CLIENT_S2S_TOKEN))
                .thenReturn(hearingResponse);

            controller.saveHearing(null, CLIENT_S2S_TOKEN, hearingRequest);
            InOrder orderVerifier = Mockito.inOrder(hearingManagementService);
            orderVerifier.verify(hearingManagementService).saveHearingRequest(hearingRequest, null, CLIENT_S2S_TOKEN);
            verifyNoMoreInteractions(hearingManagementService);
        }
    }

    @Nested
    @DisplayName("getHearing")
    class GetHearing {

        @Test
        void shouldReturn204_whenRequestIdIsValid() {
            controller.getHearing(1234L, true);
            verify(hearingManagementService).getHearingRequest(any(), anyBoolean());
        }

        @Test
        void shouldReturn200_whenRequestIdIsValid() {
            controller.getHearing(1234L, false);
            verify(hearingManagementService).getHearingRequest(any(), anyBoolean());
        }
    }

    @Nested
    @DisplayName("deleteHearing")
    class DeleteHearing {

        @Test
        void shouldReturn200_whenDeleteRequestIdIsValid() {
            when(hearingManagementService.deleteHearingRequest(any(), any(), any()))
                .thenReturn(TestingUtil.deleteHearingResponse());

            controller.deleteHearing(1234L, CLIENT_S2S_TOKEN, TestingUtil.deleteHearingRequest());
            verify(hearingManagementService).deleteHearingRequest(any(), any(), any());
        }

        @Test
        void shouldReturn404_whenDeleteRequestIdIsInvalid() {
            DeleteHearingRequest request = TestingUtil.deleteHearingRequest();
            request.setCancellationReasonCodes(List.of(""));

            HearingResponse hearingResponse = generateHearingResponse();
            when(hearingManagementService.deleteHearingRequest(any(), any(), any())).thenReturn(hearingResponse);

            controller.deleteHearing(1234L, CLIENT_S2S_TOKEN, request);
            verify(hearingManagementService).deleteHearingRequest(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("getHearings")
    class GetHearings {

        @Test
        void shouldReturnHearingRequest_WhenGetHearingsForValidCaseRefLuhn() {
            final String validCaseRef = "9372710950276233";
            when(hearingManagementService.getHearings(any(), any()))
                .thenReturn(TestingUtil.getHearingsResponseWhenDataIsPresent(validCaseRef, "HEARING_REQUESTED"));

            GetHearingsResponse hearingRequest = controller.getHearings(validCaseRef, null);
            verify(hearingManagementService).getHearings(any(), any());
            assertThat(hearingRequest.getCaseRef()).isEqualTo(validCaseRef);
            assertThat(hearingRequest.getCaseHearings().getFirst().getHearingIsLinkedFlag()).isTrue();
        }

        @Test
        void shouldReturnHearingRequest_WhenGetHearingsForValidCaseRefAndListedStatus() {
            final String validCaseRef = "9372710950276233";
            when(hearingManagementService.getHearings(any(), any()))
                .thenReturn(TestingUtil.getHearingsResponseWhenDataIsPresent(validCaseRef, "LISTED"));

            List<String> rolesRequired = Lists.newArrayList(HEARING_VIEWER, LISTED_HEARING_VIEWER);
            List<String> filteredRoleAssignments = Lists.newArrayList(LISTED_HEARING_VIEWER);

            when(accessControlService.verifyCaseAccess(validCaseRef, rolesRequired, null))
                .thenReturn(filteredRoleAssignments);

            GetHearingsResponse hearingRequest = controller.getHearings(validCaseRef, null);
            verify(hearingManagementService).getHearings(validCaseRef, "LISTED");
            assertThat(hearingRequest.getCaseRef()).isEqualTo(validCaseRef);
            assertThat(hearingRequest.getCaseHearings().getFirst().getHmcStatus()).isEqualTo("LISTED");
            assertThat(hearingRequest.getCaseHearings().getFirst().getHearingIsLinkedFlag()).isTrue();
        }

        @Test
        void shouldReturnHearingRequest_WhenGetHearingsForValidCaseRefLuhnAndStatus() {
            final String validCaseRef = "9372710950276233";
            final String status = "UPDATED"; // for example
            when(hearingManagementService.getEmptyHearingsResponse(any()))
                .thenReturn(TestingUtil.getHearingsResponseWhenDataIsPresent(validCaseRef, "HEARING_REQUESTED"));

            GetHearingsResponse hearingRequest = controller.getHearings(validCaseRef, status);
            verify(hearingManagementService).getEmptyHearingsResponse(validCaseRef);
            assertThat(hearingRequest.getCaseRef()).isEqualTo(validCaseRef);
            assertThat(hearingRequest.getCaseHearings().getFirst().getHearingIsLinkedFlag()).isTrue();
        }
    }

    @Nested
    @DisplayName("updateHearing")
    class UpdateHearing {

        @BeforeEach
        void setUp() {
            when(securityUtils.getServiceNameFromS2SToken(any())).thenReturn(CLIENT_S2S_TOKEN);
            when(applicationParams.isHmctsDeploymentIdEnabled()).thenReturn(false);
        }

        @Test
        void shouldCallUpdateHearingRequest() {
            final long hearingId = 2000000000L;
            UpdateHearingRequest hearingRequest = generateUpdateHearingRequest(false);
            HearingResponse hearingResponse = generateHearingResponse();
            when(hearingManagementService.updateHearingRequest(hearingId, hearingRequest, "", CLIENT_S2S_TOKEN))
                .thenReturn(hearingResponse);

            controller.updateHearing("", hearingRequest,CLIENT_S2S_TOKEN, hearingId);
            InOrder orderVerifier = Mockito.inOrder(hearingManagementService);
            orderVerifier.verify(hearingManagementService)
                .updateHearingRequest(hearingId, hearingRequest, "", CLIENT_S2S_TOKEN);
            verifyNoMoreInteractions(hearingManagementService);
        }

        @Test
        void shouldCallUpdateHearingRequestWithPartyWithOrgIdNull() {
            final long hearingId = 2000000000L;
            UpdateHearingRequest hearingRequest = generateUpdateHearingRequest(true);
            HearingResponse hearingResponse = generateHearingResponse();
            when(hearingManagementService.updateHearingRequest(hearingId, hearingRequest, "", CLIENT_S2S_TOKEN))
                .thenReturn(hearingResponse);

            controller.updateHearing("", hearingRequest, CLIENT_S2S_TOKEN, hearingId);
            InOrder orderVerifier = Mockito.inOrder(hearingManagementService);
            orderVerifier.verify(hearingManagementService)
                .updateHearingRequest(hearingId, hearingRequest, "", CLIENT_S2S_TOKEN);
            verifyNoMoreInteractions(hearingManagementService);
        }
    }

    @Nested
    @DisplayName("updateHearingCompletion")
    class UpdateHearingCompletion {

        @BeforeEach
        void setUp() {
            when(securityUtils.getServiceNameFromS2SToken(any())).thenReturn(CLIENT_S2S_TOKEN);
        }

        @Test
        void shouldInvokeHearingCompletion() {
            final long hearingId = 2000000000L;

            controller.hearingCompletion(hearingId, CLIENT_S2S_TOKEN);
            InOrder orderVerifier = Mockito.inOrder(hearingManagementService);
            orderVerifier.verify(hearingManagementService).hearingCompletion(hearingId, CLIENT_S2S_TOKEN);
        }
    }

    @Nested
    @DisplayName("getHearingsForListOfCases")
    class GetHearingsForListOfCases {

        @Test
        void shouldReturnHearingResponseForListed() {
            List<String> ccdCaseRefs  = List.of("9372710950276233");
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .id("9372710950276233")
                .jurisdiction("CMC")
                .build();
            List<DataStoreCaseDetails> cases = List.of(caseDetails);
            when(hearingManagementService.getHearings(any(), any()))
                .thenReturn(TestingUtil.getHearingsResponseWhenDataIsPresent(ccdCaseRefs.getFirst(),
                                                                             HearingStatus.LISTED.name()));
            when(hearingManagementService.getCaseSearchResults(any(), any(), any())).thenReturn(cases);

            List<GetHearingsResponse> hearingsResponseList =
                controller.getHearingsForListOfCases(ccdCaseRefs, null, "AAT_PRIVATE");
            verify(hearingManagementService).getHearings(any(), any());
            assertThat(hearingsResponseList.getFirst().getCaseRef()).isEqualTo(ccdCaseRefs.getFirst());
            assertThat(hearingsResponseList.getFirst().getCaseHearings().getFirst().getHearingIsLinkedFlag()).isTrue();
        }

        @Test
        void shouldReturnHearingResponseForListOfCases() {
            List<String> ccdCaseRefs  = List.of("9372710950276233", "9856815055686759");
            DataStoreCaseDetails caseDetails = DataStoreCaseDetails.builder()
                .id("9856815055686759")
                .jurisdiction("CMC")
                .build();
            List<DataStoreCaseDetails> cases = List.of(caseDetails);
            when(hearingManagementService.getCaseSearchResults(any(), any(), any())).thenReturn(cases);
            for (DataStoreCaseDetails dataStoreCaseDetails : cases) {
                when(hearingManagementService.getHearings(any(), any()))
                    .thenReturn(TestingUtil.getHearingsResponseWhenDataIsPresent(dataStoreCaseDetails.getId(), null));
            }

            List<GetHearingsResponse> hearingsResponseList =
                controller.getHearingsForListOfCases(ccdCaseRefs, null, "AAT_PRIVATE");
            verify(hearingManagementService).getHearings(any(), any());
            assertThat(hearingsResponseList.getFirst().getCaseRef()).isEqualTo(ccdCaseRefs.get(1));
            assertThat(hearingsResponseList.getFirst().getCaseHearings().getFirst().getHearingIsLinkedFlag()).isTrue();
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
