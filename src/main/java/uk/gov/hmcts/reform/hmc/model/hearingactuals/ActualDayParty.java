package uk.gov.hmcts.reform.hmc.model.hearingactuals;


import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.hmc.model.IndividualDetails;
import uk.gov.hmcts.reform.hmc.model.OrganisationDetails;

import java.util.List;

@Data
@Setter
@Getter
@NoArgsConstructor
public class ActualDayParty {
    private Integer actualPartyId;
    private String partyRole;
    private String partyChannelSubType;
    private Boolean didNotAttendFlag;
    private String representedParty;
    private List<IndividualDetails> actualIndividualDetails;
    private List<OrganisationDetails> actualOrganisationDetails;
}
