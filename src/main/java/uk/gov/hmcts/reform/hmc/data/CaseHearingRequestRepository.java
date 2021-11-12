package uk.gov.hmcts.reform.hmc.data;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.helper.CaseHearingRequestMapper;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;

import java.time.LocalDateTime;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
@Transactional
//@Rollback(true)
public class CaseHearingRequestRepository {

    @PersistenceContext
    private EntityManager em;

    private final CaseHearingRequestMapper caseHearingRequestMapper;

    public CaseHearingRequestRepository(CaseHearingRequestMapper caseHearingRequestMapper) {
        this.caseHearingRequestMapper = caseHearingRequestMapper;
    }


    public HearingResponse saveCaseHearingRequest(HearingRequest hearingRequest) {

        HearingResponse response = new HearingResponse();
        response.setHearingRequestId(2000000L);
        response.setStatus("Requested");
        response.setTimeStamp(LocalDateTime.now());
        return response;
    }

}
