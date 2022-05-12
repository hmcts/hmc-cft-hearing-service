package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.model.RelatedParty;
import uk.gov.hmcts.reform.hmc.model.hmi.RelatedEntity;

import java.util.ArrayList;
import java.util.List;

@Component
class RelatedEntitiesMapper {

    public List<RelatedEntity> getRelatedEntities(List<RelatedParty> relatedParties) {
        List<RelatedEntity> relatedEntities = new ArrayList<>();
        if (relatedParties != null) {
            for (RelatedParty relatedParty : relatedParties) {
                RelatedEntity relatedEntity = RelatedEntity.builder()
                        .relatedEntityId(relatedParty.getRelatedPartyID())
                        .relatedEntityRelationshipType(relatedParty.getRelationshipType())
                        .build();
                relatedEntities.add(relatedEntity);
            }
        }
        return relatedEntities;
    }
}