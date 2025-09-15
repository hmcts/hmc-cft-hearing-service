package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.PendingRequestEntity;

@Transactional(propagation = Propagation.REQUIRES_NEW)
@Repository("pendingRequestRepository")
public interface PendingRequestRepository extends CrudRepository<PendingRequestEntity, Long> {
}
