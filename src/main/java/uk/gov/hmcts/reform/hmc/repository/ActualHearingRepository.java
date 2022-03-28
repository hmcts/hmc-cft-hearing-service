package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.ActualHearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;

import java.util.Optional;

@Transactional(propagation = Propagation.REQUIRES_NEW)
@Repository
public interface ActualHearingRepository extends CrudRepository<ActualHearingEntity, Long> {

    Optional<ActualHearingEntity> findByHearingResponse(HearingResponseEntity hearingResponse);
}
