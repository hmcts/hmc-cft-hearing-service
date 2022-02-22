package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.PartiesNotifiedEntity;

import java.util.List;

@Transactional(propagation = Propagation.REQUIRES_NEW)
@Repository
public interface HearingResponseRepository extends CrudRepository<HearingResponseEntity, Long> {

    @Query("FROM PartiesNotifiedEntity hre WHERE hre.hearing.id = :hearingId "
            + "AND hre.partiesNotifiedDateTime is NOT NULL")
    List<PartiesNotifiedEntity> getPartiesNotified(Long hearingId);

}
