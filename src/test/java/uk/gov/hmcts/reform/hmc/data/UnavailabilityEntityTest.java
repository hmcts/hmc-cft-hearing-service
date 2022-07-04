package uk.gov.hmcts.reform.hmc.data;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.model.DayOfWeekUnAvailableType;
import uk.gov.hmcts.reform.hmc.model.DayOfWeekUnavailable;
import uk.gov.hmcts.reform.hmc.model.PartyType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.hmc.constants.Constants.UNAVAILABILITY_DOW_TYPE;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.hearingPartyEntityInd;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.unavailabilityEntity;

class UnavailabilityEntityTest {

    @Nested
    class GetClone {
        @Test
        void shouldMatchOnBasicField() {
            UnavailabilityEntity entity = unavailabilityEntity().get(0);
            entity.setHearingParty(hearingPartyEntityInd());
            UnavailabilityEntity response = new UnavailabilityEntity(entity);
            assertEquals(DayOfWeekUnavailable.FRIDAY, response.getDayOfWeekUnavailable());
            assertEquals(DayOfWeekUnAvailableType.ALL, response.getDayOfWeekUnavailableType());
            assertEquals(UNAVAILABILITY_DOW_TYPE, response.getUnAvailabilityType());
            assertEquals("reference", response.getHearingParty().getPartyReference());
            assertEquals(PartyType.IND, response.getHearingParty().getPartyType());
        }
    }
}
