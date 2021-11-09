package uk.gov.hmcts.reform.hmc.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.hmc.helper.CaseHearingRequestMapper;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;

@Repository
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
    public void saveHearing(HearingRequest hearingRequest) {

        CaseHearingRequestEntity caseHearingRequestEntity = caseHearingRequestMapper
            .modelToEntity(hearingRequest.getRequestDetails());

        caseHearingRequestEntity = caseHearingRequestMapper.modelToEntity(hearingRequest.getHearingDetails(),
                                                                          caseHearingRequestEntity);


    }
}
