package uk.gov.hmcts.reform.hmc.client.hmi;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;

import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class HearingRoom {

    private String locationIdCaseHQ;
    @Size(max = 70, message = ValidationError.HEARING_ROOM_NAME_LENGTH)
    private String locationName;
    private JsonNode locationRegion;
    private JsonNode locationCluster;
    private JsonNode locationReferences;
}
