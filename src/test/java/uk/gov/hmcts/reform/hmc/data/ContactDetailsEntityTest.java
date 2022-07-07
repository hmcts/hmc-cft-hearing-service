package uk.gov.hmcts.reform.hmc.data;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.model.PartyType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.contactDetailsEntity_Email;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.hearingPartyEntityInd;

class ContactDetailsEntityTest {
    @Nested
    class GetClone {
        @Test
        void shouldMatchOnBasicField() {
            ContactDetailsEntity contactDetailsEntity = contactDetailsEntity_Email();
            contactDetailsEntity.setHearingParty(hearingPartyEntityInd());
            ContactDetailsEntity response = new ContactDetailsEntity(contactDetailsEntity);
            assertEquals("email", response.getContactType());
            assertEquals("hearing.channel@email.com", response.getContactDetails());
            assertEquals("reference", response.getHearingParty().getPartyReference());
            assertEquals(PartyType.IND, response.getHearingParty().getPartyType());
        }
    }
}
