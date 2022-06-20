package uk.gov.hmcts.reform.hmc.service;

import uk.gov.hmcts.reform.hmc.model.RoomAttribute;

public interface RoomAttributesService {

    RoomAttribute findByReasonableAdjustmentCode(String reasonableAdjustmentCode);

    RoomAttribute findByRoomAttributeCode(String roomAttributeCode);
}
