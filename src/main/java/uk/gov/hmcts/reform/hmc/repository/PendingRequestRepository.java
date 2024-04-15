package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.PendingRequestEntity;

import java.util.List;
import javax.persistence.LockModeType;

@Transactional(propagation = Propagation.REQUIRES_NEW)
@Repository
public interface PendingRequestRepository extends CrudRepository<PendingRequestEntity, Long> {

    @Modifying
    @Query("UPDATE PendingRequestEntity SET status = 'PROCESSING' WHERE id = :id")
    void markRequestAsProcessing(Long id);

    @Modifying
    @Query("UPDATE PendingRequestEntity SET status = 'PENDING', retryCount = :retryCount + 1, "
        + "last_tried_date_time = CURRENT_TIMESTAMP WHERE id = :id")
    void markRequestAsPendingAndBumpRetryCount(Long id);

    @Modifying
    @Query("UPDATE PendingRequestEntity SET status = 'COMPLETED' WHERE id = :id")
    void markRequestAsCompleted(Long id);

    @Modifying
    @Query("UPDATE PendingRequestEntity SET status = 'EXCEPTION' WHERE id = :id")
    void markRequestAsException(Long id);
}
