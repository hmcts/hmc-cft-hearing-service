package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.IndividualDetailEntity;
import uk.gov.hmcts.reform.hmc.model.IndividualDetails;
import uk.gov.hmcts.reform.hmc.model.RelatedParty;

@Component
public class IndividualDetailMapper {

    public IndividualDetailMapper() {
    }

    public IndividualDetailEntity  modelToEntity(IndividualDetails individualDetail,
                                                  HearingPartyEntity hearingPartyEntity) {
        final IndividualDetailEntity individualDetailEntity = new IndividualDetailEntity();
            if (individualDetail.getRelatedParties() != null) {
                for (RelatedParty relatedParty : individualDetail.getRelatedParties()) {
                    individualDetailEntity.setRelatedPartyID(relatedParty.getRelatedPartyID());
                    individualDetailEntity.setRelatedPartyRelationshipType(relatedParty.getRelationshipType());
                    setIndividualDetails(individualDetail, individualDetailEntity);
                    individualDetailEntity.setHearingParty(hearingPartyEntity);
                }
            } else {
                setIndividualDetails(individualDetail, individualDetailEntity);
                individualDetailEntity.setHearingParty(hearingPartyEntity);
            }
            return individualDetailEntity;
    }

    private void setIndividualDetails(IndividualDetails individualDetail, IndividualDetailEntity individualDetailEntity) {
        individualDetailEntity.setVulnerabilityDetails(individualDetail.getVulnerabilityDetails());
        individualDetailEntity.setVulnerableFlag(individualDetail.getVulnerableFlag());
        individualDetailEntity.setInterpreterLanguage(individualDetail.getInterpreterLanguage());
        individualDetailEntity.setChannelType(individualDetail.getPreferredHearingChannel());
        individualDetailEntity.setLastName(individualDetail.getLastName());
        individualDetailEntity.setFirstName(individualDetail.getFirstName());
        individualDetailEntity.setTitle(individualDetail.getTitle());
    }
}
