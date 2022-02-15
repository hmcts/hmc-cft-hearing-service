package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.NonStandardDurationsEntity;
import uk.gov.hmcts.reform.hmc.repository.CancellationReasonsRepository;
import uk.gov.hmcts.reform.hmc.repository.NonStandardDurationsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.beans.BeanUtils.copyProperties;

@Component
public class NonStandardDurationsMapper {

    private final NonStandardDurationsRepository repository;

    public NonStandardDurationsMapper(NonStandardDurationsRepository repository) {
        this.repository = repository;
    }

    public List<NonStandardDurationsEntity> modelToEntity(List<String> durations,
                                                          CaseHearingRequestEntity caseHearingRequestEntity) {
        List<NonStandardDurationsEntity> nonStandardDurationsEntities = new ArrayList<>();
        for (String duration : durations) {
            final NonStandardDurationsEntity nonStandardDurationEntity = new NonStandardDurationsEntity();
            nonStandardDurationEntity.setNonStandardHearingDurationReasonType(duration);
            nonStandardDurationEntity.setCaseHearing(caseHearingRequestEntity);
            nonStandardDurationsEntities.add(nonStandardDurationEntity);
        }
        return nonStandardDurationsEntities;
    }


    /*public List<NonStandardDurationsEntity> modelToEntity1(List<String> durations,
                                                          CaseHearingRequestEntity caseHearingRequestEntity) {
        NonStandardDurationsEntity nonStandardDurationEntity = null;
        List<NonStandardDurationsEntity> nonStandardDurationsEntities = new ArrayList<>();

        for (NonStandardDurationsEntity entity : caseHearingRequestEntity.getNonStandardDurations()) {
            if (entity.getId() == null) {
                nonStandardDurationEntity = new NonStandardDurationsEntity();
                nonStandardDurationEntity.setNonStandardHearingDurationReasonType(duration);
                nonStandardDurationEntity.setCaseHearing(caseHearingRequestEntity);
                nonStandardDurationsEntities.add(nonStandardDurationEntity);
            } else {
                Optional<NonStandardDurationsEntity> durationResult = repository.findById(entity.getId());
                if (durationResult.isPresent()) {
                    NonStandardDurationsEntity durationEntity = durationResult.get();
                }
            }

            nonStandardDurationEntity.setNonStandardHearingDurationReasonType(duration);
            nonStandardDurationEntity.setCaseHearing(caseHearingRequestEntity);
            nonStandardDurationsEntities.add(savedEntity);
        }
        return nonStandardDurationsEntities;
    }*/

}
