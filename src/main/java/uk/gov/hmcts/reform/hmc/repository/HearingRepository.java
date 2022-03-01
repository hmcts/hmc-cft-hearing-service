package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Transactional(propagation = Propagation.REQUIRES_NEW)
@Repository
public interface HearingRepository extends CrudRepository<HearingEntity, Long> {

    @Query("SELECT status from HearingEntity where id = :hearingId")
    String getStatus(Long hearingId);

    @Query("select hr.id from HearingDayDetailsEntity hdd "
        +"INNER JOIN HearingResponseEntity hr on hr.id = hdd.hearingResponse "
        +"INNER JOIN CaseHearingRequestEntity csr on hr.id = csr.caseHearingID "
        +"where csr.hmctsServiceID = :hmctsServiceCode "
        +"GROUP BY hr.hearing.id "
        +"having MIN(hdd.startDateTime) >= :hearingStartDateFrom and "
        +"MAX(hdd.endDateTime) <= :hearingStartDateTo")
    List<String> getUnNotifiedHearings(String hmctsServiceCode, LocalDate hearingStartDateFrom, LocalDate hearingStartDateTo);
}

