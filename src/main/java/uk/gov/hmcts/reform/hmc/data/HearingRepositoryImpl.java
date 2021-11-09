package uk.gov.hmcts.reform.hmc.data;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.hmc.helper.CaseHearingRequestMapper;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;

@Service
@Slf4j
public class HearingRepositoryImpl implements  HearingRepository {

    private final CaseHearingRequestMapper caseHearingRequestMapper;


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
