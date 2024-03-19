package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingStatusAuditEntity;

import java.util.List;

@Transactional(propagation = Propagation.REQUIRED)
@Repository
public interface HearingStatusAuditRepository extends JpaRepository<HearingStatusAuditEntity, Long> {

    @Query("from HearingStatusAuditEntity hs where hs.hearingId = :hearingId")
    List<HearingStatusAuditEntity> findByHearingId(String hearingId);

}
