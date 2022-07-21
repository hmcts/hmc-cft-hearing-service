package uk.gov.hmcts.reform.hmc.helper.hmi;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.helper.HearingMapper;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingWindow;
import uk.gov.hmcts.reform.hmc.model.PanelPreference;
import uk.gov.hmcts.reform.hmc.model.PanelRequirements;
import uk.gov.hmcts.reform.hmc.model.RoomAttribute;
import uk.gov.hmcts.reform.hmc.model.hmi.Entity;
import uk.gov.hmcts.reform.hmc.model.hmi.Listing;
import uk.gov.hmcts.reform.hmc.model.hmi.ListingJoh;
import uk.gov.hmcts.reform.hmc.model.hmi.ListingLocation;
import uk.gov.hmcts.reform.hmc.repository.CaseHearingRequestRepository;
import uk.gov.hmcts.reform.hmc.service.RoomAttributesService;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.constants.Constants.AMEND_REASON_CODE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.COURT;
import static uk.gov.hmcts.reform.hmc.constants.Constants.DURATION_OF_DAY;
import static uk.gov.hmcts.reform.hmc.constants.Constants.EPIMS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.VERSION_NUMBER_TO_INCREMENT;

@ExtendWith(MockitoExtension.class)
class ListingMapperTest {

    @Mock
    private ListingJohsMapper listingJohsMapper;

    @Mock
    private ListingLocationsMapper listingLocationsMapper;

    @Mock
    private RoomAttributesService roomAttributesService;

    @Mock
    CaseHearingRequestRepository caseHearingRequestRepository;

    @InjectMocks
    private ListingMapper listingMapper;

    private static final String HEARING_PRIORITY_TYPE = "Priority type";
    private static final String HEARING_TYPE = "Some hearing type";
    private static final String LISTING_COMMENTS = "Some listing comments";
    private static final String HEARING_REQUESTER = "Some judge";
    private static final String ROLE_TYPE = "RoleType1";
    private static final String HEARING_CHANNEL = "someChannelType";
    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.now();
    private static final Long HEARING_ID = 1L;

    @Test
    void shouldReturnListingWithBothHearingWindowFieldsAndRoleType() {
        ListingJoh listingJoh = generateListingJoh();
        ListingLocation listingLocation = generateListingLocation();
        HearingDetails hearingDetails = buildHearingDetails(150);
        Listing listing = buildListing(hearingDetails,TestingUtil.getEntity(hearingDetails.getFacilitiesRequired()));

        assertListingLocations(listingLocation, listing.getListingLocations());
        assertListingJohs(listingJoh, listing.getListingJohs());

        assertEquals(HEARING_PRIORITY_TYPE, listing.getListingPriority());
        assertEquals(HEARING_TYPE, listing.getListingType());
        assertEquals(150, listing.getListingDuration());
        assertNull(listing.getListingMultiDay());
        assertEquals(LOCAL_DATE_TIME, listing.getListingDate());
        assertEquals(4, listing.getListingNumberAttendees());
        assertEquals(LISTING_COMMENTS, listing.getListingComments());
        assertEquals(HEARING_REQUESTER, listing.getListingRequestedBy());
        assertEquals(false, listing.getListingPrivateFlag());
        assertEquals(2, listing.getListingHearingChannels().size());
        assertEquals(AMEND_REASON_CODE, listing.getAmendReasonCode());
        assertEquals(HEARING_CHANNEL, listing.getListingHearingChannels().get(0));
        assertEquals(LOCAL_DATE_TIME.minusDays(1).toLocalDate(), listing.getListingStartDate());
        assertEquals(LOCAL_DATE_TIME.plusDays(1).toLocalDate(), listing.getListingEndDate());
        assertEquals(2, listing.getListingJohTiers().size());
        assertEquals(ROLE_TYPE, listing.getListingJohTiers().get(0));
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
        assertEquals(1, listing.getListingMultiDay().getHours());
    }

    @Test
    void shouldReturnEmptyListingOtherConsiderationsWhenFacilityTypesIsEmpty() {
        HearingDetails hearingDetails = buildHearingDetails(DURATION_OF_DAY);
        hearingDetails.setFacilitiesRequired(List.of());
        Listing listing = buildListing(hearingDetails,Entity.builder().build());
        assertTrue(listing.getListingOtherConsiderations().isEmpty());
    }

