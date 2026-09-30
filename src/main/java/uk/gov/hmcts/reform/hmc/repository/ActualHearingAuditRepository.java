package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.ActualHearingAuditEntity;

import java.util.List;

@Transactional(propagation = Propagation.REQUIRED)
@Repository
public interface ActualHearingAuditRepository extends CrudRepository<ActualHearingAuditEntity, Long> {

    @Query("from ActualHearingAuditEntity ah where ah.hearingResponseId = :hearingResponseId order by ah.id desc")
    List<ActualHearingAuditEntity> findByHearingResponseId(Long hearingResponseId);

    @Query("from ActualHearingAuditEntity ah where ah.hearingId = :hearingId order by ah.id desc")
    List<ActualHearingAuditEntity> findByHearingId(Long hearingId);

}
