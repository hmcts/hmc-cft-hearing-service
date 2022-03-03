package uk.gov.hmcts.reform.hmc.model;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class ActualDayParty {
    private final Integer actualPartyId;
    private final String partyRole;
    private final String partyChannelSubType;
    private final Boolean didNotAttendFlag;
    private final IndividualDetails individualDetails;
}
