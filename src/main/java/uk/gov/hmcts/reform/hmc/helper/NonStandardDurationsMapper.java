package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.NonStandardDurationsEntity;

import java.util.ArrayList;
import java.util.List;

@Component
public class NonStandardDurationsMapper {

    public NonStandardDurationsMapper() {
    }

    public List<NonStandardDurationsEntity> modelToEntity(List<String> durations,
                                                          CaseHearingRequestEntity caseHearingRequestEntity) {
        List<NonStandardDurationsEntity> nonStandardDurationsEntities = new ArrayList<>();
        for(String duration : durations) {
            final NonStandardDurationsEntity nonStandardDurationEntity = new NonStandardDurationsEntity();
            nonStandardDurationEntity.setNonStandardHearingDurationReasonType(duration);
            nonStandardDurationEntity.setCaseHearing(caseHearingRequestEntity);
            nonStandardDurationsEntities.add(nonStandardDurationEntity);
        }
        return nonStandardDurationsEntities;
    }

}
