package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;

import java.time.LocalDateTime;
import java.util.List;

@Transactional(propagation = Propagation.REQUIRES_NEW)
@Repository
public interface HearingRepository extends JpaRepository<HearingEntity, Long> {

    @Query("SELECT status from HearingEntity where id = :hearingId")
    String getStatus(Long hearingId);

    @Query("select hr.hearing.id from HearingDayDetailsEntity hdd "
        + "INNER JOIN HearingResponseEntity hr on hr.id = hdd.hearingResponse "
        + "INNER JOIN CaseHearingRequestEntity csr on hr.id = csr.caseHearingID "
        + "where csr.hmctsServiceID = :hmctsServiceCode "
        + "GROUP BY hr.hearing.id "
        + "having MIN(hdd.startDateTime) >= :hearingStartDateFrom")
    List<String> getUnNotifiedHearings(String hmctsServiceCode, LocalDateTime hearingStartDateFrom, Pageable pageable);

    @Query("select hr.hearing.id from HearingDayDetailsEntity hdd "
        + "INNER JOIN HearingResponseEntity hr on hr.id = hdd.hearingResponse "
        + "INNER JOIN CaseHearingRequestEntity csr on hr.id = csr.caseHearingID "
        + "where csr.hmctsServiceID = :hmctsServiceCode "
        + "GROUP BY hr.hearing.id "
        + "having MIN(hdd.startDateTime) >= :hearingStartDateFrom and "
        + "MAX(hdd.endDateTime) <= :hearingStartDateTo")
    List<String> getUnNotifiedHearingsWithStartDateTo(String hmctsServiceCode, LocalDateTime hearingStartDateFrom,
                                                      LocalDateTime hearingStartDateTo, Pageable pageable);

    @Query("select count(*) from select hr.hearing.id from HearingDayDetailsEntity hdd "
        + "INNER JOIN HearingResponseEntity hr on hr.id = hdd.hearingResponse "
        + "INNER JOIN CaseHearingRequestEntity csr on hr.id = csr.caseHearingID "
        + "where csr.hmctsServiceID = :hmctsServiceCode "
        + "GROUP BY hr.hearing.id "
        + "having MIN(hdd.startDateTime) >= :hearingStartDateFrom and "
        + "MAX(hdd.endDateTime) <= :hearingStartDateTo cnt")
    Long getUnNotifiedHearingsTotalCountWithStartDateTo(String hmctsServiceCode, LocalDateTime hearingStartDateFrom,
                                                          LocalDateTime hearingStartDateTo);

    @Query("select count(*) from select hr.hearing.id from HearingDayDetailsEntity hdd "
        + "INNER JOIN HearingResponseEntity hr on hr.id = hdd.hearingResponse "
        + "INNER JOIN CaseHearingRequestEntity csr on hr.id = csr.caseHearingID "
        + "where csr.hmctsServiceID = :hmctsServiceCode "
        + "GROUP BY hr.hearing.id "
        + "having MIN(hdd.startDateTime) >= :hearingStartDateFrom cnt")
    Long getUnNotifiedHearingsTotalCount(String hmctsServiceCode, LocalDateTime hearingStartDateFrom);
}

