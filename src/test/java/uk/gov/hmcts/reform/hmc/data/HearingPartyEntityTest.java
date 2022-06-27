package uk.gov.hmcts.reform.hmc.data;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HearingPartyEntityTest {

    @Nested
    class GetClone {
        @Test
        void shouldMatchOnBasicField() {
            HearingPartyEntity hearingPartyEntity = TestingUtil.hearingPartyEntityForClone();
            HearingPartyEntity response = new HearingPartyEntity(hearingPartyEntity);
            assertEquals(2, response.getUnavailabilityEntity().size());
            assertEquals(1, response.getReasonableAdjustmentsEntity().size());
            assertEquals(1, response.getContactDetailsEntity().size());
            assertEquals(2, response.getPartyRelationshipDetailsEntity().size());
            assertEquals("name", response.getOrganisationDetailEntity().getOrganisationName());
            assertEquals("code", response.getOrganisationDetailEntity().getOrganisationTypeCode());
            assertEquals("reference", response.getOrganisationDetailEntity().getHmctsOrganisationReference());
        }
    }

    @Test
    void testClone() throws CloneNotSupportedException {
        HearingPartyEntity hearingPartyEntity = TestingUtil.hearingPartyEntityInd();
        HearingPartyEntity cloned = (HearingPartyEntity) hearingPartyEntity.clone();
        assertEquals(2, cloned.getPartyRelationshipDetailsEntity().size());
        assertEquals("A", cloned.getPartyRelationshipDetailsEntity().get(0).getRelationshipType());
        assertEquals("P1", cloned.getPartyRelationshipDetailsEntity().get(0)
            .getTargetTechParty().getPartyReference());
        assertEquals("B", cloned.getPartyRelationshipDetailsEntity().get(1).getRelationshipType());
        assertEquals("P2", cloned.getPartyRelationshipDetailsEntity().get(1)
            .getTargetTechParty().getPartyReference());

    }

    @Test
    void testCloneForReasonableAdjustmentsAndContactDetails() throws CloneNotSupportedException {
        HearingPartyEntity hearingPartyEntity = TestingUtil.hearingPartyEntityForClone();
        HearingPartyEntity cloned = (HearingPartyEntity) hearingPartyEntity.clone();
        assertEquals(1, cloned.getReasonableAdjustmentsEntity().size());
        assertEquals("A", cloned.getPartyRelationshipDetailsEntity().get(0).getRelationshipType());
        assertEquals(1, cloned.getContactDetailsEntity().size());
        assertEquals("email", cloned.getContactDetailsEntity().get(0).getContactType());
        assertEquals("hearing.channel@email.com", cloned.getContactDetailsEntity().get(0).getContactDetails());
    }
}
