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
}
