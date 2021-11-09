package uk.gov.hmcts.reform.hmc.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;

@Transactional
public interface HearingRepository {

    @Query("select h from HearingEntity h where h.id = :hearingId")
    HearingEntity getHearingByHearingId(String hearingId);

    void saveHearing(HearingRequest hearingRequest);
}
