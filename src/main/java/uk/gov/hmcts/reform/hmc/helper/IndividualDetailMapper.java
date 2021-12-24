package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.IndividualDetailEntity;
import uk.gov.hmcts.reform.hmc.model.IndividualDetails;
import uk.gov.hmcts.reform.hmc.model.RelatedParty;

import java.util.ArrayList;
import java.util.List;

@Component
public class IndividualDetailMapper {

    public List<IndividualDetailEntity> modelToEntity(IndividualDetails individualDetail,
                                                      HearingPartyEntity hearingPartyEntity) {
        List<IndividualDetailEntity> individualDetailEntities = new ArrayList<>();
        if (individualDetail.getRelatedParties() != null) {
            for (RelatedParty relatedParty : individualDetail.getRelatedParties()) {
                IndividualDetailEntity individualDetailEntity = new IndividualDetailEntity();
                individualDetailEntity.setRelatedPartyID(relatedParty.getRelatedPartyID());
                individualDetailEntity.setRelatedPartyRelationshipType(relatedParty.getRelationshipType());
                setIndividualDetails(individualDetail, individualDetailEntity);
                individualDetailEntity.setHearingParty(hearingPartyEntity);
                individualDetailEntities.add(individualDetailEntity);
            }
        } else {
            IndividualDetailEntity individualDetailEntity = new IndividualDetailEntity();
            setIndividualDetails(individualDetail, individualDetailEntity);
            individualDetailEntity.setHearingParty(hearingPartyEntity);
            individualDetailEntities.add(individualDetailEntity);
        }
        return individualDetailEntities;
    }

    private void setIndividualDetails(IndividualDetails individualDetail, IndividualDetailEntity individualEntity) {
        individualEntity.setVulnerabilityDetails(individualDetail.getVulnerabilityDetails());
        individualEntity.setVulnerableFlag(individualDetail.getVulnerableFlag());
        individualEntity.setInterpreterLanguage(individualDetail.getInterpreterLanguage());
        individualEntity.setChannelType(individualDetail.getPreferredHearingChannel());
        individualEntity.setLastName(individualDetail.getLastName());
        individualEntity.setFirstName(individualDetail.getFirstName());
        individualEntity.setTitle(individualDetail.getTitle());
    }
}
