package uk.gov.hmcts.reform.hmc.data;

import org.springframework.data.jpa.repository.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.REQUIRES_NEW)
@Repository
public interface HearingRepository {

    @Query("select h from HearingEntity h where m.id = :id")
    HearingEntity findHearing(String id);

}
