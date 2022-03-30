package uk.gov.hmcts.reform.hmc.model.hearingactuals;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.model.IndividualDetails;
import uk.gov.hmcts.reform.hmc.model.OrganisationDetails;

import java.util.List;

@Data
@NoArgsConstructor
public class Party {

    private String partyID;

    private String partyRole;

    private List<IndividualDetails> individualDetails;

    private OrganisationDetails organisationDetails;

    private String partyChannelSubType;
}
