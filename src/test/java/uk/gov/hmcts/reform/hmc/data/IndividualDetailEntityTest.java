package uk.gov.hmcts.reform.hmc.data;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.hmc.utils.TestingUtil.individualDetailEntity;

class IndividualDetailEntityTest {

    @Nested
    class GetClone {
        @Test
        void shouldMatchOnBasicField() {
            IndividualDetailEntity individualDetailEntity = individualDetailEntity();
            IndividualDetailEntity response = new IndividualDetailEntity(individualDetailEntity);
            assertEquals("details", response.getVulnerabilityDetails());
            assertEquals("english", response.getInterpreterLanguage());
            assertEquals("channelType", response.getChannelType());
            assertEquals("custodyStatus", response.getCustodyStatus());
            assertEquals("bloggs", response.getLastName());
            assertEquals("joe", response.getFirstName());
        }
    }
}
