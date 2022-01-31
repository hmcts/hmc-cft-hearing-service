package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
import uk.gov.hmcts.reform.hmc.model.HearingWindow;
import uk.gov.hmcts.reform.hmc.model.HearingWindowDateRange;
import uk.gov.hmcts.reform.hmc.model.HearingWindowFirstDate;
import uk.gov.hmcts.reform.hmc.model.PanelPreference;
import uk.gov.hmcts.reform.hmc.model.PanelRequirements;
import uk.gov.hmcts.reform.hmc.model.hmi.Listing;
import uk.gov.hmcts.reform.hmc.model.hmi.ListingJoh;
import uk.gov.hmcts.reform.hmc.model.hmi.ListingLocation;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListingMapperTest {

    @Mock
    private ListingJohsMapper listingJohsMapper;

    @Mock
    private ListingLocationsMapper listingLocationsMapper;

    @InjectMocks
    private ListingMapper listingMapper;

    private static final String HEARING_PRIORITY_TYPE = "HearingPriorityType";
    private static final String HEARING_TYPE = "HearingType";
    private static final String LISTING_COMMENTS = "ListingComments";
    private static final String HEARING_REQUESTER = "HearingRequester";
    private static final String ROLE_TYPE = "RoleType";
    private static final String HEARING_CHANNEL = "Email";

    @Test
    void shouldReturnListingWithBothHearingWindowFieldsAndRoleType() {
        LocalDateTime localDateTime = LocalDateTime.now();
        HearingWindow hearingWindow = generateHearingWindow(localDateTime);
        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutoListFlag(true);
        hearingDetails.setHearingPriorityType(HEARING_PRIORITY_TYPE);
        hearingDetails.setHearingType(HEARING_TYPE);
        hearingDetails.setDuration(1);
        hearingDetails.setHearingWindow(hearingWindow);
        hearingDetails.setNumberOfPhysicalAttendees(2);
        hearingDetails.setListingComments(LISTING_COMMENTS);
        hearingDetails.setHearingRequester(HEARING_REQUESTER);
        hearingDetails.setPrivateHearingRequiredFlag(false);
        PanelRequirements panelRequirements = new PanelRequirements();
        PanelPreference panelPreference = new PanelPreference();
        panelRequirements.setPanelPreferences(Collections.singletonList(panelPreference));
        panelRequirements.setRoleType(Collections.singletonList(ROLE_TYPE));
        hearingDetails.setPanelRequirements(panelRequirements);
        ListingLocation listingLocation = ListingLocation.builder().build();
        when(listingLocationsMapper.getListingLocations(any())).thenReturn(Collections.singletonList(listingLocation));
        HearingLocation hearingLocation = new HearingLocation();
        hearingDetails.setHearingLocations(Collections.singletonList(hearingLocation));
        ListingJoh listingJoh = ListingJoh.builder().build();
        when(listingJohsMapper.getListingJohs(any())).thenReturn(Collections.singletonList(listingJoh));

        Listing listing = listingMapper.getListing(hearingDetails, Collections.singletonList(HEARING_CHANNEL));

        assertEquals(true, listing.getListingAutoCreateFlag());
        assertEquals(HEARING_PRIORITY_TYPE, listing.getListingPriority());
        assertEquals(HEARING_TYPE, listing.getListingType());
        assertEquals(1, listing.getListingDuration());
        assertEquals(localDateTime, listing.getListingDate());
        assertEquals(2, listing.getListingNumberAttendees());
        assertEquals(LISTING_COMMENTS, listing.getListingComments());
        assertEquals(HEARING_REQUESTER, listing.getListingRequestedBy());
        assertEquals(false, listing.getListingPrivateFlag());
        assertEquals(1, listing.getListingJohs().size());
        assertEquals(listingJoh, listing.getListingJohs().get(0));
        assertEquals(1, listing.getListingHearingChannels().size());
        assertEquals(HEARING_CHANNEL, listing.getListingHearingChannels().get(0));
        assertEquals(1, listing.getListingLocations().size());
        assertEquals(listingLocation, listing.getListingLocations().get(0));
        assertEquals(localDateTime.minusDays(1).toLocalDate(), listing.getListingStartDate());
        assertEquals(localDateTime.plusDays(1).toLocalDate(), listing.getListingEndDate());
        assertEquals(1, listing.getListingJohTiers().size());
        assertEquals(ROLE_TYPE, listing.getListingJohTiers().get(0));
    }

    @Test
    void shouldReturnListingWithHearingWindowFieldsAndRoleTypeNull() {
        LocalDateTime localDateTime = LocalDateTime.now();
        HearingWindow hearingWindow = generateHearingWindow(localDateTime);
        hearingWindow.getHearingWindowDateRange().setHearingWindowStartDateRange(null);
        hearingWindow.getHearingWindowDateRange().setHearingWindowEndDateRange(null);
        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutoListFlag(true);
        hearingDetails.setHearingPriorityType(HEARING_PRIORITY_TYPE);
        hearingDetails.setHearingType(HEARING_TYPE);
        hearingDetails.setDuration(1);
        hearingDetails.setHearingWindow(hearingWindow);
        hearingDetails.setNumberOfPhysicalAttendees(2);
        hearingDetails.setListingComments(LISTING_COMMENTS);
        hearingDetails.setHearingRequester(HEARING_REQUESTER);
        hearingDetails.setPrivateHearingRequiredFlag(false);
        PanelRequirements panelRequirements = new PanelRequirements();
        PanelPreference panelPreference = new PanelPreference();
        panelRequirements.setPanelPreferences(Collections.singletonList(panelPreference));
        panelRequirements.setRoleType(null);
        hearingDetails.setPanelRequirements(panelRequirements);
        ListingLocation listingLocation = ListingLocation.builder().build();
        when(listingLocationsMapper.getListingLocations(any())).thenReturn(Collections.singletonList(listingLocation));
        HearingLocation hearingLocation = new HearingLocation();
        hearingDetails.setHearingLocations(Collections.singletonList(hearingLocation));
        ListingJoh listingJoh = ListingJoh.builder().build();
        when(listingJohsMapper.getListingJohs(any())).thenReturn(Collections.singletonList(listingJoh));

        Listing listing = listingMapper.getListing(hearingDetails, Collections.singletonList(HEARING_CHANNEL));

        assertEquals(true, listing.getListingAutoCreateFlag());
        assertEquals(HEARING_PRIORITY_TYPE, listing.getListingPriority());
        assertEquals(HEARING_TYPE, listing.getListingType());
        assertEquals(1, listing.getListingDuration());
        assertEquals(localDateTime, listing.getListingDate());
        assertEquals(2, listing.getListingNumberAttendees());
        assertEquals(LISTING_COMMENTS, listing.getListingComments());
        assertEquals(HEARING_REQUESTER, listing.getListingRequestedBy());
        assertEquals(false, listing.getListingPrivateFlag());
        assertEquals(1, listing.getListingJohs().size());
        assertEquals(listingJoh, listing.getListingJohs().get(0));
        assertEquals(1, listing.getListingHearingChannels().size());
        assertEquals(HEARING_CHANNEL, listing.getListingHearingChannels().get(0));
        assertEquals(1, listing.getListingLocations().size());
        assertEquals(listingLocation, listing.getListingLocations().get(0));
        assertNull(listing.getListingStartDate());
        assertNull(listing.getListingEndDate());
        assertNull(listing.getListingJohTiers());
    }

    @Test
    void shouldReturnListingWithNoRoleTypeWhenEmpty() {
        LocalDateTime localDateTime = LocalDateTime.now();
        HearingWindow hearingWindow = generateHearingWindow(localDateTime);
        hearingWindow.getHearingWindowDateRange().setHearingWindowStartDateRange(null);
        hearingWindow.getHearingWindowDateRange().setHearingWindowEndDateRange(null);
        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutoListFlag(true);
        hearingDetails.setHearingPriorityType(HEARING_PRIORITY_TYPE);
        hearingDetails.setHearingType(HEARING_TYPE);
        hearingDetails.setDuration(1);
        hearingDetails.setHearingWindow(hearingWindow);
        hearingDetails.setNumberOfPhysicalAttendees(2);
        hearingDetails.setListingComments(LISTING_COMMENTS);
        hearingDetails.setHearingRequester(HEARING_REQUESTER);
        hearingDetails.setPrivateHearingRequiredFlag(false);
        PanelRequirements panelRequirements = new PanelRequirements();
        PanelPreference panelPreference = new PanelPreference();
        panelRequirements.setPanelPreferences(Collections.singletonList(panelPreference));
        hearingDetails.setPanelRequirements(panelRequirements);
        panelRequirements.setRoleType(Collections.emptyList());
        ListingLocation listingLocation = ListingLocation.builder().build();
        when(listingLocationsMapper.getListingLocations(any())).thenReturn(Collections.singletonList(listingLocation));
        HearingLocation hearingLocation = new HearingLocation();
        hearingDetails.setHearingLocations(Collections.singletonList(hearingLocation));
        ListingJoh listingJoh = ListingJoh.builder().build();
        when(listingJohsMapper.getListingJohs(any())).thenReturn(Collections.singletonList(listingJoh));

        Listing listing = listingMapper.getListing(hearingDetails, Collections.singletonList(HEARING_CHANNEL));

        assertEquals(true, listing.getListingAutoCreateFlag());
        assertEquals(HEARING_PRIORITY_TYPE, listing.getListingPriority());
        assertEquals(HEARING_TYPE, listing.getListingType());
        assertEquals(1, listing.getListingDuration());
        assertEquals(localDateTime, listing.getListingDate());
        assertEquals(2, listing.getListingNumberAttendees());
        assertEquals(LISTING_COMMENTS, listing.getListingComments());
        assertEquals(HEARING_REQUESTER, listing.getListingRequestedBy());
        assertEquals(false, listing.getListingPrivateFlag());
        assertEquals(1, listing.getListingJohs().size());
        assertEquals(listingJoh, listing.getListingJohs().get(0));
        assertEquals(1, listing.getListingHearingChannels().size());
        assertEquals(HEARING_CHANNEL, listing.getListingHearingChannels().get(0));
        assertEquals(1, listing.getListingLocations().size());
        assertEquals(listingLocation, listing.getListingLocations().get(0));
        assertNull(listing.getListingStartDate());
        assertNull(listing.getListingEndDate());
        assertNull(listing.getListingJohTiers());
    }

    private HearingWindow generateHearingWindow(LocalDateTime localDateTime) {
        HearingWindow hearingWindow = new HearingWindow();
        HearingWindowFirstDate hearingWindowFirstDate = new HearingWindowFirstDate();
        hearingWindowFirstDate.setFirstDateTimeMustBe(localDateTime);
        hearingWindow.setHearingWindowFirstDate(hearingWindowFirstDate);
        HearingWindowDateRange hearingWindowDateRange = new HearingWindowDateRange();
        hearingWindowDateRange.setHearingWindowStartDateRange(localDateTime.minusDays(1).toLocalDate());
        hearingWindowDateRange.setHearingWindowEndDateRange(localDateTime.plusDays(1).toLocalDate());
        hearingWindow.setHearingWindowDateRange(hearingWindowDateRange);
        return hearingWindow;
    }

}
