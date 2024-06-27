package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.PendingRequestEntity;

import java.sql.Timestamp;
import javax.persistence.LockModeType;


@Transactional(propagation = Propagation.REQUIRES_NEW)
@Repository
public interface PendingRequestRepository extends CrudRepository<PendingRequestEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "SELECT * FROM pending_requests WHERE status = 'PENDING' ORDER "
        + "BY submitted_date_time ASC LIMIT 1", nativeQuery = true)
    PendingRequestEntity findOldestPendingRequestForProcessing();

    @Modifying
    @Query("UPDATE PendingRequestEntity SET status = :status, retryCount = :retryCount WHERE id = :id")
    void updateStatusAndRetryCount(Long id, String status, int retryCount);

    @Modifying
    @Query("UPDATE PendingRequestEntity SET status = 'COMPLETED' WHERE id = :id")
    void markRequestAsCompleted(Long id);

    @Modifying
    @Query("UPDATE PendingRequestEntity SET status = 'PENDING', retryCount = :retryCount WHERE id = :id")
    void markRequestAsPending(Long id, int retryCount);

    @Modifying
    @Query("UPDATE PendingRequestEntity SET status = 'EXCEPTION' WHERE id = :id")
    void markRequestAsException(Long id);

    @Modifying
    @Query("UPDATE PendingRequestEntity SET incidentFlag = true WHERE submittedDateTime < "
        + ":thresholdDateTime AND incidentFlag = false")
    void identifyRequestsForEscalation(Timestamp thresholdDateTime);

    @Modifying
    @Query("DELETE FROM PendingRequestEntity WHERE status = 'COMPLETED' AND submittedDateTime < :thresholdDateTime")
    void deleteCompletedRecords(Timestamp thresholdDateTime);
}
