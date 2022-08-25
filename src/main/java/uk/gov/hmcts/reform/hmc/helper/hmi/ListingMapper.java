package uk.gov.hmcts.reform.hmc.helper.hmi;

import io.jsonwebtoken.lang.Collections;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.constants.Constants;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.RoomAttribute;
import uk.gov.hmcts.reform.hmc.model.hmi.Entity;
import uk.gov.hmcts.reform.hmc.model.hmi.Listing;
import uk.gov.hmcts.reform.hmc.model.hmi.ListingMultiDay;
import uk.gov.hmcts.reform.hmc.service.RoomAttributesService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static uk.gov.hmcts.reform.hmc.constants.Constants.DURATION_OF_DAY;

@Component
public class ListingMapper {

    public static final String WELSH_LANGUAGE_TRUE_VALUE = "cym";
    private final ListingJohsMapper listingJohsMapper;
    private final ListingLocationsMapper listingLocationsMapper;
    private final RoomAttributesService roomAttributesService;

    @Autowired
    public ListingMapper(ListingJohsMapper listingJohsMapper,
                         ListingLocationsMapper listingLocationsMapper,
                         RoomAttributesService roomAttributesService) {
        this.listingJohsMapper = listingJohsMapper;
        this.listingLocationsMapper = listingLocationsMapper;
        this.roomAttributesService = roomAttributesService;
    }

    public Listing getListing(HearingDetails hearingDetails, List<Entity> entitiesList) {

        Listing listing = Listing.builder()
            .listingAutoCreateFlag(hearingDetails.getAutoListFlag())
            .listingPriority(hearingDetails.getHearingPriorityType())
            .listingType(hearingDetails.getHearingType())
            .listingNumberAttendees(hearingDetails.getNumberOfPhysicalAttendees())
            .listingComments(hearingDetails.getListingComments())
            .listingRequestedBy(hearingDetails.getHearingRequester())
            .listingPrivateFlag(hearingDetails.getPrivateHearingRequiredFlag())
            .listingJohs(listingJohsMapper.getListingJohs(hearingDetails.getPanelRequirements()))
            .listingHearingChannels(hearingDetails.getHearingChannels())
            .listingLocations(listingLocationsMapper.getListingLocations(hearingDetails.getHearingLocations()))
            .listingJohSpecialisms(hearingDetails.getPanelRequirements().getPanelSpecialisms())
            .listingJohTickets(hearingDetails.getPanelRequirements().getAuthorisationSubType())
            .listingLanguage(Boolean.TRUE.equals(hearingDetails.getHearingInWelshFlag())
                    ? WELSH_LANGUAGE_TRUE_VALUE : null)
            .build();

        if (hearingDetails.getHearingWindow() != null) {
            listing.setListingDate(hearingDetails.getHearingWindow().getFirstDateTimeMustBe());
            if (hearingDetails.getHearingWindow().getDateRangeStart() != null) {
                listing.setListingStartDate(hearingDetails.getHearingWindow().getDateRangeStart());
            }
            if (hearingDetails.getHearingWindow().getDateRangeEnd() != null) {
                listing.setListingEndDate(hearingDetails.getHearingWindow().getDateRangeEnd());
            }
        }
        if (hearingDetails.getPanelRequirements().getRoleType() != null && !hearingDetails
            .getPanelRequirements().getRoleType().isEmpty()) {
            listing.setListingJohTiers(new ArrayList<>(hearingDetails.getPanelRequirements()
                                                           .getRoleType()));
        }
        if (hearingDetails.isMultiDayHearing()) {
            listing.setListingDuration(DURATION_OF_DAY);
            listing.setListingMultiDay(calculateMultiDayDurations(hearingDetails.getDuration()));
        } else {
            listing.setListingDuration(hearingDetails.getDuration());
        }

        setRoomAttributes(entitiesList, hearingDetails, listing);
        validateAutoListFlag(hearingDetails, listing, entitiesList);

        if (!Collections.isEmpty(hearingDetails.getAmendReasonCodes())) {
            listing.setAmendReasonCode(Constants.AMEND_REASON_CODE);
        }

        return listing;
    }

    private ListingMultiDay calculateMultiDayDurations(Integer hearingDetailsDuration) {
        int weeks = getWeeks(hearingDetailsDuration);
        int days = getDays(hearingDetailsDuration, weeks);
        int hours = getHours(hearingDetailsDuration, weeks, days);
        return setMultiDay(weeks, days, hours);
    }

    private ListingMultiDay setMultiDay(int weeks, int days, int hours) {
        ListingMultiDay multiDay = new ListingMultiDay();
        multiDay.setWeeks(weeks);
        multiDay.setDays(days);
        multiDay.setHours(hours);
        return multiDay;
    }

    private int getHours(Integer hearingDetailsDuration, int weeks, int days) {
        val valueInMinutes = hearingDetailsDuration - (weeks * 360 * 5) - (days * 360);
        val result = (valueInMinutes % 60 == 0) ? valueInMinutes / 60 : Math.floor(valueInMinutes / 60) + 1;
        return Double.valueOf(result).intValue();
    }

    private int getDays(Integer hearingDetailsDuration, int weeks) {
        return ((hearingDetailsDuration - weeks * 360 * 5) / 360);
    }

    private int getWeeks(Integer hearingDetailsDuration) {
        return (hearingDetailsDuration / (360 * 5));
    }

