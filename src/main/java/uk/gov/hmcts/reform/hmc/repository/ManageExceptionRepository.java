package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;

@Transactional(propagation = Propagation.REQUIRED)
@Repository
public interface ManageExceptionRepository {

    @Query("from HearingEntity where id = :hearingId")
    HearingEntity findByHearingId(Long hearingId);

}
