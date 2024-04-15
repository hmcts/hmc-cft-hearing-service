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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "SELECT * FROM pending_requests WHERE status = 'PENDING' "
        + "AND (last_tried_date_time IS NULL OR last_tried_date_time < NOW() - INTERVAL '15' MINUTE) "
        + "ORDER BY submitted_date_time ASC LIMIT 1")
    PendingRequestEntity findOldestPendingRequestForProcessing();

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

    @Query("SELECT pr FROM PendingRequestEntity pr WHERE pr.submittedDateTime < :CURRENT_TIMESTAMP "
        + "- INTERVAL '1' DAY AND pr.incidentFlag = false")
    List<PendingRequestEntity> findRequestsForEscalation();

    @Modifying
    @Query("UPDATE PendingRequestEntity SET incidentFlag = true WHERE submittedDateTime < CURRENT_TIMESTAMP "
        + "- INTERVAL '1' DAY AND incidentFlag = false")
    void identifyRequestsForEscalation();

    @Modifying
    @Query("DELETE FROM PendingRequestEntity WHERE status = 'COMPLETED' "
        + "AND submittedDateTime < CURRENT_TIMESTAMP - INTERVAL '30' DAY")
    void deleteCompletedRecords();
}
