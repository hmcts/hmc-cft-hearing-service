package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;

import java.util.List;

@Transactional(propagation = Propagation.REQUIRES_NEW)
@Repository
public interface CaseHearingRequestRepository extends CrudRepository<CaseHearingRequestEntity, Long> {

    @Query("SELECT versionNumber from CaseHearingRequestEntity where hearing.id = :hearingId")
    Integer getVersionNumber(Long hearingId);

    @Query("from CaseHearingRequestEntity csr WHERE csr.caseReference = :caseRef")
    List<CaseHearingRequestEntity> getHearingDetails(String caseRef);

    @Query("from CaseHearingRequestEntity csr WHERE csr.caseReference = :caseRef and csr.hearing.status = :status")
    List<CaseHearingRequestEntity> getHearingDetailsWithStatus(String caseRef, String status);

}
