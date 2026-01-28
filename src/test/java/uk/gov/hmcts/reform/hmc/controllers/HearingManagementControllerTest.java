package uk.gov.hmcts.reform.hmc.controllers;

import com.microsoft.applicationinsights.core.dependencies.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import uk.gov.hmcts.reform.hmc.model.CaseHearing;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.GetHearingsResponse;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.HearingsForListOfCasesPaginatedRequest;
import uk.gov.hmcts.reform.hmc.model.HearingsForListOfCasesPaginatedRequestCaseReference;
import uk.gov.hmcts.reform.hmc.model.RequestDetails;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;
import uk.gov.hmcts.reform.hmc.service.AccessControlService;
import uk.gov.hmcts.reform.hmc.service.HearingManagementService;
import uk.gov.hmcts.reform.hmc.service.common.HearingStatusAuditService;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
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

    @Nested
    @DisplayName("getHearingsForListOfCasesPaginated")
    class GetHearingsForListOfCasesPaginated {

        private static final String ACCESS_ROLE_HEARING_MANAGER = "hearing-manager";
        private static final String ACCESS_ROLE_HEARING_VIEWER = "hearing-viewer";
        private static final String ACCESS_ROLE_LISTED_HEARING_VIEWER = "listed-hearing-viewer";

        private static final String HEARING_STATUS_LISTED = "LISTED";
        private static final String HEARING_STATUS_AWAITING_LISTING = "AWAITING_LISTING";

        private static final String CASE_REFERENCE = "1000100010001002";
        private static final String CASE_TYPE = "AAT_PRIVATE";

        private static final List<String> REQUIRED_ROLES =
            List.of(ACCESS_ROLE_HEARING_VIEWER, ACCESS_ROLE_LISTED_HEARING_VIEWER);

        @Test
        void shouldReturnNullWhenNoCasesFound() {
            List<String> caseReferences = List.of(CASE_REFERENCE);

            when(hearingManagementService.getCaseSearchResultsPaginated(10, 0, caseReferences, null, CASE_TYPE))
                .thenReturn(Collections.emptyList());

            HearingsForListOfCasesPaginatedRequest request = createRequest(caseReferences);
            List<GetHearingsResponse> response =
                controller.getHearingsForListOfCasesPaginated(null, CASE_TYPE, request);

            assertThat(response).isNull();

            verify(hearingManagementService).getCaseSearchResultsPaginated(10, 0, caseReferences, null, CASE_TYPE);
        }

        @Test
        void shouldReturnNullWhenCaseHasNoHearings() {
            List<String> caseReferences = List.of(CASE_REFERENCE);

            DataStoreCaseDetails caseDetails =
                DataStoreCaseDetails.builder()
                    .id(CASE_REFERENCE)
                    .caseTypeId(CASE_TYPE)
                    .jurisdiction("CMC")
                    .build();
            List<DataStoreCaseDetails> caseDetailsList = List.of(caseDetails);

            when(hearingManagementService
                     .getCaseSearchResultsPaginated(10, 0, caseReferences, null, CASE_TYPE))
                .thenReturn(caseDetailsList);

            when(accessControlService.verifyCaseAccess(CASE_REFERENCE, REQUIRED_ROLES, caseDetails))
                .thenReturn(List.of(ACCESS_ROLE_LISTED_HEARING_VIEWER));

            GetHearingsResponse getHearingsResponse = createEmptyGetHearingsResponse();
            when(hearingManagementService.getHearings(CASE_REFERENCE, HEARING_STATUS_LISTED))
                .thenReturn(getHearingsResponse);

            HearingsForListOfCasesPaginatedRequest request = createRequest(caseReferences);
            List<GetHearingsResponse> getHearingsResponseList =
                controller.getHearingsForListOfCasesPaginated(null, CASE_TYPE, request);

            assertThat(getHearingsResponseList).isNull();

            verify(hearingManagementService)
                .getCaseSearchResultsPaginated(10, 0, caseReferences, null, CASE_TYPE);
            verify(accessControlService).verifyCaseAccess(CASE_REFERENCE, REQUIRED_ROLES, caseDetails);
            verify(hearingManagementService).getHearings(CASE_REFERENCE, HEARING_STATUS_LISTED);
        }

        @ParameterizedTest(name = "{index}: Hearing status {0}, Role Assignments {1}, Actual Hearing Status {2}")
        @MethodSource("getHearingsStatusAndRoleAssignments")
        void shouldGetHearingsUsingStatusBasedOnRoleAssignments(String requestedHearingStatus,
                                                                List<String> roleAssignments,
                                                                String actualHearingStatus) {
            List<String> caseReferences = List.of(CASE_REFERENCE);

            DataStoreCaseDetails caseDetails =
                DataStoreCaseDetails.builder()
                    .id(CASE_REFERENCE)
                    .caseTypeId(CASE_TYPE)
                    .jurisdiction("CMC")
                    .build();
            List<DataStoreCaseDetails> caseDetailsList = List.of(caseDetails);

            when(hearingManagementService
                     .getCaseSearchResultsPaginated(10, 0, caseReferences, requestedHearingStatus, CASE_TYPE))
                .thenReturn(caseDetailsList);

            when(accessControlService.verifyCaseAccess(CASE_REFERENCE, REQUIRED_ROLES, caseDetails))
                .thenReturn(roleAssignments);

            GetHearingsResponse getHearingsResponse = createGetHearingsResponse();
            when(hearingManagementService.getHearings(CASE_REFERENCE, actualHearingStatus))
                .thenReturn(getHearingsResponse);

            HearingsForListOfCasesPaginatedRequest request = createRequest(caseReferences);
            List<GetHearingsResponse> returnedGetHearingsResponseList =
                controller.getHearingsForListOfCasesPaginated(requestedHearingStatus, CASE_TYPE, request);

            assertThat(returnedGetHearingsResponseList)
                .isNotNull()
                .hasSize(1);

            GetHearingsResponse returnedGetHearingsResponse = returnedGetHearingsResponseList.getFirst();
            assertThat(returnedGetHearingsResponse.getCaseRef()).isEqualTo(CASE_REFERENCE);

            List<CaseHearing> caseHearingList = returnedGetHearingsResponse.getCaseHearings();
            assertThat(caseHearingList)
                .isNotNull()
                .hasSize(1);

            CaseHearing caseHearing = caseHearingList.getFirst();
            assertThat(caseHearing.getHearingId()).isEqualTo(2000000000L);

            verify(hearingManagementService)
                .getCaseSearchResultsPaginated(10, 0, caseReferences, requestedHearingStatus, CASE_TYPE);
            verify(accessControlService).verifyCaseAccess(CASE_REFERENCE, REQUIRED_ROLES, caseDetails);
            verify(hearingManagementService).getHearings(CASE_REFERENCE, actualHearingStatus);
        }

        @ParameterizedTest(name = "{index}: Hearing Status {0}, Role Assignments {1}")
        @MethodSource("emptyHearingsResponseStatusAndRoleAssignments")
        void shouldGetEmptyHearingsResponse(String requestedHearingStatus, List<String> roleAssignments) {
            List<String> caseReferences = List.of(CASE_REFERENCE);

            DataStoreCaseDetails caseDetails =
                DataStoreCaseDetails.builder()
                    .id(CASE_REFERENCE)
                    .caseTypeId(CASE_TYPE)
                    .jurisdiction("CMC")
                    .build();
            List<DataStoreCaseDetails> caseDetailsList = List.of(caseDetails);

            when(hearingManagementService
                     .getCaseSearchResultsPaginated(10, 0, caseReferences, requestedHearingStatus, CASE_TYPE))
                .thenReturn(caseDetailsList);

            when(accessControlService.verifyCaseAccess(CASE_REFERENCE, REQUIRED_ROLES, caseDetails))
                .thenReturn(roleAssignments);

            GetHearingsResponse emptyGetHearingsResponse = createEmptyGetHearingsResponse();
            when(hearingManagementService.getEmptyHearingsResponse(CASE_REFERENCE))
                .thenReturn(emptyGetHearingsResponse);

            HearingsForListOfCasesPaginatedRequest request = createRequest(caseReferences);
            List<GetHearingsResponse> response =
                controller.getHearingsForListOfCasesPaginated(requestedHearingStatus, CASE_TYPE, request);

            assertThat(response).isNull();

            verify(hearingManagementService)
                .getCaseSearchResultsPaginated(10, 0, caseReferences, requestedHearingStatus, CASE_TYPE);
            verify(accessControlService).verifyCaseAccess(CASE_REFERENCE, REQUIRED_ROLES, caseDetails);
            verify(hearingManagementService).getEmptyHearingsResponse(CASE_REFERENCE);
        }

        private HearingsForListOfCasesPaginatedRequest createRequest(List<String> caseReferences) {
            List<HearingsForListOfCasesPaginatedRequestCaseReference> requestCaseReferences = new ArrayList<>();

            caseReferences
                .forEach(caseReference -> requestCaseReferences
                    .add(new HearingsForListOfCasesPaginatedRequestCaseReference(caseReference)));

            return new HearingsForListOfCasesPaginatedRequest(10, 0, requestCaseReferences);
        }

        private GetHearingsResponse createEmptyGetHearingsResponse() {
            GetHearingsResponse response = new GetHearingsResponse();
            response.setCaseRef(CASE_REFERENCE);
            response.setCaseHearings(Collections.emptyList());

            return response;
        }

        private GetHearingsResponse createGetHearingsResponse() {
            CaseHearing caseHearing = new CaseHearing();
            caseHearing.setHearingId(2000000000L);

            GetHearingsResponse response = new GetHearingsResponse();
            response.setCaseRef(CASE_REFERENCE);
            response.setCaseHearings(List.of(caseHearing));

            return response;
        }

        private static Stream<Arguments> getHearingsStatusAndRoleAssignments() {
            return Stream.of(
                arguments(
                    HEARING_STATUS_AWAITING_LISTING,
                    List.of(ACCESS_ROLE_HEARING_MANAGER),
                    HEARING_STATUS_AWAITING_LISTING
                ),
                arguments(
                    HEARING_STATUS_AWAITING_LISTING,
                    List.of(ACCESS_ROLE_HEARING_MANAGER, ACCESS_ROLE_LISTED_HEARING_VIEWER),
                    HEARING_STATUS_AWAITING_LISTING
                ),
                arguments(
                    null,
                    List.of(ACCESS_ROLE_LISTED_HEARING_VIEWER),
                    HEARING_STATUS_LISTED
                ),
                arguments(
                    null,
                    Collections.emptyList(),
                    HEARING_STATUS_LISTED
                ),
                arguments(
                    HEARING_STATUS_LISTED,
                    List.of(ACCESS_ROLE_LISTED_HEARING_VIEWER),
                    HEARING_STATUS_LISTED
                ),
                arguments(
                    HEARING_STATUS_LISTED,
                    Collections.emptyList(),
                    HEARING_STATUS_LISTED
                )
            );
        }

        private static Stream<Arguments> emptyHearingsResponseStatusAndRoleAssignments() {
            return Stream.of(
                arguments(
                    HEARING_STATUS_AWAITING_LISTING,
                    List.of(ACCESS_ROLE_LISTED_HEARING_VIEWER)
                ),
                arguments(
                    HEARING_STATUS_AWAITING_LISTING,
                    Collections.emptyList())
            );
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
