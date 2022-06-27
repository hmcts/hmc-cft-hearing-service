package uk.gov.hmcts.reform.hmc.helper.hmi;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.helper.HearingMapper;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingWindow;
import uk.gov.hmcts.reform.hmc.model.PanelPreference;
import uk.gov.hmcts.reform.hmc.model.PanelRequirements;
import uk.gov.hmcts.reform.hmc.model.hmi.Listing;
import uk.gov.hmcts.reform.hmc.model.hmi.ListingJoh;
import uk.gov.hmcts.reform.hmc.model.hmi.ListingLocation;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

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
import static uk.gov.hmcts.reform.hmc.constants.Constants.DURATION_OF_DAY;
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

    private static final String HEARING_PRIORITY_TYPE = "Priority type";
    private static final String HEARING_TYPE = "Some hearing type";
    private static final String LISTING_COMMENTS = "Some listing comments";
    private static final String HEARING_REQUESTER = "Some judge";
    private static final String ROLE_TYPE = "RoleType1";
    private static final String HEARING_CHANNEL = "someChannelType";
    private static final String AMEND_REASON_CODE = "reason";
    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.now();
    private static final Boolean hearingInWelsh = Boolean.TRUE;
    private static final String FACILITY_TYPE_1 = "consideration 1";
    private static final String FACILITY_TYPE_2 = "consideration 2";

    @Test
    void shouldReturnListingWithBothHearingWindowFieldsAndRoleType() {
        ListingJoh listingJoh = ListingJoh.builder().build();
        List<String> facilityTypes = buildFacilityTypes();
        ListingLocation listingLocation = generateListingLocation();
        generateOtherConsiderations(facilityTypes,listingJoh);
        HearingDetails hearingDetails = buildHearingDetails(150);
        Listing listing = listingMapper.getListing(hearingDetails);

        assertEquals(listingLocation, listing.getListingLocations().get(0));
        assertEquals(true, listing.getListingAutoCreateFlag());
        assertEquals(HEARING_PRIORITY_TYPE, listing.getListingPriority());
        assertEquals(HEARING_TYPE, listing.getListingType());
        assertEquals(150, listing.getListingDuration());
        assertNull(listing.getListingMultiDay());
        assertEquals(LOCAL_DATE_TIME, listing.getListingDate());
        assertEquals(4, listing.getListingNumberAttendees());
        assertEquals(LISTING_COMMENTS, listing.getListingComments());
        assertEquals(HEARING_REQUESTER, listing.getListingRequestedBy());
        assertEquals(false, listing.getListingPrivateFlag());

        assertEquals(1, listing.getListingJohs().size());
        assertEquals(listingJoh, listing.getListingJohs().get(0));

        assertEquals(3, listing.getListingOtherConsiderations().size());
        assertTrue(listing.getListingOtherConsiderations().contains(hearingInWelsh.toString()));
        assertTrue(listing.getListingOtherConsiderations().contains(FACILITY_TYPE_1));
        assertTrue(listing.getListingOtherConsiderations().contains(FACILITY_TYPE_2));

        assertEquals(2, listing.getListingHearingChannels().size());
        assertEquals(AMEND_REASON_CODE, listing.getAmendReasonCode());
        assertEquals(HEARING_CHANNEL, listing.getListingHearingChannels().get(0));
        assertEquals(1, listing.getListingLocations().size());
        assertEquals(LOCAL_DATE_TIME.minusDays(1).toLocalDate(), listing.getListingStartDate());
        assertEquals(LOCAL_DATE_TIME.plusDays(1).toLocalDate(), listing.getListingEndDate());
        assertEquals(2, listing.getListingJohTiers().size());
        assertEquals(ROLE_TYPE, listing.getListingJohTiers().get(0));
        assertEquals("court Id", listing.getListingLocations().get(0).getLocationId());
        assertEquals(EPIMS, listing.getListingLocations().get(0).getLocationReferenceType());
        assertEquals(COURT, listing.getListingLocations().get(0).getLocationType());
    }

    @Test
    void shouldReturnListingWithHearingWindowFieldsAndRoleTypeNull() {
        ListingJoh listingJoh = ListingJoh.builder().build();
        List<String> facilityTypes = buildFacilityTypes();
        generateOtherConsiderations(facilityTypes,listingJoh);

        HearingWindow hearingWindow = new HearingWindow();
        hearingWindow.setFirstDateTimeMustBe(LOCAL_DATE_TIME);
        hearingWindow.setDateRangeStart(null);
        hearingWindow.setDateRangeEnd(null);
        PanelRequirements panelRequirements = new PanelRequirements();
        PanelPreference panelPreference = new PanelPreference();
        panelRequirements.setPanelPreferences(Collections.singletonList(panelPreference));
        panelRequirements.setRoleType(null);

        HearingDetails hearingDetails = buildHearingDetails(DURATION_OF_DAY);
        hearingDetails.setPanelRequirements(panelRequirements);
        hearingDetails.setPanelRequirements(panelRequirements);
        hearingDetails.setHearingWindow(hearingWindow);
        ListingLocation listingLocation = generateListingLocation();
        Listing listing = listingMapper.getListing(hearingDetails);

        assertEquals(listingLocation, listing.getListingLocations().get(0));
        assertEquals(true, listing.getListingAutoCreateFlag());
        assertEquals(HEARING_PRIORITY_TYPE, listing.getListingPriority());
        assertEquals(HEARING_TYPE, listing.getListingType());
        assertEquals(360, listing.getListingDuration());
        assertNull(listing.getListingMultiDay());
        assertEquals(LOCAL_DATE_TIME, listing.getListingDate());
        assertEquals(4, listing.getListingNumberAttendees());
        assertEquals(LISTING_COMMENTS, listing.getListingComments());
        assertEquals(HEARING_REQUESTER, listing.getListingRequestedBy());
        assertEquals(false, listing.getListingPrivateFlag());
        assertEquals(1, listing.getListingJohs().size());
        assertEquals(listingJoh, listing.getListingJohs().get(0));
        assertEquals(2, listing.getListingHearingChannels().size());
        assertTrue(listing.getListingHearingChannels().contains(HEARING_CHANNEL));
        assertEquals(1, listing.getListingLocations().size());
        assertNull(listing.getListingStartDate());
        assertNull(listing.getListingEndDate());
        assertNull(listing.getListingJohTiers());
    }

    @Test
    void shouldReturnListingWithNoRoleTypeWhenEmpty() {
        ListingJoh listingJoh = ListingJoh.builder().build();
        List<String> facilityTypes = buildFacilityTypes();
        generateOtherConsiderations(facilityTypes,listingJoh);

        HearingWindow hearingWindow = new HearingWindow();
        hearingWindow.setFirstDateTimeMustBe(LOCAL_DATE_TIME);
        hearingWindow.setDateRangeStart(null);
        hearingWindow.setDateRangeEnd(null);
        PanelRequirements panelRequirements = new PanelRequirements();
        PanelPreference panelPreference = new PanelPreference();
        panelRequirements.setPanelPreferences(Collections.singletonList(panelPreference));
        panelRequirements.setRoleType(null);

        HearingDetails hearingDetails = buildHearingDetails(365);
        hearingDetails.setPanelRequirements(panelRequirements);
        hearingDetails.setPanelRequirements(panelRequirements);
        hearingDetails.setHearingWindow(hearingWindow);
        ListingLocation listingLocation = generateListingLocation();
        Listing listing = listingMapper.getListing(hearingDetails);

        assertEquals(listingLocation, listing.getListingLocations().get(0));
        assertEquals(true, listing.getListingAutoCreateFlag());
        assertEquals(HEARING_PRIORITY_TYPE, listing.getListingPriority());
        assertEquals(HEARING_TYPE, listing.getListingType());
        assertEquals(DURATION_OF_DAY, listing.getListingDuration());
        assertEquals(0, listing.getListingMultiDay().getWeeks());
        assertEquals(1, listing.getListingMultiDay().getDays());
        assertEquals(1, listing.getListingMultiDay().getHours());
        assertEquals(LOCAL_DATE_TIME, listing.getListingDate());
        assertEquals(4, listing.getListingNumberAttendees());
        assertEquals(LISTING_COMMENTS, listing.getListingComments());
        assertEquals(HEARING_REQUESTER, listing.getListingRequestedBy());
        assertEquals(false, listing.getListingPrivateFlag());
        assertEquals(1, listing.getListingJohs().size());
        assertEquals(listingJoh, listing.getListingJohs().get(0));
        assertEquals(2, listing.getListingHearingChannels().size());
        assertEquals(HEARING_CHANNEL, listing.getListingHearingChannels().get(0));
        assertEquals(1, listing.getListingLocations().size());
        assertNull(listing.getListingStartDate());
        assertNull(listing.getListingEndDate());
        assertNull(listing.getListingJohTiers());
    }

    @Test
    void shouldReturnListingForMultiDayHearingDurationLessThan360() {
        val listing = getListing(360);
        assertEquals(360, listing.getListingDuration());
        assertNull(listing.getListingMultiDay());
    }

    @Test
    void shouldReturnListingForMultiDayHearingDurationIs360() {
        val listing = getListing(360);
        assertEquals(DURATION_OF_DAY, listing.getListingDuration());
        assertNull(listing.getListingMultiDay());
    }

    @Test
    void shouldReturnListingForMultiDayHearingDurationIs720() {
        val listing = getListing(720);
        assertEquals(DURATION_OF_DAY, listing.getListingDuration());
        assertEquals(0, listing.getListingMultiDay().getWeeks());
        assertEquals(2, listing.getListingMultiDay().getDays());
        assertEquals(0, listing.getListingMultiDay().getHours());
    }

    @Test
    void shouldReturnListingForMultiDayHearingDurationIs1800() {
        val listing = getListing(1800);
        assertEquals(DURATION_OF_DAY, listing.getListingDuration());
        assertEquals(1, listing.getListingMultiDay().getWeeks());
        assertEquals(0, listing.getListingMultiDay().getDays());
        assertEquals(0, listing.getListingMultiDay().getHours());
    }

    @Test
    void shouldReturnListingForMultiDayHearingDurationIs2160() {
        val listing = getListing(2160);
        assertEquals(DURATION_OF_DAY, listing.getListingDuration());
        assertEquals(1, listing.getListingMultiDay().getWeeks());
        assertEquals(1, listing.getListingMultiDay().getDays());
        assertEquals(0, listing.getListingMultiDay().getHours());
    }

    @Test
    void shouldReturnListingForMultiDayHearingDurationIs2165() {
        val listing = getListing(2165);
        assertEquals(DURATION_OF_DAY, listing.getListingDuration());
        assertEquals(1, listing.getListingMultiDay().getWeeks());
        assertEquals(1, listing.getListingMultiDay().getDays());
        assertEquals(1, listing.getListingMultiDay().getHours());
    }

    private HearingDetails buildHearingDetails(int duration) {
        HearingDetails hearingDetails = TestingUtil.hearingDetailsWithAllFields();
        hearingDetails.setDuration(duration);
        hearingDetails.getHearingWindow().setFirstDateTimeMustBe(LOCAL_DATE_TIME);
        hearingDetails.getHearingWindow().setDateRangeStart(LOCAL_DATE_TIME.minusDays(1).toLocalDate());
        hearingDetails.getHearingWindow().setDateRangeEnd(LOCAL_DATE_TIME.plusDays(1).toLocalDate());
        return hearingDetails;
    }

    private List<String> buildFacilityTypes() {
        return List.of(FACILITY_TYPE_1,FACILITY_TYPE_2);
    }

    private ListingLocation generateListingLocation() {
        ListingLocation listingLocation = ListingLocation.builder().build();
        listingLocation.setLocationId("court Id");
        listingLocation.setLocationType(COURT);
        listingLocation.setLocationReferenceType(EPIMS);
        when(listingLocationsMapper.getListingLocations(any())).thenReturn(Collections.singletonList(listingLocation));
        return listingLocation;
    }

    private void generateOtherConsiderations(List<String> facilityTypes, ListingJoh listingJoh) {
        List<String> otherConsiderations = new ArrayList<>();
        otherConsiderations.add(hearingInWelsh.toString());
        otherConsiderations.addAll(facilityTypes);

        when(listingJohsMapper.getListingJohs(any())).thenReturn(Collections.singletonList(listingJoh));
        when(listingOtherConsiderationsMapper.getListingOtherConsiderations(any(), any()))
            .thenReturn(otherConsiderations);
    }

    @Test
    void shouldReturnListingForMultiDayHearingDurationWithManyValues() {

        testCalculatedListingForDurationValue(361, 0, 1, 1);
        testCalculatedListingForDurationValue(369, 0, 1, 1);
        testCalculatedListingForDurationValue(725, 0, 2, 1);
        testCalculatedListingForDurationValue(730, 0, 2, 1);
        testCalculatedListingForDurationValue(425, 0, 1, 2);
        testCalculatedListingForDurationValue(476, 0, 1, 2);
        testCalculatedListingForDurationValue(485, 0, 1, 3);
    }

    private void testCalculatedListingForDurationValue(Integer duration,
                                                       Integer expectedWeeks,
                                                       Integer expectedDays,
                                                       Integer expectedHours
    ) {
        val listing = getListing(duration);
        assertEquals(DURATION_OF_DAY, listing.getListingDuration());
        assertEquals(expectedWeeks, listing.getListingMultiDay().getWeeks());
        assertEquals(expectedDays, listing.getListingMultiDay().getDays());
        assertEquals(expectedHours, listing.getListingMultiDay().getHours());
    }

    private Listing getListing(int duration) {
        val hearingDetails = buildHearingDetails(HearingMapper.roundUpDuration(duration));
        Listing listing = listingMapper.getListing(hearingDetails);
        return listing;
    }
}