    private void setRoomAttributes(List<Entity> entitiesList, HearingDetails hearingDetails, Listing listing) {
        if (entitiesList != null && !entitiesList.isEmpty()) {
            if (!areRoomAttributesFound(entitiesList, hearingDetails.getFacilitiesRequired(), listing)) {
                listing.setListingOtherConsiderations(List.of());
                listing.setListingRoomAttributes(List.of());
            }
        } else {
            listing.setListingOtherConsiderations(List.of());
            listing.setListingRoomAttributes(List.of());
        }
    }

    private void validateAutoListFlag(HearingDetails hearingDetails, Listing listing, List<Entity> entitiesList) {
        boolean reasonableMatch = checkRoomAttributesByReasonableAdjustmentCode(entitiesList);

        List<String> facilitiesRoomAttributes =
            getRoomAttributesByAttributeCode(hearingDetails.getFacilitiesRequired());
        int size = (hearingDetails.getFacilitiesRequired() == null) ? 0 : hearingDetails.getFacilitiesRequired().size();
        boolean facilitiesMatch = (facilitiesRoomAttributes.size() == size);

        if (Boolean.TRUE.equals(hearingDetails.getAutoListFlag()) && !(reasonableMatch && facilitiesMatch)) {
            listing.setListingAutoCreateFlag(false);
        }
    }

    private boolean areRoomAttributesFound(List<Entity> entitiesList,
                                           List<String> facilitiesRequired, Listing listing) {
        Set<String> roomAttributesSet = new HashSet<>();
        Set<String> otherConsiderationsSet = new HashSet<>();
        getRoomAttributes(entitiesList, facilitiesRequired, roomAttributesSet, otherConsiderationsSet);

        if (!roomAttributesSet.isEmpty() || !otherConsiderationsSet.isEmpty()) {
            if (!roomAttributesSet.isEmpty()) {
                listing.setListingRoomAttributes(new ArrayList<>(roomAttributesSet));
            }
            if (!otherConsiderationsSet.isEmpty()) {
                listing.setListingOtherConsiderations(new ArrayList<>(otherConsiderationsSet));
            }
            return true;
        }
        return false;
    }

    private void getRoomAttributes(List<Entity> entitiesList, List<String> facilitiesRequired,
                                   Set<String> roomAttributesSet, Set<String> otherConsiderationsSet) {
        entitiesList.forEach(entity -> {
            List<String> roomAttributesByReasonableAdjustmentList =
                getRoomAttributesByReasonableAdjustmentCode(entity);
            List<String> roomAttributesByAttributeCodeList =
                getRoomAttributesByAttributeCode(facilitiesRequired);
            if (!roomAttributesByReasonableAdjustmentList.isEmpty()) {
                roomAttributesSet.addAll(roomAttributesByReasonableAdjustmentList);
            } else if (!roomAttributesByAttributeCodeList.isEmpty()) {
                roomAttributesSet.addAll(roomAttributesByAttributeCodeList);
            } else {
                if (facilitiesRequired != null
                    && !facilitiesRequired.isEmpty()) {
                    otherConsiderationsSet.addAll(facilitiesRequired);
                }
            }
        });
    }

    private List<String> getRoomAttributesByReasonableAdjustmentCode(Entity entity) {
        List<String> roomAttributesCodeList = new ArrayList<>();
        if (entity.getEntityOtherConsiderations() != null && !entity.getEntityOtherConsiderations().isEmpty()) {
            for (String reasonableAdjustment : entity.getEntityOtherConsiderations()) {
                Optional<RoomAttribute> roomAttributeByReasonableAdjustment =
                    roomAttributesService.findByReasonableAdjustmentCode(reasonableAdjustment);
                roomAttributeByReasonableAdjustment.ifPresent(
                    roomAttribute
                        -> roomAttributesCodeList.add(roomAttribute.getRoomAttributeCode()));
            }
        }
        return roomAttributesCodeList;
    }

    public boolean checkRoomAttributesByReasonableAdjustmentCode(List<Entity> entities) {
        boolean match = true;
        if (entities != null && !entities.isEmpty()) {
            for (Entity entity : entities) {
                if (entity.getEntityOtherConsiderations() != null && !entity.getEntityOtherConsiderations().isEmpty()) {
                    for (String reasonableAdjustment : entity.getEntityOtherConsiderations()) {
                        Optional<RoomAttribute> roomAttributeByReasonableAdjustment =
                            roomAttributesService.findByReasonableAdjustmentCode(reasonableAdjustment);
                        if (roomAttributeByReasonableAdjustment.isEmpty()) {
                            match = false;
                            break;
                        }
                    }
                }
            }
        }
        return match;
    }

    public List<String> getRoomAttributesByAttributeCode(List<String> facilityTypes) {
        List<String> roomAttributesCodeList = new ArrayList<>();
        if (facilityTypes != null && !facilityTypes.isEmpty()) {
            for (String facility : facilityTypes) {
                Optional<RoomAttribute> roomAttributeByAttributeCode =
                    roomAttributesService.findByRoomAttributeCode(facility);
                if (roomAttributeByAttributeCode.isPresent() && roomAttributeByAttributeCode.get().isFacility()) {
                    roomAttributesCodeList.add(roomAttributeByAttributeCode.get().getRoomAttributeCode());
                }
            }
        }
        return roomAttributesCodeList;
    }

}
