package uk.gov.hmcts.reform.hmc.model.hearingactuals;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter
@Getter
@NoArgsConstructor
public class ActualDayParty {
    private String actualPartyId;
    private String partyRole;
    private String partyChannelSubType;
    private Boolean didNotAttendFlag;
    private String representedParty;
    private ActualIndividualDetails actualIndividualDetails;
    private String actualOrganisationName;
}
