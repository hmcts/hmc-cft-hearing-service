package uk.gov.hmcts.reform.hmc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingRepository;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.util.ArrayList;
import java.util.List;
import uk.gov.hmcts.reform.hmc.repository.CaseHearingRequestRepository;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HearingManagementServiceTest {

    private HearingManagementServiceImpl hearingManagementService;

    @Mock
    HearingRepository hearingRepository;

    @Mock
    CaseHearingRequestRepository caseHearingRequestRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        hearingManagementService = new HearingManagementServiceImpl(hearingRepository, caseHearingRequestRepository);
    }

    @Test
    void shouldFailWithInvalidHearingId() {
        HearingEntity hearing = new HearingEntity();
        hearing.setStatus("RESPONDED");
        hearing.setId(1L);

        Exception exception = assertThrows(HearingNotFoundException.class, () -> {
            hearingManagementService.getHearingRequest(1L, true);
        });
        assertEquals("No hearing found for reference: 1", exception.getMessage());
    }

    @Test
    void shouldPassWithValidHearingId() {
        HearingEntity hearing = new HearingEntity();
        hearing.setStatus("RESPONDED");
        hearing.setId(1L);
        when(hearingRepository.existsById(1L)).thenReturn(true);
        hearingManagementService.getHearingRequest(1L, true);
        verify(hearingRepository).existsById(1L);
    }

    @Test
    void shouldFailAsHearingWindowDetailsNotPresent() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.getHearingDetails().getHearingWindow().setHearingWindowStartDateRange(null);
        hearingRequest.getHearingDetails().getHearingWindow().setHearingWindowEndDateRange(null);
        hearingRequest.getHearingDetails().getHearingWindow().setFirstDateTimeMustBe(null);
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingManagementService.validateHearingRequest(hearingRequest);
        });
        assertEquals("Hearing window details are required", exception.getMessage());
    }

    @Test
    void shouldFailAsDetailsNotPresent() {
        HearingRequest hearingRequest = new HearingRequest();
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingManagementService.validateHearingRequest(hearingRequest);
        });
        assertEquals("Invalid details", exception.getMessage());
    }

    @Test
    void shouldPassWithHearing_Case_Request_Details_Valid() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        hearingManagementService.validateHearingRequest(hearingRequest);
    }

    @Test
    void shouldPassWithHearing_Case_Request_Party_Details_Valid() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        hearingRequest.setPartyDetails(TestingUtil.partyDetails());
        hearingRequest.getPartyDetails().get(0).setOrganisationDetails(TestingUtil.organisationDetails());
        hearingRequest.getPartyDetails().get(1).setIndividualDetails(TestingUtil.individualDetails());
        hearingManagementService.validateHearingRequest(hearingRequest);
    }

    @Test
    void shouldPassWithParty_Details_InValid_Org_Individual_details_Present() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        List<PartyDetails> partyDetails = TestingUtil.partyDetails();
        partyDetails.get(0).setIndividualDetails(TestingUtil.individualDetails());
        partyDetails.get(0).setOrganisationDetails(TestingUtil.organisationDetails());
        hearingRequest.setPartyDetails(partyDetails);
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingManagementService.validateHearingRequest(hearingRequest);
        });
        assertEquals("Either Individual or Organisation details should be present", exception.getMessage());
    }

    @Test
    void shouldPassWithParty_Details_InValid_Dow_details_Present() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        List<PartyDetails> partyDetails = TestingUtil.partyDetails();
        partyDetails.get(0).setIndividualDetails(TestingUtil.individualDetails());
        partyDetails.get(0).setUnavailabilityDow(new ArrayList<>());
        hearingRequest.setPartyDetails(partyDetails);
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingManagementService.validateHearingRequest(hearingRequest);
        });
        assertEquals("Unavailability DOW details should be present", exception.getMessage());
    }

    @Test
    void shouldPassWithParty_Details_InValid_UnavailabilityRange_details_Present() {
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setRequestDetails(TestingUtil.requestDetails());
        hearingRequest.setHearingDetails(TestingUtil.hearingDetails());
        hearingRequest.getHearingDetails().setPanelRequirements(TestingUtil.panelRequirements());
        hearingRequest.setCaseDetails(TestingUtil.caseDetails());
        List<PartyDetails> partyDetails = TestingUtil.partyDetails();
        partyDetails.get(0).setIndividualDetails(TestingUtil.individualDetails());
        partyDetails.get(0).setUnavailabilityRanges(new ArrayList<>());
        hearingRequest.setPartyDetails(partyDetails);
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingManagementService.validateHearingRequest(hearingRequest);
        });
        assertEquals("Unavailability range details should be present", exception.getMessage());

    }

    @Test
    void deleteHearingRequestShouldPassWithValidDetails() {
        when(caseHearingRequestRepository.getVersionNumber(2000000000L)).thenReturn(1);
        when(hearingRepository.existsById(2000000000L)).thenReturn(true);
        hearingManagementService.deleteHearingRequest(2000000000L, TestingUtil.deleteHearingRequest());
        verify(hearingRepository).existsById(2000000000L);
        verify(caseHearingRequestRepository).getVersionNumber(2000000000L);
    }

    @Test
    void testExpectedException_DeleteHearing_VersionNumber_Not_Equal_To_DB_VersionNumber() {
        when(caseHearingRequestRepository.getVersionNumber(2000000000L)).thenReturn(2);
        when(hearingRepository.existsById(2000000000L)).thenReturn(true);
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingManagementService.deleteHearingRequest(2000000000L, TestingUtil.deleteHearingRequest());
        });
        assertEquals("Invalid Version number", exception.getMessage());
    }

    @Test
    void testExpectedException_DeleteHearing_HearingId_NotPresent_inDB() {
        when(hearingRepository.existsById(2000000000L)).thenReturn(false);
        Exception exception = assertThrows(HearingNotFoundException.class, () -> {
            hearingManagementService.deleteHearingRequest(2000000000L, TestingUtil.deleteHearingRequest());
        });
        assertEquals("No hearing found for reference: 2000000000", exception.getMessage());
    }

    @Test
    void testExpectedException_DeleteHearing_HearingId_Null() {
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingManagementService.deleteHearingRequest(null, TestingUtil.deleteHearingRequest());
        });
        assertEquals("Invalid hearing Id", exception.getMessage());
    }

    @Test
    void testExpectedException_DeleteHearing_HearingId_Exceeds_MaxLength() {
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingManagementService.deleteHearingRequest(20000000001111L, TestingUtil.deleteHearingRequest());
        });
        assertEquals("Invalid hearing Id", exception.getMessage());
    }

    @Test
    void testExpectedException_DeleteHearing_HearingId_First_Char_Is_Not_2() {
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingManagementService.deleteHearingRequest(1000000100L, TestingUtil.deleteHearingRequest());
        });
        assertEquals("Invalid hearing Id", exception.getMessage());
    }
}
