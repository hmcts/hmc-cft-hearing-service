package uk.gov.hmcts.reform.hmc.controllers;

import com.microsoft.applicationinsights.core.dependencies.google.common.collect.Lists;
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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.TestIdamConfiguration;
import uk.gov.hmcts.reform.hmc.client.datastore.model.DataStoreCaseDetails;
import uk.gov.hmcts.reform.hmc.config.SecurityConfiguration;
import uk.gov.hmcts.reform.hmc.config.UrlManager;
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
import uk.gov.hmcts.reform.hmc.security.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.hmc.service.AccessControlService;
import uk.gov.hmcts.reform.hmc.service.HearingManagementService;
import uk.gov.hmcts.reform.hmc.service.common.OverrideAuditService;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.HEARING_VIEWER;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.LISTED_HEARING_VIEWER;

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

    @MockitoBean
    private HearingManagementService hearingManagementService;

    @MockitoBean
    private AccessControlService accessControlService;

    @MockitoBean
    private ApplicationParams applicationParams;

    @MockitoBean
    private UrlManager urlManager;

    @MockitoBean
    private OverrideAuditService overrideAuditService;

    @MockitoBean
    SecurityUtils securityUtils;

    private static final String CLIENT_S2S_TOKEN = "xui_webapp";

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        doReturn("xui_webapp").when(securityUtils)
            .getServiceNameFromS2SToken(any());
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
            when(hearingManagementService.saveHearingRequest(hearingRequest, null, CLIENT_S2S_TOKEN))
                .thenReturn(hearingResponse);

            HearingManagementController controller = new HearingManagementController(hearingManagementService,
                                                                                     accessControlService,
                                                                                     applicationParams,
                                                                                     securityUtils);
            controller.saveHearing(null, CLIENT_S2S_TOKEN, hearingRequest);
            verify(hearingManagementService, times(1)).saveHearingRequest(any(), any(), any());
        }

        @Test
        void shouldReturn400_whenCase_Details_Are_NotPresent() {
            // hearing request with no request details
            HearingRequest hearingRequest = generateHearingRequest(false);
            hearingRequest.setCaseDetails(null);

            HearingResponse hearingResponse = generateHearingResponse();
            when(hearingManagementService.saveHearingRequest(hearingRequest, null, CLIENT_S2S_TOKEN))
                .thenReturn(hearingResponse);

            HearingManagementController controller = new HearingManagementController(hearingManagementService,
                                                                                     accessControlService,
                                                                                     applicationParams,
                                                                                     securityUtils);
            controller.saveHearing(null, CLIENT_S2S_TOKEN,hearingRequest);
            verify(hearingManagementService, times(1)).saveHearingRequest(any(),any(), any());
        }

        @Test
        void shouldReturn201_whenHearingRequestData() {
            // hearing request - valid
            HearingRequest hearingRequest = generateHearingRequest(false);

            HearingResponse hearingResponse = generateHearingResponse();
            when(hearingManagementService.saveHearingRequest(hearingRequest, null, CLIENT_S2S_TOKEN))
                .thenReturn(hearingResponse);

            HearingManagementController controller = new HearingManagementController(hearingManagementService,
                                                                                     accessControlService,
                                                                                     applicationParams, securityUtils);
            controller.saveHearing(null, CLIENT_S2S_TOKEN, hearingRequest);
            verify(hearingManagementService, times(1)).saveHearingRequest(any(), any(), any());
        }


        @Test
        void shouldReturn201_whenHearingRequestDataWithOrgIdNull() {
            // hearing request - valid
            HearingRequest hearingRequest = generateHearingRequest(true);

            HearingResponse hearingResponse = generateHearingResponse();
            when(hearingManagementService.saveHearingRequest(hearingRequest, null, CLIENT_S2S_TOKEN))
                .thenReturn(hearingResponse);

            HearingManagementController controller = new HearingManagementController(hearingManagementService,
                                                                                     accessControlService,
                                                                                     applicationParams, securityUtils);
            controller.saveHearing(null, CLIENT_S2S_TOKEN, hearingRequest);
            verify(hearingManagementService, times(1)).saveHearingRequest(any(), any(), any());
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
            HearingManagementController controller = new HearingManagementController(hearingManagementService,
                                                                                     accessControlService,
                                                                                     applicationParams, securityUtils);
            controller.saveHearing(null, CLIENT_S2S_TOKEN, hearingRequest);
            InOrder orderVerifier = Mockito.inOrder(hearingManagementService);
            orderVerifier.verify(hearingManagementService).saveHearingRequest(hearingRequest, null,
                                                                              CLIENT_S2S_TOKEN);
            verifyNoMoreInteractions(hearingManagementService);
        }
    }

    @Nested
    @DisplayName("getHearing")
    class GetHearing {
        @Test
        void shouldReturn204_whenRequestIdIsValid() {
            HearingManagementController controller = new HearingManagementController(hearingManagementService,
                                                                                     accessControlService,
                                                                                     applicationParams,
                                                                                     securityUtils);
            controller.getHearing(1234L, true);
            verify(hearingManagementService, times(1)).getHearingRequest(any(), anyBoolean());
        }

        @Test
        void shouldReturn200_whenRequestIdIsValid() {
            HearingManagementController controller = new HearingManagementController(hearingManagementService,
                                                                                     accessControlService,
                                                                                     applicationParams,
                                                                                     securityUtils);
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
                .deleteHearingRequest(any(), any(), any());
            HearingManagementController controller = new HearingManagementController(hearingManagementService,
                                                                                     accessControlService,
                                                                                     applicationParams,
                                                                                     securityUtils);
            controller.deleteHearing(1234L, CLIENT_S2S_TOKEN, TestingUtil.deleteHearingRequest());
            verify(hearingManagementService, times(1)).deleteHearingRequest(any(), any(), any());
        }

        @Test
        void shouldReturn404_whenDeleteRequestIdIsInvalid() {
            DeleteHearingRequest request = TestingUtil.deleteHearingRequest();
            request.setCancellationReasonCodes(List.of(""));

            HearingResponse hearingResponse = generateHearingResponse();
            when(hearingManagementService.deleteHearingRequest(any(), any(), any())).thenReturn(hearingResponse);

            HearingManagementController controller = new HearingManagementController(hearingManagementService,
                                                                                     accessControlService,
                                                                                     applicationParams,
                                                                                     securityUtils);
            controller.deleteHearing(1234L, CLIENT_S2S_TOKEN, request);
            verify(hearingManagementService, times(1)).deleteHearingRequest(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("getHearings")
    class GetHearings {
        @Test
        void shouldReturnHearingRequest_WhenGetHearingsForValidCaseRefLuhn() {
            final String validCaseRef = "9372710950276233";
            doReturn(TestingUtil.getHearingsResponseWhenDataIsPresent(validCaseRef, "HEARING_REQUESTED"))
                .when(hearingManagementService)
                .getHearings(any(), any());
            HearingManagementController controller = new HearingManagementController(hearingManagementService,
                                                                                     accessControlService,
                                                                                     applicationParams,securityUtils);
            GetHearingsResponse hearingRequest = controller.getHearings(validCaseRef, null);
            verify(hearingManagementService, times(1)).getHearings(any(), any());
            assertThat(hearingRequest.getCaseRef()).isEqualTo(validCaseRef);
            assertThat(hearingRequest.getCaseHearings().getFirst().getHearingIsLinkedFlag()).isTrue();
        }

        @Test
        void shouldReturnHearingRequest_WhenGetHearingsForValidCaseRefAndListedStatus() {
            final String validCaseRef = "9372710950276233";
            doReturn(TestingUtil.getHearingsResponseWhenDataIsPresent(validCaseRef, "LISTED"))
                .when(hearingManagementService)
                .getHearings(any(), any());

            List<String> rolesRequired = Lists.newArrayList(HEARING_VIEWER, LISTED_HEARING_VIEWER);
            List<String> filteredRoleAssignments = Lists.newArrayList(LISTED_HEARING_VIEWER);

            doReturn(filteredRoleAssignments).when(accessControlService).verifyCaseAccess(validCaseRef, rolesRequired,
                                                                                          null);
            HearingManagementController controller = new HearingManagementController(hearingManagementService,
                                                                                     accessControlService,
                                                                                     applicationParams, securityUtils);
            GetHearingsResponse hearingRequest = controller.getHearings(validCaseRef, null);
            verify(hearingManagementService, times(1)).getHearings(validCaseRef, "LISTED");
            assertThat(hearingRequest.getCaseRef()).isEqualTo(validCaseRef);
            assertThat(hearingRequest.getCaseHearings().getFirst().getHmcStatus()).isEqualTo("LISTED");
            assertThat(hearingRequest.getCaseHearings().getFirst().getHearingIsLinkedFlag()).isTrue();
        }

        @Test
        void shouldReturnHearingRequest_WhenGetHearingsForValidCaseRefLuhnAndStatus() {
            final String validCaseRef = "9372710950276233";
            final String status = "UPDATED"; // for example
            doReturn(TestingUtil.getHearingsResponseWhenDataIsPresent(validCaseRef, "HEARING_REQUESTED"))
                .when(hearingManagementService)
                .getEmptyHearingsResponse(any());
            HearingManagementController controller = new HearingManagementController(hearingManagementService,
                                                                                     accessControlService,
                                                                                     applicationParams, securityUtils);
            GetHearingsResponse hearingRequest = controller.getHearings(validCaseRef, status);
            verify(hearingManagementService, times(1)).getEmptyHearingsResponse(validCaseRef);
            assertThat(hearingRequest.getCaseRef()).isEqualTo(validCaseRef);
            assertThat(hearingRequest.getCaseHearings().getFirst().getHearingIsLinkedFlag()).isTrue();
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
            when(hearingManagementService.updateHearingRequest(hearingId, hearingRequest, "",
                                                               CLIENT_S2S_TOKEN))
                    .thenReturn(hearingResponse);

            HearingManagementController controller = new HearingManagementController(hearingManagementService,
                                                                                     accessControlService,
                                                                                     applicationParams, securityUtils);
            controller.updateHearing("",hearingRequest,CLIENT_S2S_TOKEN,  hearingId);
            InOrder orderVerifier = Mockito.inOrder(hearingManagementService);
            orderVerifier.verify(hearingManagementService).updateHearingRequest(hearingId, hearingRequest,
                                                                                "", CLIENT_S2S_TOKEN);
            verifyNoMoreInteractions(hearingManagementService);
        }

        @Test
        void shouldCallUpdateHearingRequestWithPartyWithOrgIdNull() {
            final long hearingId = 2000000000L;
            UpdateHearingRequest hearingRequest = generateUpdateHearingRequest(true);
            HearingResponse hearingResponse = generateHearingResponse();
            when(hearingManagementService.updateHearingRequest(hearingId, hearingRequest, "",
                                                               CLIENT_S2S_TOKEN))
                .thenReturn(hearingResponse);

            HearingManagementController controller = new HearingManagementController(hearingManagementService,
                                                                                     accessControlService,
                                                                                     applicationParams, securityUtils);
            controller.updateHearing("", hearingRequest, CLIENT_S2S_TOKEN, hearingId);
            InOrder orderVerifier = Mockito.inOrder(hearingManagementService);
            orderVerifier.verify(hearingManagementService).updateHearingRequest(hearingId, hearingRequest,
                                                                                "", CLIENT_S2S_TOKEN);
            verifyNoMoreInteractions(hearingManagementService);
        }
    }

    @Nested
    @DisplayName("updateHearingCompletion")
    class UpdateHearingCompletion {

        @Test
        void shouldInvokeHearingCompletion() {
            final long hearingId = 2000000000L;
            HearingManagementController controller = new HearingManagementController(hearingManagementService,
                                                                                     accessControlService,
                                                                                     applicationParams, securityUtils);
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
            doReturn(TestingUtil.getHearingsResponseWhenDataIsPresent(ccdCaseRefs.getFirst(),
                                                                      HearingStatus.LISTED.name()))
                .when(hearingManagementService)
                .getHearings(any(), any());
            doReturn(cases).when(hearingManagementService).getCaseSearchResults(any(), any(), any());
            HearingManagementController controller = new HearingManagementController(
                hearingManagementService,
                accessControlService,
                applicationParams, securityUtils
            );
            List<GetHearingsResponse> hearingsResponseList = controller.getHearingsForListOfCases(ccdCaseRefs,
                                                               null,"AAT_PRIVATE");
            verify(hearingManagementService, times(1)).getHearings(any(), any());
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
            doReturn(cases).when(hearingManagementService).getCaseSearchResults(any(), any(), any());
            for (DataStoreCaseDetails dataStoreCaseDetails : cases) {
                doReturn(TestingUtil.getHearingsResponseWhenDataIsPresent(dataStoreCaseDetails.getId(), null))
                    .when(hearingManagementService)
                    .getHearings(any(), any());
            }
            HearingManagementController controller = new HearingManagementController(
                hearingManagementService,
                accessControlService,
                applicationParams, securityUtils
            );
            List<GetHearingsResponse> hearingsResponseList = controller.getHearingsForListOfCases(ccdCaseRefs,
                                                                                 null, "AAT_PRIVATE");
            verify(hearingManagementService, times(1)).getHearings(any(), any());
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
