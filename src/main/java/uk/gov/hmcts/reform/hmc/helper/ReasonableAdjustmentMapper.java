package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.ReasonableAdjustmentsEntity;

import java.util.ArrayList;
import java.util.List;

@Component
public class ReasonableAdjustmentMapper {

    public ReasonableAdjustmentMapper() {
    }


    public List<ReasonableAdjustmentsEntity> modelToEntity(List<String> reasonableAdjustments,
                                                           HearingPartyEntity hearingPartyEntity) {
        List<ReasonableAdjustmentsEntity> reasonableAdjustmentsEntities = new ArrayList<>();
        for(String adjustmentReason : reasonableAdjustments) {
            final ReasonableAdjustmentsEntity reasonableAdjustmentsEntity = new ReasonableAdjustmentsEntity();
            reasonableAdjustmentsEntity.setReasonableAdjustmentCode(adjustmentReason);
            reasonableAdjustmentsEntity.setHearingParty(hearingPartyEntity);
            reasonableAdjustmentsEntities.add(reasonableAdjustmentsEntity);
        }
        return reasonableAdjustmentsEntities;
    }
}
