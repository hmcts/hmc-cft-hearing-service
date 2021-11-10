package uk.gov.hmcts.reform.hmc.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;

@Repository
public interface HearingRepository extends CrudRepository<HearingEntity, Long> {

    //@Query("select h from HearingEntity h where h.id = :id")
    HearingEntity findHearing(Long id);

    HearingResponse saveHearing(HearingRequest hearingRequest);
}
