package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ActualHearingDayParties {

    private Long actualPartyId;
    private String partyRole;
    private ActualHearingDayPartyDetail individualDetails;
    private String partyChannelSubType;
    private Boolean didNotAttendFlag;
}
