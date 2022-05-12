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

    @Query("SELECT max(versionNumber) from CaseHearingRequestEntity where hearing.id = :hearingId")
    Integer getLatestVersionNumber(Long hearingId);

    @Query("SELECT caseHearingID from CaseHearingRequestEntity where hearing.id = :hearingId")
    Long getCaseHearingId(Long hearingId);

    @Query("from CaseHearingRequestEntity chr WHERE chr.caseReference = :caseRef "
        + "and chr.versionNumber = (select max(chr2.versionNumber) from CaseHearingRequestEntity chr2 "
        + "where chr2.hearing.id = chr.hearing.id) "
        + "order by chr.hearing.id desc")
    List<CaseHearingRequestEntity> getHearingDetails(String caseRef);

    @Query("from CaseHearingRequestEntity chr WHERE chr.caseReference = :caseRef and chr.hearing.status = :status "
        + "and chr.versionNumber = (select max(chr2.versionNumber) from CaseHearingRequestEntity chr2 "
        + "where chr2.hearing.id = chr.hearing.id) "
        + "order by chr.hearing.id desc")
    List<CaseHearingRequestEntity> getHearingDetailsWithStatus(String caseRef, String status);

    @Query("from CaseHearingRequestEntity chr where hearing.id = :hearingId and chr.versionNumber = "
        + "(SELECT max(versionNumber) from CaseHearingRequestEntity where hearing.id = :hearingId)")
    CaseHearingRequestEntity getLatestCaseHearingRequest(Long hearingId);

    @Query("select count(hmctsServiceCode) from CaseHearingRequestEntity where hmctsServiceCode = :hmctsServiceCode")
    Long getHmctsServiceCodeCount(String hmctsServiceCode);
}
