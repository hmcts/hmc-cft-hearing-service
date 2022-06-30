package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingChannelsEntity;
import uk.gov.hmcts.reform.hmc.utils.TestingUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HearingChannelsMapperTest {

    @Test
    void modelToEntityTest() {
        HearingChannelsMapper mapper = new HearingChannelsMapper();
        List<String> hearingChannels = TestingUtil.getHearingChannelsList();
        CaseHearingRequestEntity caseHearingRequestEntity = new CaseHearingRequestEntity();
        List<HearingChannelsEntity> entities = mapper.modelToEntity(hearingChannels,caseHearingRequestEntity);

        assertAll(
            () -> assertEquals(2, entities.size()),
            () -> assertTrue(entities.get(0).getHearingChannelType().contains("someChannelType")),
            () -> assertTrue(entities.get(1).getHearingChannelType().contains("someOtherChannelType"))
        );
    }
}
