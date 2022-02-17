package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.CreateHearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;

import java.util.List;

import static uk.gov.hmcts.reform.hmc.constants.Constants.POST_HEARING_STATUS;

@Component
public class HearingMapper {

    private final CaseHearingRequestMapper caseHearingRequestMapper;

    private PartyDetailMapper partyDetailMapper;

    private HearingDetailsMapper hearingDetailsMapper;

    @Autowired
    public HearingMapper(CaseHearingRequestMapper caseHearingRequestMapper,
                         PartyDetailMapper partyDetailMapper,
                         HearingDetailsMapper hearingDetailsMapper) {
        this.caseHearingRequestMapper = caseHearingRequestMapper;
        this.partyDetailMapper = partyDetailMapper;
        this.hearingDetailsMapper = hearingDetailsMapper;
    }

    public HearingEntity modelToEntity(HearingRequest hearingRequest, CaseHearingRequestEntity caseHearingRequestEntity,
                                       HearingEntity hearingEntity) {
        setHearingDetails(hearingRequest.getHearingDetails(), caseHearingRequestEntity);
        setCaseDetails(hearingRequest.getCaseDetails(), caseHearingRequestEntity);
        setPartyDetails(hearingRequest.getPartyDetails(), caseHearingRequestEntity);
        if (hearingRequest instanceof CreateHearingRequest) {
            hearingEntity.setStatus(POST_HEARING_STATUS);
        }
        hearingEntity.setCaseHearingRequest(caseHearingRequestEntity);
        return hearingEntity;
    }

    private void setPartyDetails(List<PartyDetails> partyDetails, CaseHearingRequestEntity caseHearingRequestEntity) {
        partyDetailMapper.mapPartyDetails(partyDetails, caseHearingRequestEntity);
    }

    private void setHearingDetails(HearingDetails hearingDetails, CaseHearingRequestEntity caseHearingRequestEntity) {
        hearingDetailsMapper.mapHearingDetails(hearingDetails, caseHearingRequestEntity);
    }

    private void setCaseDetails(CaseDetails caseDetails, CaseHearingRequestEntity caseHearingRequestEntity) {
        caseHearingRequestMapper.mapCaseCategories(caseDetails.getCaseCategories(), caseHearingRequestEntity);
    }

}

