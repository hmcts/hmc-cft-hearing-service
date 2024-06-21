package uk.gov.hmcts.reform.hmc.data;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.caseHearingRequestEntityWithPartyOrg;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.hearingChannelsEntity;

class HearingChannelsEntityTest {

    @Nested
    class GetClone {
        @Test
        void shouldMatchOnBasicField() {
            HearingChannelsEntity contactDetailsEntity = hearingChannelsEntity().get(0);
            contactDetailsEntity.setCaseHearing(caseHearingRequestEntityWithPartyOrg());
            HearingChannelsEntity response = new HearingChannelsEntity(contactDetailsEntity);
            assertEquals("someChannelType", response.getHearingChannelType());
            assertEquals("TEST", response.getCaseHearing().getHmctsServiceCode());
            assertEquals("12345", response.getCaseHearing().getCaseReference());
            assertEquals("Some hearing type", response.getCaseHearing().getHearingType());
        }
    }
}
