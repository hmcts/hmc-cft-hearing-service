package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingDetailsAudit;

import java.util.List;

@Transactional(propagation = Propagation.REQUIRES_NEW)
@Repository
public interface LinkedHearingDetailsRepository extends CrudRepository<LinkedHearingDetailsAudit, Long> {

    @Query("from LinkedHearingDetailsAudit lhd WHERE lhd.hearing.id = :hearingId ")
    LinkedHearingDetailsAudit getLinkedHearingDetailsByHearingId(Long hearingId);

    @Query("from LinkedHearingDetailsAudit lhd WHERE lhd.linkedGroup.requestId = :requestId ")
    List<LinkedHearingDetailsAudit> getLinkedHearingDetailsByRequestId(String requestId);
}
