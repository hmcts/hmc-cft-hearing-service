package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;

import java.time.LocalDateTime;
import java.util.List;

@Transactional(propagation = Propagation.REQUIRES_NEW)
@Repository
public interface HearingDayDetailsRepository extends JpaRepository<HearingDayDetailsEntity, Long> {

    @Query("select hr.hearing.id from HearingDayDetailsEntity hdd "
        + "JOIN hdd.hearingResponse hr "
        + "INNER JOIN CaseHearingRequestEntity csr on hr.id = csr.caseHearingID "
        + "where csr.hmctsServiceID = :hmctsServiceCode "
        + "GROUP BY hr.hearing.id, hr.requestVersion, hr.partiesNotifiedDateTime "
        + "having MIN(hdd.startDateTime) >= :hearingStartDateFrom and "
        + "MAX(csr.versionNumber)= hr.requestVersion and "
        + "hr.partiesNotifiedDateTime is NULL")
    List<String> getUnNotifiedHearingsWithOutStartDateTo(String hmctsServiceCode, LocalDateTime hearingStartDateFrom,
                                                         Pageable pageable);

    @Query("select hr.hearing.id from HearingDayDetailsEntity hdd "
        + "JOIN hdd.hearingResponse hr "
        + "INNER JOIN CaseHearingRequestEntity csr on hr.id = csr.caseHearingID "
        + "where csr.hmctsServiceID = :hmctsServiceCode "
        + "GROUP BY hr.hearing.id , hr.requestVersion, hr.partiesNotifiedDateTime "
        + "having MIN(hdd.startDateTime) >= :hearingStartDateFrom and "
        + "MAX(hdd.endDateTime) <= :hearingStartDateTo and "
        + "MAX(csr.versionNumber)=hr.requestVersion and "
        + "hr.partiesNotifiedDateTime is NULL")
    List<String> getUnNotifiedHearingsWithStartDateTo(String hmctsServiceCode, LocalDateTime hearingStartDateFrom,
                                                      LocalDateTime hearingStartDateTo, Pageable pageable);

    @Query("select count(hr.hearing.id) from HearingDayDetailsEntity hdd "
        + "JOIN hdd.hearingResponse hr "
        + "INNER JOIN CaseHearingRequestEntity csr on hr.id = csr.caseHearingID "
        + "where csr.hmctsServiceID = :hmctsServiceCode "
        + "GROUP BY hr.hearing.id , hr.requestVersion, hr.partiesNotifiedDateTime "
        + "having MIN(hdd.startDateTime) >= :hearingStartDateFrom and "
        + "MAX(hdd.endDateTime) <= :hearingStartDateTo and "
        + "MAX(csr.versionNumber)=hr.requestVersion and "
        + "hr.partiesNotifiedDateTime is NULL")
    Long getUnNotifiedHearingsTotalCountWithStartDateTo(String hmctsServiceCode, LocalDateTime hearingStartDateFrom,
                                                        LocalDateTime hearingStartDateTo);

    @Query("select count(hr.hearing.id) from HearingDayDetailsEntity hdd "
        + "JOIN hdd.hearingResponse hr "
        + "INNER JOIN CaseHearingRequestEntity csr on hr.id = csr.caseHearingID "
        + "where csr.hmctsServiceID = :hmctsServiceCode "
        + "GROUP BY hr.hearing.id , hr.requestVersion, hr.partiesNotifiedDateTime "
        + "having MIN(hdd.startDateTime) >= :hearingStartDateFrom and "
        + "MAX(csr.versionNumber)=hr.requestVersion and "
        + "hr.partiesNotifiedDateTime is NULL")
    Long getUnNotifiedHearingsTotalCountWithOutStartDateTo(String hmctsServiceCode, LocalDateTime hearingStartDateFrom);
}
