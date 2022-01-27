package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.model.RelatedParty;
import uk.gov.hmcts.reform.hmc.model.hmi.RelatedEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RelatedEntitiesMapperTest {

    private static final String PARTY_ID = "PartyId";
    private static final String PARTY_ID_TWO = "PartyIdTwo";
    private static final String RELATIONSHIP = "Relationship";
    private static final String RELATIONSHIP_TWO = "RelationshipTwo";

    @Test
    void shouldReturnRelatedEntities() {
        RelatedParty relatedParty = new RelatedParty();
        relatedParty.setRelatedPartyID(PARTY_ID);
        relatedParty.setRelationshipType(RELATIONSHIP);
        RelatedParty relatedPartyTwo = new RelatedParty();
        relatedPartyTwo.setRelatedPartyID(PARTY_ID_TWO);
        relatedPartyTwo.setRelationshipType(RELATIONSHIP_TWO);
        List<RelatedParty> relatedPartyList = Arrays.asList(relatedParty, relatedPartyTwo);

        RelatedEntity relatedEntity = RelatedEntity.builder()
            .relatedEntityId(PARTY_ID)
            .relatedEntityRelationshipType(RELATIONSHIP)
            .build();
        RelatedEntity relatedEntityTwo = RelatedEntity.builder()
            .relatedEntityId(PARTY_ID_TWO)
            .relatedEntityRelationshipType(RELATIONSHIP_TWO)
            .build();
        List<RelatedEntity> expectedRelatedEntities = Arrays.asList(relatedEntity, relatedEntityTwo);
        RelatedEntitiesMapper relatedEntitiesMapper = new RelatedEntitiesMapper();
        List<RelatedEntity> actualRelatedEntities = relatedEntitiesMapper.getRelatedEntities(relatedPartyList);
        assertEquals(expectedRelatedEntities, actualRelatedEntities);
    }

    @Test
    void shouldHandleNullRelatedParties() {
        RelatedEntitiesMapper relatedEntitiesMapper = new RelatedEntitiesMapper();
        List<RelatedEntity> relatedEntities = relatedEntitiesMapper.getRelatedEntities(null);
        assertTrue(relatedEntities.isEmpty());
    }
}
