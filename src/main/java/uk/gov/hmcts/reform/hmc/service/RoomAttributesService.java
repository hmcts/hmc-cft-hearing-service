package uk.gov.hmcts.reform.hmc.service;

import uk.gov.hmcts.reform.hmc.model.RoomAttribute;

import java.util.Optional;

public interface RoomAttributesService {

    Optional<RoomAttribute> findByReasonableAdjustmentCode(String reasonableAdjustmentCode);

    Optional<RoomAttribute> findByRoomAttributeCode(String roomAttributeCode);
}
