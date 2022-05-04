package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.IndividualDetailEntity;
import uk.gov.hmcts.reform.hmc.model.IndividualDetails;

import java.util.List;

@Component
public class IndividualDetailMapper {

    public List<IndividualDetailEntity> modelToEntity(IndividualDetails individualDetail,
                                                      HearingPartyEntity hearingPartyEntity) {
        IndividualDetailEntity individualDetailEntity = new IndividualDetailEntity();
        setIndividualDetails(individualDetail, individualDetailEntity);
        individualDetailEntity.setHearingParty(hearingPartyEntity);

        return List.of(individualDetailEntity);
    }

    private void setIndividualDetails(IndividualDetails individualDetail, IndividualDetailEntity individualEntity) {
        individualEntity.setVulnerabilityDetails(individualDetail.getVulnerabilityDetails());
        individualEntity.setVulnerableFlag(individualDetail.getVulnerableFlag());
        individualEntity.setInterpreterLanguage(individualDetail.getInterpreterLanguage());
        individualEntity.setChannelType(individualDetail.getPreferredHearingChannel());
        individualEntity.setLastName(individualDetail.getLastName());
        individualEntity.setFirstName(individualDetail.getFirstName());
        individualEntity.setTitle(individualDetail.getTitle());
        individualEntity.setCustodyStatus(individualDetail.getCustodyStatus());
        individualEntity.setOtherReasonableAdjustmentDetails(individualDetail.getOtherReasonableAdjustmentDetails());
    }
}
