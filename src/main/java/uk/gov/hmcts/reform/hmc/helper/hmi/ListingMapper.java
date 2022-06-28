package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
            .listingDate(hearingDetails.getHearingWindow().getFirstDateTimeMustBe())
            .listingNumberAttendees(hearingDetails.getNumberOfPhysicalAttendees())
            .listingComments(hearingDetails.getListingComments())
            .listingRequestedBy(hearingDetails.getHearingRequester())
            .listingPrivateFlag(hearingDetails.getPrivateHearingRequiredFlag())
            .listingJohs(listingJohsMapper.getListingJohs(hearingDetails.getPanelRequirements()))
            .listingHearingChannels(hearingDetails.getHearingChannels())
            .listingLocations(listingLocationsMapper.getListingLocations(hearingDetails.getHearingLocations()))
            .amendReasonCode(hearingDetails.getAmendReasonCode())
            .listingJohSpecialisms(hearingDetails.getPanelRequirements().getPanelSpecialisms())
            .listingJohTickets(hearingDetails.getPanelRequirements().getAuthorisationSubType())
            .listingWelshHearingFlag(hearingDetails.getHearingInWelshFlag())
            .build();

        if (entitiesList != null && !entitiesList.isEmpty()) {
            if (!areRoomAttributesFound(entitiesList, hearingDetails, listing)) {
                listing.setListingOtherConsiderations(hearingDetails.getFacilitiesRequired());
                listing.setRoomAttributes(List.of());
            }
        } else {
            listing.setListingOtherConsiderations(List.of());
            listing.setRoomAttributes(List.of());
        }

        if (hearingDetails.getHearingWindow().getDateRangeStart() != null) {
            listing.setListingStartDate(hearingDetails.getHearingWindow().getDateRangeStart());
        }
        if (hearingDetails.getHearingWindow().getDateRangeEnd() != null) {
            listing.setListingEndDate(hearingDetails.getHearingWindow().getDateRangeEnd());
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
        return hearingDetailsDuration - (weeks * 360 * 5) - (days * 360);
    }

    private int getDays(Integer hearingDetailsDuration, int weeks) {
        return ((hearingDetailsDuration - weeks * 360 * 5) / 360);
    }

    private int getWeeks(Integer hearingDetailsDuration) {
        return (hearingDetailsDuration / (360 * 5));
    }

    private boolean areRoomAttributesFound(List<Entity> entityList,
                                         HearingDetails hearingDetails,
                                         Listing listing) {
        Set<String> roomAttributesSet = new HashSet<>();
        Set<String> otherConsiderationsSet = new HashSet<>();
        entityList.forEach(entity -> {
            roomAttributesSet.addAll(getRoomAttributesByReasonableAdjustment(entity));
            otherConsiderationsSet.addAll(getRoomAttributeByAttributeCode(
                roomAttributesSet,
                hearingDetails.getFacilitiesRequired()));
        });

        if (roomAttributesSet.isEmpty() && otherConsiderationsSet.isEmpty()) {
            return false;
        } else {
            listing.setRoomAttributes((new ArrayList<>(roomAttributesSet)));
            listing.setListingOtherConsiderations(new ArrayList<>(otherConsiderationsSet));
            return true;
        }
    }

    private List<String> getRoomAttributesByReasonableAdjustment(Entity entity) {
        List<String> roomAttributesList = new ArrayList<>();
        if (entity.getEntityOtherConsiderations() != null && !entity.getEntityOtherConsiderations().isEmpty()) {
            for (String reasonableAdjustment : entity.getEntityOtherConsiderations()) {
                Optional<RoomAttribute> roomAttributeByReasonableAdjustment =
                    roomAttributesService.findByReasonableAdjustmentCode(reasonableAdjustment);
                roomAttributeByReasonableAdjustment.ifPresent(
                    roomAttribute
                        -> roomAttributesList.add(roomAttribute.getRoomAttributeCode()));
            }
        }
        return roomAttributesList;
    }

    private List<String> getRoomAttributeByAttributeCode(Set<String> roomAttributesSet, List<String> facilityTypes) {
        List<String> otherConsiderationsList = new ArrayList<>();
        if (facilityTypes != null && !facilityTypes.isEmpty()) {
            for (String facility : facilityTypes) {
                Optional<RoomAttribute> roomAttributeByAttributeCode =
                    roomAttributesService.findByRoomAttributeCode(facility);
                if (roomAttributeByAttributeCode.isPresent() && roomAttributeByAttributeCode.get().isFacility()) {
                    roomAttributesSet.add(roomAttributeByAttributeCode.get().getRoomAttributeCode());
                } else {
                    otherConsiderationsList.add(facility);
                }
            }
        }
        return otherConsiderationsList;
    }

}
