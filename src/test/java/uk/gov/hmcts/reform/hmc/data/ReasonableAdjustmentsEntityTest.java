package uk.gov.hmcts.reform.hmc.data;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.model.PartyType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.hearingPartyEntityInd;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.reasonableAdjustmentsEntity;

class ReasonableAdjustmentsEntityTest {
    @Nested
    class GetClone {
        @Test
        void shouldMatchOnBasicField() {
            ReasonableAdjustmentsEntity entity = reasonableAdjustmentsEntity();
            entity.setHearingParty(hearingPartyEntityInd());
            ReasonableAdjustmentsEntity response = new ReasonableAdjustmentsEntity(entity);
            assertEquals("First reason", response.getReasonableAdjustmentCode());
            assertEquals("reference", response.getHearingParty().getPartyReference());
            assertEquals(PartyType.IND, response.getHearingParty().getPartyType());
        }
    }
}
