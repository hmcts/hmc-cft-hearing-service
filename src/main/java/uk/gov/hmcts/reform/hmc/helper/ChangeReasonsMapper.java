package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.ChangeReasonsEntity;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChangeReasonsMapper {

    public List<ChangeReasonsEntity> modelToEntity(List<String> changeReasons,
                                                   CaseHearingRequestEntity caseHearingRequestEntity) {
        if (changeReasons == null) {
            return Collections.emptyList();
        } else {
            return changeReasons.stream()
                    .map(changeReason -> {
                        ChangeReasonsEntity entity = new ChangeReasonsEntity();
                        entity.setChangeReasonType(changeReason);
                        entity.setCaseHearing(caseHearingRequestEntity);
                        return entity;
                    })
                    .collect(Collectors.toList());
        }
    }
}
