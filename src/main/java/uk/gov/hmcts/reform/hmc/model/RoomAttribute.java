package uk.gov.hmcts.reform.hmc.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomAttribute {

    private String roomAttributeCode;
    private String roomAttributeName;
    private String reasonableAdjustmentCode;
    private boolean facility;
}
