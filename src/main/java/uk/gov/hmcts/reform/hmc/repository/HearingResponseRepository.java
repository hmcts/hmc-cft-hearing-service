package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

@Transactional(propagation = Propagation.REQUIRES_NEW)
@Repository
public interface HearingResponseRepository extends JpaRepository<HearingResponseEntity, Long> {

    @Query("from HearingResponseEntity hre where hre.hearing.id = :hearingId")
    HearingResponseEntity getHearingResponse(Long hearingId);

    @Query("FROM HearingResponseEntity hre WHERE hre.hearing.id = :hearingId "
        + "AND hre.partiesNotifiedDateTime is NOT NULL")
    List<HearingResponseEntity> getPartiesNotified(Long hearingId);

    @Query("select distinct(hr.hearing.id) from HearingResponseEntity hr "
        + "INNER JOIN HearingDayDetailsEntity hdd on hdd.hearingResponse.hearingResponseId = hr.hearingResponseId "
        + "INNER JOIN CaseHearingRequestEntity csr on hr.id = csr.caseHearingID "
        + "where csr.hmctsServiceID = :hmctsServiceCode "
        + "and hr.partiesNotifiedDateTime is NULL "
        + "and hr.requestVersion = (select MAX(versionNumber) from CaseHearingRequestEntity csr "
        + "where hr.hearing.id = csr.hearing.id) "
        + "GROUP BY hr.hearing.id "
        + "having MIN(hdd.startDateTime) >= :hearingStartDateFrom")
    Page<Long> getUnNotifiedHearingsWithOutStartDateTo(String hmctsServiceCode, LocalDateTime hearingStartDateFrom,
                                                       Pageable pageable);

    @Query("select distinct(hr.hearing.id) from HearingResponseEntity hr "
        + "INNER JOIN HearingDayDetailsEntity hdd on hdd.hearingResponse.hearingResponseId = hr.hearingResponseId "
        + "INNER JOIN CaseHearingRequestEntity csr on hr.id = csr.caseHearingID "
        + "where csr.hmctsServiceID = :hmctsServiceCode "
        + "and hr.partiesNotifiedDateTime is NULL "
        + "and hr.requestVersion = (select MAX(versionNumber) from CaseHearingRequestEntity csr "
        + "where hr.hearing.id = csr.hearing.id) "
        + "GROUP BY hr.hearing.id "
        + "having MIN(hdd.startDateTime) >= :hearingStartDateFrom "
        + "and MAX(hdd.endDateTime) <= :hearingStartDateTo")
    Page<Long> getUnNotifiedHearingsWithStartDateTo(String hmctsServiceCode, LocalDateTime hearingStartDateFrom,
                                                    LocalDateTime hearingStartDateTo, Pageable pageable);

}
