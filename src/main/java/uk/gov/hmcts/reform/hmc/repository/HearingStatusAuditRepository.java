package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.HearingStatusAuditEntity;

@Transactional(propagation = Propagation.REQUIRED)
@Repository
public interface HearingStatusAuditRepository extends JpaRepository<HearingStatusAuditEntity, Long> {

}
