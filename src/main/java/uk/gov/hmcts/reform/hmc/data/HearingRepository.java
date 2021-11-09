package uk.gov.hmcts.reform.hmc.data;

import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.REQUIRES_NEW)
@Repository
public interface HearingRepository extends CrudRepository<HearingEntity, Long> {

    @Query("select h from HearingEntity h where h.id = :id")
    HearingEntity findHearing(Long id);

}
