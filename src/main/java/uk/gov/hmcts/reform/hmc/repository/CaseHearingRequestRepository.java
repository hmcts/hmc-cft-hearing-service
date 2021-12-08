package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;

@Transactional(propagation = Propagation.REQUIRES_NEW)
@Repository
public interface CaseHearingRequestRepository extends CrudRepository<CaseHearingRequestEntity, Long> {

    @Query("SELECT versionNumber from CaseHearingRequestEntity where hearing.id = :hearingId")
    Integer getVersionNumber(Long hearingId);

    @Query("SELECT csr from CaseHearingRequestEntity csr LEFT JOIN FETCH csr.hearing WHERE csr.caseReference = :caseRef")
    CaseHearingRequestEntity getHearingDetails(String caseRef);
}
