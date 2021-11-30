package uk.gov.hmcts.reform.hmc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingRepository;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.helper.HearingMapper;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.VERSION_NUMBER;

class HearingManagementServiceTest {

    private HearingManagementServiceImpl hearingManagementService;

    @Mock
    private HearingMapper hearingMapper;

    @Mock
    HearingRepository hearingRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        hearingManagementService = new HearingManagementServiceImpl(hearingRepository, hearingMapper);
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
        HearingEntity hearingEntity = new HearingEntity();
        given(hearingRepository.save(hearingEntity)).willReturn(TestingUtil.hearingEntity());
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingManagementService.saveHearingRequest(hearingRequest);
        });
        assertEquals("Hearing window details are required", exception.getMessage());
    }

    @Test
    void shouldFailAsDetailsNotPresent() {
        HearingRequest hearingRequest = new HearingRequest();
        Exception exception = assertThrows(BadRequestException.class, () -> {
            hearingManagementService.saveHearingRequest(hearingRequest);
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
        given(hearingMapper.modelToEntity(hearingRequest)).willReturn(TestingUtil.hearingEntity());
        given(hearingRepository.save(TestingUtil.hearingEntity())).willReturn(TestingUtil.hearingEntity());
        given(hearingRepository.findById(1L)).willReturn(Optional.of(TestingUtil.hearingEntity()));
        HearingResponse response = hearingManagementService.saveHearingRequest(hearingRequest);
        assertEquals(VERSION_NUMBER,response.getVersionNumber());
        assertEquals(HEARING_STATUS, response.getStatus());
        assertNotNull(response.getHearingRequestId());
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
        given(hearingMapper.modelToEntity(hearingRequest)).willReturn(TestingUtil.hearingEntity());
        given(hearingRepository.save(TestingUtil.hearingEntity())).willReturn(TestingUtil.hearingEntity());
        given(hearingRepository.findById(1L)).willReturn(Optional.of(TestingUtil.hearingEntity()));
        HearingResponse response = hearingManagementService.saveHearingRequest(hearingRequest);
        assertEquals(VERSION_NUMBER,response.getVersionNumber());
        assertEquals(HEARING_STATUS, response.getStatus());
        assertNotNull(response.getHearingRequestId());

    }

    @Test
    void shouldFailWithParty_Details_InValid_Org_Individual_details_Present() {
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
            hearingManagementService.saveHearingRequest(hearingRequest);
        });
        assertEquals("Either Individual or Organisation details should be present", exception.getMessage());
    }

    @Test
    void shouldFailWithParty_Details_InValid_Dow_details_Present() {
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
            hearingManagementService.saveHearingRequest(hearingRequest);
        });
        assertEquals("Unavailability DOW details should be present", exception.getMessage());
    }

    @Test
    void shouldFailWithParty_Details_InValid_UnavailabilityRange_details_Present() {
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
            hearingManagementService.saveHearingRequest(hearingRequest);
        });
        assertEquals("Unavailability range details should be present", exception.getMessage());

    }
}
