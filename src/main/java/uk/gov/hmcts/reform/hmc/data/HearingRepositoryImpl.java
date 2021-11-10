package uk.gov.hmcts.reform.hmc.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.helper.CaseHearingRequestMapper;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;

import java.time.LocalDateTime;

@Repository
@Transactional
public class HearingRepositoryImpl implements  HearingRepository {

    private final CaseHearingRequestMapper caseHearingRequestMapper;

    @Autowired
    public HearingRepositoryImpl(CaseHearingRequestMapper caseHearingRequestMapper) {
        this.caseHearingRequestMapper = caseHearingRequestMapper;
    }

    @Override
    public HearingEntity getHearingByHearingId(String hearingId) {
        return null;
    }

    @Override
    public HearingResponse saveHearing(HearingRequest hearingRequest) {

        CaseHearingRequestEntity caseHearingRequestEntity = caseHearingRequestMapper
            .modelToEntity(hearingRequest.getRequestDetails());

        caseHearingRequestMapper.modelToEntity(hearingRequest.getHearingDetails(),
                                                                          caseHearingRequestEntity);
        HearingResponse response = new HearingResponse();
        response.setHearingRequestId(2000000L);
        response.setStatus("Requested");
        response.setTimeStamp(LocalDateTime.now());
        return response;


    }
}
