package uk.gov.hmcts.reform.hmc.helper.hmi;

import io.jsonwebtoken.lang.Collections;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.client.hmi.ListingReasonCode;
import uk.gov.hmcts.reform.hmc.constants.Constants;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.RoomAttribute;
import uk.gov.hmcts.reform.hmc.model.hmi.Entity;
import uk.gov.hmcts.reform.hmc.model.hmi.Listing;
import uk.gov.hmcts.reform.hmc.model.hmi.ListingMultiDay;
import uk.gov.hmcts.reform.hmc.repository.CaseHearingRequestRepository;
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
    private final CaseHearingRequestRepository caseHearingRequestRepository;
    private boolean reasonableAdjustmentIsMappedToRoomAttributes = false;

    @Autowired
    public ListingMapper(ListingJohsMapper listingJohsMapper,
                         ListingLocationsMapper listingLocationsMapper,
                         RoomAttributesService roomAttributesService,
                         CaseHearingRequestRepository caseHearingRequestRepository) {
        this.listingJohsMapper = listingJohsMapper;
        this.listingLocationsMapper = listingLocationsMapper;
        this.roomAttributesService = roomAttributesService;
        this.caseHearingRequestRepository = caseHearingRequestRepository;
    }

    public Listing getListing(HearingDetails hearingDetails, List<Entity> entitiesList, Long hearingId) {

        Listing listing = Listing.builder()
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

        if (entitiesList != null && !entitiesList.isEmpty()) {
            if (!areRoomAttributesFound(entitiesList, hearingDetails, listing)) {
                listing.setListingOtherConsiderations(List.of());
                listing.setRoomAttributes(List.of());
            }
        } else {
            listing.setListingOtherConsiderations(List.of());
            listing.setRoomAttributes(List.of());
        }
        setAutoListFlag(hearingDetails, hearingId, listing);

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

    private boolean areRoomAttributesFound(List<Entity> entitiesList,
                                           HearingDetails hearingDetails,
                                           Listing listing) {
        Set<String> roomAttributesSet = new HashSet<>();
        Set<String> otherConsiderationsSet = new HashSet<>();
        entitiesList.forEach(entity -> {
            List<String> roomAttributesByReasonableAdjustmentList =
                getRoomAttributesByReasonableAdjustmentCode(entity);
            List<String> roomAttributesByAttributeCodeList =
                getRoomAttributesByAttributeCode(hearingDetails.getFacilitiesRequired());
            if (!roomAttributesByReasonableAdjustmentList.isEmpty()) {
                roomAttributesSet.addAll(roomAttributesByReasonableAdjustmentList);
                reasonableAdjustmentIsMappedToRoomAttributes = true;
            } else if (!roomAttributesByAttributeCodeList.isEmpty()) {
                roomAttributesSet.addAll(roomAttributesByAttributeCodeList);
            } else {
                if (hearingDetails.getFacilitiesRequired() != null
                    && !hearingDetails.getFacilitiesRequired().isEmpty()) {
                    otherConsiderationsSet.addAll(hearingDetails.getFacilitiesRequired());
                }
            }
        });

        if (!roomAttributesSet.isEmpty() || !otherConsiderationsSet.isEmpty()) {
            if (!roomAttributesSet.isEmpty()) {
                listing.setRoomAttributes(new ArrayList<>(roomAttributesSet));
            }
            if (!otherConsiderationsSet.isEmpty()) {
                listing.setListingOtherConsiderations(new ArrayList<>(otherConsiderationsSet));
            }
            return true;
        }
        return false;
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

    private List<String> getRoomAttributesByAttributeCode(List<String> facilityTypes) {
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

    private void setAutoListFlag(HearingDetails hearingDetails, Long hearingId, Listing listing) {
        CaseHearingRequestEntity requestEntity = caseHearingRequestRepository.getLatestCaseHearingRequest(hearingId);
        if (Boolean.TRUE.equals(hearingDetails.getAutoListFlag())
            && !(reasonableAdjustmentIsMappedToRoomAttributes
                && hearingDetails.getFacilitiesRequired().equals(listing.getRoomAttributes()))) {

            listing.setListingAutoCreateFlag(false);
            if (Boolean.TRUE.equals(isUpdateRequest(requestEntity))) {
                requestEntity.setVersionNumber(requestEntity.getHearing().getNextRequestVersion());
                requestEntity.setAutoListFlag(false);
                requestEntity.setListingAutoChangeReasonCode(ListingReasonCode.NO_MAPPING_AVAILABLE.label);
            }

        }

        if (hearingDetails.getListingAutoChangeReasonCode() != null) {
            if (Boolean.FALSE.equals(hearingDetails.getAutoListFlag())) {
                if (Boolean.TRUE.equals(isUpdateRequest(requestEntity))) {
                    requestEntity.setVersionNumber(requestEntity.getHearing().getNextRequestVersion());
                    requestEntity.setListingAutoChangeReasonCode(
                        ListingReasonCode.valueOf(hearingDetails.getListingAutoChangeReasonCode()).label);
                }
            } else {
                throw new BadRequestException(ValidationError.MUST_BE_FALSE_IF_YOU_SUPPLY_A_CHANGE_REASONCODE);
            }
        }
    }

    private Boolean isUpdateRequest(CaseHearingRequestEntity requestEntity) {
        return requestEntity != null;
    }

}

