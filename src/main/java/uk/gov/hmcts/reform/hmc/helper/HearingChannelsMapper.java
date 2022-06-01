package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingChannelsEntity;
import uk.gov.hmcts.reform.hmc.model.HearingChannel;

import java.util.ArrayList;
import java.util.List;

@Component
public class HearingChannelsMapper {

    public List<HearingChannelsEntity> modelToEntity(List<HearingChannel> hearingChannels,
                                                     CaseHearingRequestEntity caseHearingRequestEntity) {
        List<HearingChannelsEntity> hearingChannelsEntities = new ArrayList<>();
        for (HearingChannel hearingChannel: hearingChannels) {
            final HearingChannelsEntity hearingChannelsEntity = new HearingChannelsEntity();
            hearingChannelsEntity.setHearingChannelType(hearingChannel.getChannelType());
            hearingChannelsEntity.setCaseHearing(caseHearingRequestEntity);
            hearingChannelsEntities.add(hearingChannelsEntity);
        }
        return hearingChannelsEntities;
    }
}
