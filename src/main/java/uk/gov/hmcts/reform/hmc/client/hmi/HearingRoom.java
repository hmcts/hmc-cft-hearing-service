package uk.gov.hmcts.reform.hmc.client.hmi;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class HearingRoom {

    private String locationIdCaseHQ;
    @Size(max = 70, message = ValidationError.HEARING_ROOM_NAME_LENGTH)
    private String roomName;
    private Object roomLocationRegion;
    private Object roomLocationCluster;
    private Object roomLocationReference;
}
