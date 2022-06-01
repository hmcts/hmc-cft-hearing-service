package uk.gov.hmcts.reform.hmc.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingChannelsEntity;
import uk.gov.hmcts.reform.hmc.model.HearingChannel;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HearingChannelsMapperTest {

    @Test
    void modelToEntityTest() {
        HearingChannelsMapper mapper = new HearingChannelsMapper();
        List<HearingChannel> hearingChannels = getHearingChannels();
        CaseHearingRequestEntity caseHearingRequestEntity = new CaseHearingRequestEntity();
        List<HearingChannelsEntity> entities = mapper.modelToEntity(hearingChannels,caseHearingRequestEntity);

        assertAll(
            () -> assertEquals(2, entities.size()),
            () -> assertEquals("someChannelType", entities.get(0).getHearingChannelType()),
            () -> assertEquals("someOtherChannelType", entities.get(1).getHearingChannelType())
        );
    }

    private List<HearingChannel> getHearingChannels() {
        HearingChannel channel1 = new HearingChannel();
        channel1.setChannelType("someChannelType");
        HearingChannel channel2 = new HearingChannel();
        channel2.setChannelType("someOtherChannelType");
        return List.of(channel1,channel2);
    }
}
