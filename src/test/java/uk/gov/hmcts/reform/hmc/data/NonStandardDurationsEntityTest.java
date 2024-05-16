package uk.gov.hmcts.reform.hmc.data;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.caseHearingRequestEntityWithPartyOrg;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.getNonStandardDurationEntities;

class NonStandardDurationsEntityTest {

    @Nested
    class GetClone {
        @Test
        void shouldMatchOnBasicField() {
            NonStandardDurationsEntity nonStandardDurationsEntity = getNonStandardDurationEntities().get(0);
            nonStandardDurationsEntity.setCaseHearing(caseHearingRequestEntityWithPartyOrg());
            NonStandardDurationsEntity response = new NonStandardDurationsEntity(nonStandardDurationsEntity);
            assertEquals("Reason", response.getNonStandardHearingDurationReasonType());
            assertEquals("TEST", response.getCaseHearing().getHmctsServiceCode());
            assertEquals("12345", response.getCaseHearing().getCaseReference());
            assertEquals("Some hearing type", response.getCaseHearing().getHearingType());
        }
    }
}
