package uk.gov.hmcts.reform.hmc.data;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.partyRelationshipDetailsEntity;

class PartyRelationshipDetailsEntityTest {

    @Nested
    class GetClone {
        @Test
        void shouldMatchOnBasicField() {
            PartyRelationshipDetailsEntity entity = partyRelationshipDetailsEntity("P1", "A");
            PartyRelationshipDetailsEntity response = new PartyRelationshipDetailsEntity(entity);
            assertEquals("P1", response.getTargetTechParty().getPartyReference());
            assertEquals("A", response.getRelationshipType());
        }
    }
}
