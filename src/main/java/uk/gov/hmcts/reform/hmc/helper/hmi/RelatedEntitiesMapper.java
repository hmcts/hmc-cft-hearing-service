package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.RelatedParty;
import uk.gov.hmcts.reform.hmc.model.hmi.RelatedEntity;

import java.util.ArrayList;
import java.util.List;

@Component
public class RelatedEntitiesMapper {

    public List<RelatedEntity> getRelatedEntities(PartyDetails partyDetails) {
        List<RelatedEntity> relatedEntities = new ArrayList<>();
        for (RelatedParty relatedParty : partyDetails.getIndividualDetails().getRelatedParties()) {
            RelatedEntity relatedEntity = RelatedEntity.builder()
                .relatedEntityId(relatedParty.getRelatedPartyID())
                .relatedEntityRelationshipType(relatedParty.getRelationshipType())
                .build();
            relatedEntities.add(relatedEntity);
        }
        return relatedEntities;
    }
}
