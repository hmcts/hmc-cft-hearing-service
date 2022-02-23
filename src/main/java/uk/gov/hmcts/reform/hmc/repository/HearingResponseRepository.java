package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;

@Transactional(propagation = Propagation.REQUIRES_NEW)
@Repository
public interface HearingResponseRepository extends JpaRepository<HearingResponseEntity, Long> {

    @Query("from HearingResponseEntity hre where hre.hearing.id = :hearingId")
    HearingResponseEntity getHearingResponse(Long hearingId);

}
