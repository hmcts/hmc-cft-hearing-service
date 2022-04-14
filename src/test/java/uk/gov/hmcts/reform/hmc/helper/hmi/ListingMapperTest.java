package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
import uk.gov.hmcts.reform.hmc.model.HearingWindow;
import uk.gov.hmcts.reform.hmc.model.PanelPreference;
import uk.gov.hmcts.reform.hmc.model.PanelRequirements;
import uk.gov.hmcts.reform.hmc.model.hmi.Listing;
import uk.gov.hmcts.reform.hmc.model.hmi.ListingJoh;
import uk.gov.hmcts.reform.hmc.model.hmi.ListingLocation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.constants.Constants.COURT;
import static uk.gov.hmcts.reform.hmc.constants.Constants.EPIMS;

@ExtendWith(MockitoExtension.class)
class ListingMapperTest {

    @Mock
    private ListingJohsMapper listingJohsMapper;

    @Mock
    private ListingLocationsMapper listingLocationsMapper;

    @Mock
    private ListingOtherConsiderationsMapper listingOtherConsiderationsMapper;

    @InjectMocks
    private ListingMapper listingMapper;

    private static final String HEARING_PRIORITY_TYPE = "HearingPriorityType";
    private static final String HEARING_TYPE = "HearingType";
    private static final String LISTING_COMMENTS = "ListingComments";
    private static final String HEARING_REQUESTER = "HearingRequester";
    private static final String ROLE_TYPE = "RoleType";
    private static final String HEARING_CHANNEL = "Email";
    private static final String AMEND_REASON_CODE = "code";

    @Test
    void shouldReturnListingWithBothHearingWindowFieldsAndRoleType() {
        LocalDateTime localDateTime = LocalDateTime.now();
        HearingWindow hearingWindow = new HearingWindow();
        hearingWindow.setFirstDateTimeMustBe(localDateTime);
        hearingWindow.setDateRangeStart(localDateTime.minusDays(1).toLocalDate());
        hearingWindow.setDateRangeEnd(localDateTime.plusDays(1).toLocalDate());
        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutoListFlag(true);
        hearingDetails.setHearingPriorityType(HEARING_PRIORITY_TYPE);
        hearingDetails.setHearingType(HEARING_TYPE);
        hearingDetails.setDuration(150);
        hearingDetails.setHearingWindow(hearingWindow);
        hearingDetails.setNumberOfPhysicalAttendees(2);
        hearingDetails.setListingComments(LISTING_COMMENTS);
        hearingDetails.setHearingRequester(HEARING_REQUESTER);
        hearingDetails.setAmendReasonCode(AMEND_REASON_CODE);
        hearingDetails.setPrivateHearingRequiredFlag(false);
        PanelRequirements panelRequirements = new PanelRequirements();
        PanelPreference panelPreference = new PanelPreference();
        panelRequirements.setPanelPreferences(Collections.singletonList(panelPreference));
        panelRequirements.setRoleType(Collections.singletonList(ROLE_TYPE));
        hearingDetails.setPanelRequirements(panelRequirements);
        ListingLocation listingLocation = ListingLocation.builder().build();
        listingLocation.setLocationId("court Id");
        listingLocation.setLocationType(COURT);
        listingLocation.setLocationReferenceType(EPIMS);
        when(listingLocationsMapper.getListingLocations(any())).thenReturn(Collections.singletonList(listingLocation));
        HearingLocation hearingLocation = new HearingLocation();
        hearingDetails.setHearingLocations(Collections.singletonList(hearingLocation));
        ListingJoh listingJoh = ListingJoh.builder().build();

        Boolean hearingInWelsh = Boolean.TRUE;
        String facilityType1 = "consideration 1";
        String facilityType2 = "consideration 2";
        List<String> facilityTypes = new ArrayList<>();
        facilityTypes.add(facilityType1);
        facilityTypes.add(facilityType2);
        List<String> otherConsiderations = generateOtherConsiderations(hearingInWelsh, facilityTypes);

        when(listingJohsMapper.getListingJohs(any())).thenReturn(Collections.singletonList(listingJoh));
        when(listingOtherConsiderationsMapper.getListingOtherConsiderations(any(), any()))
                .thenReturn(otherConsiderations);

        Listing listing = listingMapper.getListing(hearingDetails, Collections.singletonList(HEARING_CHANNEL));

        assertEquals(true, listing.getListingAutoCreateFlag());
        assertEquals(HEARING_PRIORITY_TYPE, listing.getListingPriority());
        assertEquals(HEARING_TYPE, listing.getListingType());
        assertEquals(150, listing.getListingDuration());
        assertEquals(localDateTime, listing.getListingDate());
        assertEquals(2, listing.getListingNumberAttendees());
        assertEquals(LISTING_COMMENTS, listing.getListingComments());
        assertEquals(HEARING_REQUESTER, listing.getListingRequestedBy());
        assertEquals(false, listing.getListingPrivateFlag());

        assertEquals(1, listing.getListingJohs().size());
        assertEquals(listingJoh, listing.getListingJohs().get(0));

        assertEquals(3, listing.getListingOtherConsiderations().size());
        assertTrue(listing.getListingOtherConsiderations().contains(hearingInWelsh.toString()));
        assertTrue(listing.getListingOtherConsiderations().contains(facilityType1));
        assertTrue(listing.getListingOtherConsiderations().contains(facilityType2));

        assertEquals(1, listing.getListingHearingChannels().size());
        assertEquals(AMEND_REASON_CODE, listing.getAmendReasonCode());
        assertEquals(HEARING_CHANNEL, listing.getListingHearingChannels().get(0));
        assertEquals(1, listing.getListingLocations().size());
        assertEquals(listingLocation, listing.getListingLocations().get(0));
        assertEquals(localDateTime.minusDays(1).toLocalDate(), listing.getListingStartDate());
        assertEquals(localDateTime.plusDays(1).toLocalDate(), listing.getListingEndDate());
        assertEquals(1, listing.getListingJohTiers().size());
        assertEquals(ROLE_TYPE, listing.getListingJohTiers().get(0));
        assertEquals("court Id", listing.getListingLocations().get(0).getLocationId());
        assertEquals(EPIMS, listing.getListingLocations().get(0).getLocationReferenceType());
        assertEquals(COURT, listing.getListingLocations().get(0).getLocationType());
    }

