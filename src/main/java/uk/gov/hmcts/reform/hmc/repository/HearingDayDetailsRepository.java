package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;

import java.util.List;

@Transactional(propagation = Propagation.REQUIRES_NEW)
@Repository
public interface HearingDayDetailsRepository extends CrudRepository<HearingDayDetailsEntity, Long> {

    @Query("select FROM HearingDayDetailsEntity hdd WHERE hdd.hearingResponse.hearing.caseHearingRequest.hmctsServiceID"
        +" = :hmctsServiceCode  AND MIN(hdd.startDateTime >= :hearingStartDateFrom) and "
        + "MAX(hdd.endDateTime <= :hearingStartDateTo)")
    List<String> getUnNotifiedHearings(String hmctsServiceCode, String hearingStartDateFrom, String hearingStartDateTo );
}
