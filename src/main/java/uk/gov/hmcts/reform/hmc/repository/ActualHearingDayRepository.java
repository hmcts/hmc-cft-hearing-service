package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.ActualHearingDayEntity;
import uk.gov.hmcts.reform.hmc.data.ActualHearingEntity;

import java.util.Optional;

@Transactional(propagation = Propagation.REQUIRES_NEW)
@Repository
public interface ActualHearingDayRepository extends CrudRepository<ActualHearingDayEntity, Long> {

    Optional<ActualHearingDayEntity> findByActualHearing(ActualHearingEntity actualHearing);
}