    private List<String> generateOtherConsiderations(Boolean hearingInWelsh,
                                                     List<String> facilityTypes) {
        List<String> otherConsiderations = new ArrayList<>();
        otherConsiderations.add(hearingInWelsh.toString());
        facilityTypes.forEach(e -> otherConsiderations.add(e));
        return otherConsiderations;
    }

    @Test
    void shouldReturnListingWithHearingWindowFieldsAndRoleTypeNull() {
        LocalDateTime localDateTime = LocalDateTime.now();
        HearingWindow hearingWindow = new HearingWindow();
        hearingWindow.setFirstDateTimeMustBe(localDateTime);
        hearingWindow.setDateRangeStart(null);
        hearingWindow.setDateRangeEnd(null);
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
        HearingWindow hearingWindow = new HearingWindow();
        hearingWindow.setFirstDateTimeMustBe(localDateTime);
        hearingWindow.setDateRangeStart(null);
        hearingWindow.setDateRangeEnd(null);
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

    @Test
    void shouldReturnListingForMultiDayHearingDurationGreaterThan360() {
        LocalDateTime localDateTime = LocalDateTime.now();
        HearingWindow hearingWindow = new HearingWindow();
        hearingWindow.setFirstDateTimeMustBe(localDateTime);
        hearingWindow.setDateRangeStart(localDateTime.minusDays(1).toLocalDate());
        hearingWindow.setDateRangeEnd(localDateTime.plusDays(1).toLocalDate());
        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutoListFlag(true);
        hearingDetails.setHearingPriorityType(HEARING_PRIORITY_TYPE);
        hearingDetails.setHearingType(HEARING_TYPE);
        hearingDetails.setDuration(400);
        hearingDetails.setHearingWindow(hearingWindow);
        hearingDetails.setNumberOfPhysicalAttendees(2);
        hearingDetails.setListingComments(LISTING_COMMENTS);
        hearingDetails.setHearingRequester(HEARING_REQUESTER);
        hearingDetails.setAmendReasonCode(AMEND_REASON_CODE);
        hearingDetails.setPrivateHearingRequiredFlag(false);
        PanelRequirements panelRequirements = new PanelRequirements();
        PanelPreference panelPreference = new PanelPreference();
        panelRequirements.setPanelPreferences(Collections.singletonList(panelPreference));
        panelRequirements.setRoleType(Collections.singletonList(ROLE_TYPE));
        hearingDetails.setPanelRequirements(panelRequirements);
        ListingLocation listingLocation = ListingLocation.builder().build();
        listingLocation.setLocationId("court Id");
        listingLocation.setLocationType(COURT);
        listingLocation.setLocationReferenceType(EPIMS);
        when(listingLocationsMapper.getListingLocations(any())).thenReturn(Collections.singletonList(listingLocation));
        HearingLocation hearingLocation = new HearingLocation();
        hearingDetails.setHearingLocations(Collections.singletonList(hearingLocation));
        ListingJoh listingJoh = ListingJoh.builder().build();

        Boolean hearingInWelsh = Boolean.TRUE;
        String facilityType1 = "consideration 1";
        String facilityType2 = "consideration 2";
        List<String> facilityTypes = new ArrayList<>();
        facilityTypes.add(facilityType1);
        facilityTypes.add(facilityType2);
        List<String> otherConsiderations = generateOtherConsiderations(hearingInWelsh, facilityTypes);

        when(listingJohsMapper.getListingJohs(any())).thenReturn(Collections.singletonList(listingJoh));
        when(listingOtherConsiderationsMapper.getListingOtherConsiderations(any(), any()))
            .thenReturn(otherConsiderations);

        Listing listing = listingMapper.getListing(hearingDetails, Collections.singletonList(HEARING_CHANNEL));

        assertEquals(true, listing.getListingAutoCreateFlag());
        assertEquals(HEARING_PRIORITY_TYPE, listing.getListingPriority());
        assertEquals(HEARING_TYPE, listing.getListingType());
        assertEquals(360, listing.getListingDuration());
        assertEquals(localDateTime, listing.getListingDate());
        assertEquals(2, listing.getListingNumberAttendees());
        assertEquals(LISTING_COMMENTS, listing.getListingComments());
        assertEquals(HEARING_REQUESTER, listing.getListingRequestedBy());
        assertEquals(false, listing.getListingPrivateFlag());

        assertEquals(1, listing.getListingJohs().size());
        assertEquals(listingJoh, listing.getListingJohs().get(0));

        assertEquals(3, listing.getListingOtherConsiderations().size());
        assertTrue(listing.getListingOtherConsiderations().contains(hearingInWelsh.toString()));
        assertTrue(listing.getListingOtherConsiderations().contains(facilityType1));
        assertTrue(listing.getListingOtherConsiderations().contains(facilityType2));

        assertEquals(1, listing.getListingHearingChannels().size());
        assertEquals(AMEND_REASON_CODE, listing.getAmendReasonCode());
        assertEquals(HEARING_CHANNEL, listing.getListingHearingChannels().get(0));
        assertEquals(1, listing.getListingLocations().size());
        assertEquals(listingLocation, listing.getListingLocations().get(0));
        assertEquals(localDateTime.minusDays(1).toLocalDate(), listing.getListingStartDate());
        assertEquals(localDateTime.plusDays(1).toLocalDate(), listing.getListingEndDate());
        assertEquals(1, listing.getListingJohTiers().size());
        assertEquals(ROLE_TYPE, listing.getListingJohTiers().get(0));
        assertEquals("court Id", listing.getListingLocations().get(0).getLocationId());
        assertEquals(EPIMS, listing.getListingLocations().get(0).getLocationReferenceType());
        assertEquals(COURT, listing.getListingLocations().get(0).getLocationType());
    }
}
