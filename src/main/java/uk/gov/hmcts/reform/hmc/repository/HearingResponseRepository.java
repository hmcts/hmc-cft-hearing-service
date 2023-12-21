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

    @Query("from HearingResponseEntity hre where hre.hearing.id = :hearingId"
            + " and hre.requestVersion = :requestVersion"
            + " and hre.requestTimeStamp = :receivedDateTime")
    HearingResponseEntity getHearingResponse(Long hearingId, Integer requestVersion,
                                             LocalDateTime receivedDateTime);

    @Query("FROM HearingResponseEntity hre WHERE hre.hearing.id = :hearingId "
        + "AND hre.partiesNotifiedDateTime is NOT NULL")
    List<HearingResponseEntity> getPartiesNotified(Long hearingId);

    @Query("select hr.hearing.id FROM HearingResponseEntity hr "
        + "join HearingDayDetailsEntity hdd ON hr.hearingResponseId = hdd.hearingResponse.hearingResponseId "
        + "join CaseHearingRequestEntity csr ON hr.hearing.id = csr.hearing.id "
        + "join (select csr.hearing.id as hearingId, max(csr.versionNumber) as maxHearingRequestVersion "
        + "from CaseHearingRequestEntity csr where csr.hmctsServiceCode = :hmctsServiceCode "
        + "group BY csr.hearing.id ) mrv ON csr.hearing.id = mrv.hearingId "
        + "and csr.versionNumber = mrv.maxHearingRequestVersion "
        + "where csr.hmctsServiceCode = :hmctsServiceCode "
        + "and hr.requestVersion = mrv.maxHearingRequestVersion "
        + "and hr.partiesNotifiedDateTime IS NULL "
        + "group BY hr.hearing.id "
        + "having min(hdd.startDateTime) >= :hearingStartDateFrom")
    Page<Long> getUnNotifiedHearingsWithOutStartDateTo(String hmctsServiceCode, LocalDateTime hearingStartDateFrom,
                                                       Pageable pageable);

    @Query("select hr.hearing.id FROM HearingResponseEntity hr "
        + "join HearingDayDetailsEntity hdd ON hr.hearingResponseId = hdd.hearingResponse.hearingResponseId "
        + "join CaseHearingRequestEntity csr ON hr.hearing.id = csr.hearing.id "
        + "join MaxHearingRequestVersionView mrv ON csr.hearing.id = mrv.hearingId "
        + "and csr.versionNumber = mrv.maxHearingRequestVersion "
        + "and hmcts_service_code = :hmctsServiceCode "
        + "where csr.hmctsServiceCode = :hmctsServiceCode "
        + "and hr.requestVersion = mrv.maxHearingRequestVersion "
        + "and hr.partiesNotifiedDateTime IS NULL "
        + "group BY hr.hearing.id "
        + "having min(hdd.startDateTime) >= :hearingStartDateFrom "
        + "and max(hdd.endDateTime)<=:hearingStartDateTo")
    Page<Long> getUnNotifiedHearingsWithStartDateTo(String hmctsServiceCode, LocalDateTime hearingStartDateFrom,
                                                    LocalDateTime hearingStartDateTo, Pageable pageable);

}
