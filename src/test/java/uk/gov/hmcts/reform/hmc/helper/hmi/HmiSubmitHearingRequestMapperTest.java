package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.CreateHearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.UpdateHearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.Entity;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiCaseDetails;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiHearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiSubmitHearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.Listing;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HmiSubmitHearingRequestMapperTest {

    private static final String PREFERRED_HEARING_CHANNEL = "PreferredHearingChannel";
    private static final Long HEARING_ID = 1L;

    @Mock
    private HmiCaseDetailsMapper hmiCaseDetailsMapper;

    @Mock
    private EntitiesMapper entitiesMapper;

    @Mock
    private ListingMapper listingMapper;

    @InjectMocks
    private HmiSubmitHearingRequestMapper hmiSubmitHearingRequestMapper;

    @Test
    void shouldReturnSubmitHearingRequestForCreateHearingRequest() {
        Entity entity = Entity.builder().build();
        CreateHearingRequest createHearingRequest = new CreateHearingRequest();
        CaseDetails caseDetails = new CaseDetails();
        createHearingRequest.setCaseDetails(caseDetails);
        HearingDetails hearingDetails = new HearingDetails();
        createHearingRequest.setHearingDetails(hearingDetails);
        PartyDetails partyDetails = new PartyDetails();
        createHearingRequest.setPartyDetails(Collections.singletonList(partyDetails));
        EntitiesMapperObject entities = EntitiesMapperObject.builder()
            .entities(Collections.singletonList(entity))
            .preferredHearingChannels(Collections.singletonList(PREFERRED_HEARING_CHANNEL))
            .build();
        when(entitiesMapper.getEntities(Collections.singletonList(partyDetails))).thenReturn(entities);
        HmiCaseDetails hmiCaseDetails = HmiCaseDetails.builder().build();
        when(hmiCaseDetailsMapper.getCaseDetails(caseDetails, HEARING_ID, Boolean.TRUE)).thenReturn(hmiCaseDetails);
        Listing listing = Listing.builder().build();
        when(listingMapper.getListing(hearingDetails, Collections.singletonList(PREFERRED_HEARING_CHANNEL)))
            .thenReturn(listing);
        HmiHearingRequest hmiHearingRequest = HmiHearingRequest.builder()
            .caseDetails(hmiCaseDetails)
            .listing(listing)
            .entities(Collections.singletonList(entity))
            .build();
        HmiSubmitHearingRequest expectedHmiSubmitHearingRequest = HmiSubmitHearingRequest.builder()
            .hearingRequest(hmiHearingRequest)
            .build();
        HmiSubmitHearingRequest actualHmiSubmitHearingRequest = hmiSubmitHearingRequestMapper
            .mapRequest(HEARING_ID, createHearingRequest);
        assertEquals(expectedHmiSubmitHearingRequest, actualHmiSubmitHearingRequest);
    }

    @Test
    void shouldReturnSubmitHearingRequestForUpdateHearingRequest() {
        Entity entity = Entity.builder().build();
        UpdateHearingRequest updateHearingRequest = new UpdateHearingRequest();
        CaseDetails caseDetails = new CaseDetails();
        updateHearingRequest.setCaseDetails(caseDetails);
        HearingDetails hearingDetails = new HearingDetails();
        updateHearingRequest.setHearingDetails(hearingDetails);
        PartyDetails partyDetails = new PartyDetails();
        updateHearingRequest.setPartyDetails(Collections.singletonList(partyDetails));
        EntitiesMapperObject entities = EntitiesMapperObject.builder()
            .entities(Collections.singletonList(entity))
            .preferredHearingChannels(Collections.singletonList(PREFERRED_HEARING_CHANNEL))
            .build();
        when(entitiesMapper.getEntities(Collections.singletonList(partyDetails))).thenReturn(entities);
        HmiCaseDetails hmiCaseDetails = HmiCaseDetails.builder().build();
        when(hmiCaseDetailsMapper.getCaseDetails(caseDetails, HEARING_ID, Boolean.FALSE)).thenReturn(hmiCaseDetails);
        Listing listing = Listing.builder().build();
        when(listingMapper.getListing(hearingDetails, Collections.singletonList(PREFERRED_HEARING_CHANNEL)))
            .thenReturn(listing);
        HmiHearingRequest hmiHearingRequest = HmiHearingRequest.builder()
            .caseDetails(hmiCaseDetails)
            .listing(listing)
            .entities(Collections.singletonList(entity))
            .build();
        HmiSubmitHearingRequest expectedHmiSubmitHearingRequest = HmiSubmitHearingRequest.builder()
            .hearingRequest(hmiHearingRequest)
            .build();
        HmiSubmitHearingRequest actualHmiSubmitHearingRequest = hmiSubmitHearingRequestMapper
            .mapRequest(HEARING_ID, updateHearingRequest);
        assertEquals(expectedHmiSubmitHearingRequest, actualHmiSubmitHearingRequest);
    }

}
