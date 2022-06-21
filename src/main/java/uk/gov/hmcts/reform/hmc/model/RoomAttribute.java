package uk.gov.hmcts.reform.hmc.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomAttribute {

    private String roomAttributeCode;
    private String roomAttributeName;
    private String reasonableAdjustmentCode;
    private boolean facility;

    @JsonCreator
    public RoomAttribute(@JsonProperty(value = "roomAttributeCode", required = true)
                                 String roomAttributeCode,
                         @JsonProperty(value = "roomAttributeName", required = true)
                                 String roomAttributeName,
                         @JsonProperty(value = "reasonableAdjustmentCode", required = true)
                                 String reasonableAdjustmentCode,
                         @JsonProperty(value = "facility", required = true)
                                 boolean facility) {
        this.roomAttributeCode = roomAttributeCode;
        this.roomAttributeName = roomAttributeName;
        this.reasonableAdjustmentCode = reasonableAdjustmentCode;
        this.facility = facility;
    }
}