    @Test
    void shouldReturnEmptyListingFieldsIfEntitiesListIsNull() {
        HearingDetails hearingDetails = buildHearingDetails(DURATION_OF_DAY);
        generateCaseHearingRequestEntity(VERSION_NUMBER_TO_INCREMENT);
        Listing listing = listingMapper.getListing(hearingDetails,null, null);
        assertTrue(listing.getListingOtherConsiderations().isEmpty());
        assertTrue(listing.getRoomAttributes().isEmpty());
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
    void shouldReturnListingWithOtherConsiderationsAC03() {
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
    void shouldReturnListingWithOtherConsiderationsAC04() {
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

    @Test
    void shouldReturnListingWithAutoCreateFlag() {
        HearingDetails hearingDetails = buildHearingDetails(150);
        hearingDetails.setAutoListFlag(true);
        hearingDetails.setListingAutoChangeReasonCode(null);
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
    void shouldFail_whenListingAutoChangeReasonCodeIsProvidedAndAutoListFlagIsTrue() {
        HearingDetails hearingDetails = buildHearingDetails(DURATION_OF_DAY);
        assertNotNull(hearingDetails.getListingAutoChangeReasonCode());

        hearingDetails.setAutoListFlag(true);
        generateCaseHearingRequestEntity(VERSION_NUMBER_TO_INCREMENT);
        Exception exception = assertThrows(BadRequestException.class, () ->
            listingMapper.getListing(hearingDetails,null, null));
        assertEquals(
            "001 autoListFlag must be FALSE if you supply a change reasoncode",
            exception.getMessage());
    }

    private void assertListingJohs(ListingJoh listingJoh, List<ListingJoh> listingJohList) {
        assertEquals(1, listingJohList.size());
        assertEquals(listingJoh, listingJohList.get(0));
    }

    private void assertListingLocations(ListingLocation listingLocation, List<ListingLocation> listingLocations) {
        assertEquals(listingLocation, listingLocations.get(0));
        assertEquals(1, listingLocations.size());
        assertEquals("court Id", listingLocations.get(0).getLocationId());
        assertEquals(EPIMS, listingLocations.get(0).getLocationReferenceType());
        assertEquals(COURT, listingLocations.get(0).getLocationType());
    }

    @Test
    void shouldReturnListingIfHearingWindowNotPresent() {
        HearingDetails hearingDetails = buildHearingDetailsWithNoHearingWindow(2165);
        Listing listing = buildListing(hearingDetails,TestingUtil.getEntity(hearingDetails.getFacilitiesRequired()));
        assertNull(listing.getListingDate());
        assertNull(listing.getListingStartDate());
        assertNull(listing.getListingEndDate());
        assertEquals(DURATION_OF_DAY, listing.getListingDuration());
        assertEquals(1, listing.getListingMultiDay().getWeeks());
        assertEquals(1, listing.getListingMultiDay().getDays());
        assertEquals(1, listing.getListingMultiDay().getHours());
    }

    private HearingDetails buildHearingDetailsWithNoHearingWindow(int duration) {
        PanelRequirements panelRequirements = new PanelRequirements();
        PanelPreference panelPreference = new PanelPreference();
        panelRequirements.setPanelPreferences(Collections.singletonList(panelPreference));
        panelRequirements.setRoleType(null);
        HearingDetails hearingDetails = buildHearingDetails(DURATION_OF_DAY);
        hearingDetails.setPanelRequirements(panelRequirements);
        hearingDetails.setPanelRequirements(panelRequirements);
        hearingDetails.setHearingWindow(null);
        hearingDetails.setDuration(duration);
        return hearingDetails;
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
        generateCaseHearingRequestEntity(VERSION_NUMBER_TO_INCREMENT);
        return listingMapper.getListing(hearingDetails,List.of(entity),HEARING_ID);
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
        generateCaseHearingRequestEntity(VERSION_NUMBER_TO_INCREMENT);
        val hearingDetails = buildHearingDetails(HearingMapper.roundUpDuration(duration));
        return listingMapper.getListing(hearingDetails, null,null);
    }

    private void generateCaseHearingRequestEntity(Integer version) {
        CaseHearingRequestEntity caseHearingRequest = new CaseHearingRequestEntity();
        caseHearingRequest.setVersionNumber(version);
        caseHearingRequest.setCaseHearingID(1L);
        caseHearingRequest.setHearing(TestingUtil.hearingEntity());
        when(caseHearingRequestRepository.getLatestCaseHearingRequest(any())).thenReturn(caseHearingRequest);
    }
}
