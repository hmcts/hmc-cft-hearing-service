package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.ActualHearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;

import java.util.Optional;

@Transactional
@Repository
public interface ActualHearingRepository extends JpaRepository<ActualHearingEntity, Long> {

    Optional<ActualHearingEntity> findByHearingResponse(HearingResponseEntity hearingResponse);
}
