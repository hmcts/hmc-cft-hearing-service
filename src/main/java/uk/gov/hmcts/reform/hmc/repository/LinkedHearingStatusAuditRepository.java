package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingStatusAuditEntity;

import java.util.List;

@Transactional(propagation = Propagation.REQUIRED)
@Repository
public interface LinkedHearingStatusAuditRepository extends JpaRepository<LinkedHearingStatusAuditEntity, Long> {

    @Query("from LinkedHearingStatusAuditEntity lhs where lhs.linkedHearingGroupId = :hearingId order by lhs.id desc")
    List<LinkedHearingStatusAuditEntity> findByLinkedHearingId(String hearingId);

}
