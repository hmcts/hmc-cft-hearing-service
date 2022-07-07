package uk.gov.hmcts.reform.hmc.data;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.model.PartyType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.hearingPartyEntityOrg;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.organisationDetailEntity;

class OrganisationDetailEntityTest {

    @Nested
    class GetClone {
        @Test
        void shouldMatchOnBasicField() {
            OrganisationDetailEntity entity = organisationDetailEntity();
            entity.setHearingParty(hearingPartyEntityOrg());
            OrganisationDetailEntity response = new OrganisationDetailEntity(entity);
            assertEquals("name", response.getOrganisationName());
            assertEquals("code", response.getOrganisationTypeCode());
            assertEquals("reference", response.getHmctsOrganisationReference());
            assertEquals(PartyType.ORG, response.getHearingParty().getPartyType());
            assertEquals("role", response.getHearingParty().getPartyRoleType());
        }
    }
}
