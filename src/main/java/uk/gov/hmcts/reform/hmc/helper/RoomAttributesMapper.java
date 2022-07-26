package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.RoomAttribute;
import uk.gov.hmcts.reform.hmc.model.hmi.Entity;
import uk.gov.hmcts.reform.hmc.model.hmi.Listing;
import uk.gov.hmcts.reform.hmc.service.RoomAttributesService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class RoomAttributesMapper {

    private boolean reasonableAdjustmentIsMappedToRoomAttributes;
    private boolean hearingIsMappedToRoomAttribute;
    private final RoomAttributesService roomAttributesService;

    @Autowired
    public RoomAttributesMapper(RoomAttributesService roomAttributesService) {
        this.roomAttributesService = roomAttributesService;
    }


    public boolean areRoomAttributesFound(List<Entity> entitiesList, HearingDetails hearingDetails, Listing listing) {
        reasonableAdjustmentIsMappedToRoomAttributes = false;
        hearingIsMappedToRoomAttribute = false;
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
            if (hearingDetails.getFacilitiesRequired().equals(listing.getRoomAttributes())) {
                hearingIsMappedToRoomAttribute = true;
            }
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

    public boolean bothAreMappedTo() {
        return reasonableAdjustmentIsMappedToRoomAttributes && hearingIsMappedToRoomAttribute;
    }
}
