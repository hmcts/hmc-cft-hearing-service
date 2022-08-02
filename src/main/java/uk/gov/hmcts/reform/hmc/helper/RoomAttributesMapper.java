package uk.gov.hmcts.reform.hmc.helper;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
public class RoomAttributesMapper {

    private boolean mappedTo;
    private boolean hearingFacilitiesMappedToRoomAttributes;
    private boolean reasonableAdjustmentIsMappedToRoomAttributeCode;

    public void initialize() {
        this.setMappedTo(false);
        this.setHearingFacilitiesMappedToRoomAttributes(false);
        this.setReasonableAdjustmentIsMappedToRoomAttributeCode(false);
    }
}
