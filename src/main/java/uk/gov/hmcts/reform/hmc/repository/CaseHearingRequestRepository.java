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

    @Query("from CaseHearingRequestEntity chr "
            + "join (select chr2.hearing.id, max(chr2.versionNumber) as maxVersion from CaseHearingRequestEntity chr2 "
            + "where chr2.caseReference = :caseRef group by chr2.hearing.id) sub "
            + "on chr.hearing.id = sub.hearing.id and chr.versionNumber = sub.maxVersion "
            + "order by chr.hearing.id desc")
    List<CaseHearingRequestEntity> getHearingDetails(String caseRef);

    @Query("from CaseHearingRequestEntity chr "
            + "join (select chr2.hearing.id, max(chr2.versionNumber) as maxVersion from CaseHearingRequestEntity chr2 "
            + "where chr2.caseReference = :caseRef group by chr2.hearing.id) sub "
            + "on chr.hearing.id = sub.hearing.id and chr.versionNumber = sub.maxVersion "
            + "where chr.hearing.status = :status "
            + "order by chr.hearing.id desc")
    List<CaseHearingRequestEntity> getHearingDetailsWithStatus(String caseRef, String status);

    @Query("from CaseHearingRequestEntity chr "
            + "join (select max(chr2.versionNumber) as maxVersion from CaseHearingRequestEntity chr2 "
            + "where chr2.hearing.id = :hearingId) sub "
            + "on chr.versionNumber = sub.maxVersion and chr.hearing.id = :hearingId")
    CaseHearingRequestEntity getLatestCaseHearingRequest(Long hearingId);

    @Query("select count(hmctsServiceCode) from CaseHearingRequestEntity where hmctsServiceCode = :hmctsServiceCode")
    Long getHmctsServiceCodeCount(String hmctsServiceCode);
}
