package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingWindow;
import uk.gov.hmcts.reform.hmc.model.PanelPreference;
import uk.gov.hmcts.reform.hmc.model.PanelRequirements;
import uk.gov.hmcts.reform.hmc.model.RoomAttribute;
import uk.gov.hmcts.reform.hmc.model.hmi.Entity;
import uk.gov.hmcts.reform.hmc.model.hmi.Listing;
import uk.gov.hmcts.reform.hmc.model.hmi.ListingJoh;
import uk.gov.hmcts.reform.hmc.model.hmi.ListingLocation;
import uk.gov.hmcts.reform.hmc.service.RoomAttributesService;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    private RoomAttributesService roomAttributesService;

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

    @Test
    void shouldReturnListingWithBothHearingWindowFieldsAndRoleType() {
        ListingJoh listingJoh = generateListingJoh();
        ListingLocation listingLocation = generateListingLocation();
        HearingDetails hearingDetails = buildHearingDetails(150);
        Listing listing = buildListing(hearingDetails,TestingUtil.getEntity(hearingDetails.getFacilitiesRequired()));

        assertListingLocations(listingLocation, listing.getListingLocations());
        assertListingJohs(listingJoh, listing.getListingJohs());
        assertOtherConsiderations(listing.getListingOtherConsiderations());

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
        assertEquals(false, listing.getListingWelshHearingFlag());
        assertEquals(2, listing.getListingHearingChannels().size());
        assertEquals(AMEND_REASON_CODE, listing.getAmendReasonCode());
        assertEquals(HEARING_CHANNEL, listing.getListingHearingChannels().get(0));
        assertEquals(LOCAL_DATE_TIME.minusDays(1).toLocalDate(), listing.getListingStartDate());
        assertEquals(LOCAL_DATE_TIME.plusDays(1).toLocalDate(), listing.getListingEndDate());
        assertEquals(2, listing.getListingJohTiers().size());
        assertEquals(ROLE_TYPE, listing.getListingJohTiers().get(0));
        assertNotNull(listing.getRoomAttributes());
    }

    @Test
    void shouldReturnListingWithHearingWindowFieldsAndRoleTypeNull() {
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

        ListingJoh listingJoh = generateListingJoh();
        ListingLocation listingLocation = generateListingLocation();
        Listing listing = buildListing(hearingDetails,Entity.builder().build());

        assertListingLocations(listingLocation, listing.getListingLocations());
        assertListingJohs(listingJoh, listing.getListingJohs());

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
        assertEquals(2, listing.getListingHearingChannels().size());
        assertTrue(listing.getListingHearingChannels().contains(HEARING_CHANNEL));
        assertNull(listing.getListingStartDate());
        assertNull(listing.getListingEndDate());
        assertNull(listing.getListingJohTiers());
    }

    @Test
    void shouldReturnListingWithNoRoleTypeWhenEmpty() {
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
        ListingJoh listingJoh = generateListingJoh();
        ListingLocation listingLocation = generateListingLocation();
        Listing listing = buildListing(hearingDetails,Entity.builder().build());

        assertListingLocations(listingLocation, listing.getListingLocations());
        assertListingJohs(listingJoh, listing.getListingJohs());

        assertEquals(true, listing.getListingAutoCreateFlag());
        assertEquals(HEARING_PRIORITY_TYPE, listing.getListingPriority());
        assertEquals(HEARING_TYPE, listing.getListingType());
        assertEquals(DURATION_OF_DAY, listing.getListingDuration());
        assertEquals(0, listing.getListingMultiDay().getWeeks());
        assertEquals(1, listing.getListingMultiDay().getDays());
        assertEquals(5, listing.getListingMultiDay().getHours());
        assertEquals(LOCAL_DATE_TIME, listing.getListingDate());
        assertEquals(4, listing.getListingNumberAttendees());
        assertEquals(LISTING_COMMENTS, listing.getListingComments());
        assertEquals(HEARING_REQUESTER, listing.getListingRequestedBy());
        assertEquals(false, listing.getListingPrivateFlag());
        assertEquals(2, listing.getListingHearingChannels().size());
        assertEquals(HEARING_CHANNEL, listing.getListingHearingChannels().get(0));
        assertNull(listing.getListingStartDate());
        assertNull(listing.getListingEndDate());
        assertNull(listing.getListingJohTiers());
    }

    @Test
    void shouldReturnListingForMultiDayHearingDurationLessThan360() {
        HearingDetails hearingDetails = buildHearingDetails(300);
        Listing listing = buildListing(hearingDetails,Entity.builder().build());
        assertEquals(300, listing.getListingDuration());
        assertNull(listing.getListingMultiDay());
    }

    @Test
    void shouldReturnListingForMultiDayHearingDurationIs360() {
        HearingDetails hearingDetails = buildHearingDetails(360);
        Listing listing = buildListing(hearingDetails,Entity.builder().build());
        assertEquals(DURATION_OF_DAY, listing.getListingDuration());
        assertNull(listing.getListingMultiDay());
    }

    @Test
    void shouldReturnListingForMultiDayHearingDurationIs720() {
        HearingDetails hearingDetails = buildHearingDetails(720);
        Listing listing = buildListing(hearingDetails,Entity.builder().build());
        assertEquals(DURATION_OF_DAY, listing.getListingDuration());
        assertEquals(0, listing.getListingMultiDay().getWeeks());
        assertEquals(2, listing.getListingMultiDay().getDays());
        assertEquals(0, listing.getListingMultiDay().getHours());
    }

    @Test
    void shouldReturnListingForMultiDayHearingDurationIs1800() {
        HearingDetails hearingDetails = buildHearingDetails(1800);
        Listing listing = buildListing(hearingDetails,Entity.builder().build());
        assertEquals(DURATION_OF_DAY, listing.getListingDuration());
        assertEquals(1, listing.getListingMultiDay().getWeeks());
        assertEquals(0, listing.getListingMultiDay().getDays());
        assertEquals(0, listing.getListingMultiDay().getHours());
    }

    @Test
    void shouldReturnListingForMultiDayHearingDurationIs2160() {
        HearingDetails hearingDetails = buildHearingDetails(2160);
        Listing listing = buildListing(hearingDetails,Entity.builder().build());
        assertEquals(DURATION_OF_DAY, listing.getListingDuration());
        assertEquals(1, listing.getListingMultiDay().getWeeks());
        assertEquals(1, listing.getListingMultiDay().getDays());
        assertEquals(0, listing.getListingMultiDay().getHours());
    }

    @Test
    void shouldReturnListingForMultiDayHearingDurationIs2165() {
        HearingDetails hearingDetails = buildHearingDetails(2165);
        Listing listing = buildListing(hearingDetails,Entity.builder().build());
        assertEquals(DURATION_OF_DAY, listing.getListingDuration());
        assertEquals(1, listing.getListingMultiDay().getWeeks());
        assertEquals(1, listing.getListingMultiDay().getDays());
        assertEquals(5, listing.getListingMultiDay().getHours());
    }

    @Test
    void shouldReturnEmptyListingOtherConsiderationsWhenFacilityTypesIsEmpty() {
        HearingDetails hearingDetails = buildHearingDetails(DURATION_OF_DAY);
        hearingDetails.setFacilitiesRequired(List.of());
        Listing listing = buildListing(hearingDetails,Entity.builder().build());
        assertTrue(listing.getListingOtherConsiderations().isEmpty());
    }

    @Test
    void shouldReturnListingWithRoomAttributeAC01() {
        HearingDetails hearingDetails = buildHearingDetails(150);
        hearingDetails.setFacilitiesRequired(List.of("ReasonableAdjustment1"));
        Optional<RoomAttribute> roomAttribute =
            TestingUtil.getRoomAttribute("RoomCode1", "Name1",
                "ReasonableAdjustment1", false);
        when(roomAttributesService.findByReasonableAdjustmentCode("ReasonableAdjustment1"))
            .thenReturn(roomAttribute);
        Listing listing = buildListing(hearingDetails,TestingUtil.getEntity(hearingDetails.getFacilitiesRequired()));
        assertNotNull(listing.getRoomAttributes());
        assertTrue(listing.getRoomAttributes().contains("RoomCode1"));
    }

    @Test
    void shouldReturnListingWithRoomAttributeAC02() {
        HearingDetails hearingDetails = buildHearingDetails(150);
        hearingDetails.setFacilitiesRequired(List.of("RoomCode1"));
        Optional<RoomAttribute> roomAttribute =
            TestingUtil.getRoomAttribute("RoomCode1", "Name1",
                "ReasonableAdjustment1", true);

        when(roomAttributesService.findByRoomAttributeCode("RoomCode1"))
            .thenReturn(roomAttribute);
        Listing listing = buildListing(hearingDetails,TestingUtil.getEntity(hearingDetails.getFacilitiesRequired()));
        assertNotNull(listing.getRoomAttributes());
        assertTrue(listing.getRoomAttributes().contains("RoomCode1"));
    }

    @Test
    void shouldReturnListingWithRoomAttributeAC03() {
        HearingDetails hearingDetails = buildHearingDetails(150);
        hearingDetails.setFacilitiesRequired(List.of("RoomCode1"));
        Optional<RoomAttribute> roomAttribute =
            TestingUtil.getRoomAttribute("RoomCode1", "Name1",
                "ReasonableAdjustment1", false);

        when(roomAttributesService.findByRoomAttributeCode("RoomCode1"))
            .thenReturn(roomAttribute);
        Listing listing = buildListing(hearingDetails,TestingUtil.getEntity(hearingDetails.getFacilitiesRequired()));
        assertNotNull(listing.getListingOtherConsiderations());
        assertTrue(listing.getListingOtherConsiderations().contains("RoomCode1"));
    }

    @Test
    void shouldReturnListingWithRoomAttributeAC04() {
        HearingDetails hearingDetails = buildHearingDetails(150);
        hearingDetails.setFacilitiesRequired(List.of("randomReasonableAdjustment"));
        Optional<RoomAttribute> roomAttribute =
            TestingUtil.getRoomAttribute("RoomCode1", "Name1",
                "ReasonableAdjustment1", false);
        when(roomAttributesService.findByRoomAttributeCode("randomReasonableAdjustment"))
            .thenReturn(Optional.empty());
        when(roomAttributesService.findByReasonableAdjustmentCode("randomReasonableAdjustment"))
            .thenReturn(Optional.empty());
        Listing listing = buildListing(hearingDetails,TestingUtil.getEntity(hearingDetails.getFacilitiesRequired()));
        assertNotNull(listing.getListingOtherConsiderations());
        assertTrue(listing.getListingOtherConsiderations().contains("randomReasonableAdjustment"));
    }


    private void assertListingJohs(ListingJoh listingJoh, List<ListingJoh> listingJohList) {
        assertEquals(1, listingJohList.size());
        assertEquals(listingJoh, listingJohList.get(0));
    }

    private void assertOtherConsiderations(List<String> otherConsiderationsList) {
        assertEquals(2, otherConsiderationsList.size());
        assertTrue(otherConsiderationsList.contains("facility1"));
        assertTrue(otherConsiderationsList.contains("facility2"));
    }

    private void assertListingLocations(ListingLocation listingLocation, List<ListingLocation> listingLocations) {
        assertEquals(listingLocation, listingLocations.get(0));
        assertEquals(1, listingLocations.size());
        assertEquals("court Id", listingLocations.get(0).getLocationId());
        assertEquals(EPIMS, listingLocations.get(0).getLocationReferenceType());
        assertEquals(COURT, listingLocations.get(0).getLocationType());
    }

    private HearingDetails buildHearingDetails(int duration) {
        HearingDetails hearingDetails = TestingUtil.hearingDetailsWithAllFields();
        hearingDetails.setDuration(duration);
        hearingDetails.getHearingWindow().setFirstDateTimeMustBe(LOCAL_DATE_TIME);
        hearingDetails.getHearingWindow().setDateRangeStart(LOCAL_DATE_TIME.minusDays(1).toLocalDate());
        hearingDetails.getHearingWindow().setDateRangeEnd(LOCAL_DATE_TIME.plusDays(1).toLocalDate());
        return hearingDetails;
    }

    private ListingLocation generateListingLocation() {
        ListingLocation listingLocation = ListingLocation.builder().build();
        listingLocation.setLocationId("court Id");
        listingLocation.setLocationType(COURT);
        listingLocation.setLocationReferenceType(EPIMS);
        when(listingLocationsMapper.getListingLocations(any())).thenReturn(Collections.singletonList(listingLocation));
        return listingLocation;
    }

    private ListingJoh generateListingJoh() {
        ListingJoh listingJoh = ListingJoh.builder().build();
        when(listingJohsMapper.getListingJohs(any())).thenReturn(Collections.singletonList(listingJoh));
        return listingJoh;
    }

    private Listing buildListing(HearingDetails hearingDetails,Entity entity) {
        return listingMapper.getListing(hearingDetails,List.of(entity));
    }
}
