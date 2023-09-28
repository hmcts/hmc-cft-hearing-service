package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;

import java.util.List;

import static uk.gov.hmcts.reform.hmc.constants.Constants.CANCELLATION_REQUESTED;

@Component
public class HearingMapper {

    private static final int ROUND_VALUE = 5;
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

    public HearingEntity modelToEntity(HearingRequest hearingRequest,
                                       HearingEntity hearingEntity,
                                       Integer requestVersion,
                                       String status,
                                       boolean reasonableMatch,
                                       boolean facilitiesMatch,
                                       String deploymentId) {



        hearingRequest.getHearingDetails().setDuration(
            roundUpDuration(hearingRequest.getHearingDetails().getDuration())
        );

        CaseHearingRequestEntity caseHearingRequestEntity = caseHearingRequestMapper.modelToEntity(
            hearingRequest, hearingEntity, requestVersion, reasonableMatch, facilitiesMatch);
        setHearingDetails(hearingRequest.getHearingDetails(), caseHearingRequestEntity);
        setCaseDetails(hearingRequest.getCaseDetails(), caseHearingRequestEntity);
        setPartyDetails(hearingRequest.getPartyDetails(), caseHearingRequestEntity);
        hearingEntity.setStatus(status);
        hearingEntity.setDeploymentId(deploymentId);
        hearingEntity.setIsLinkedFlag(hearingRequest.getHearingDetails().getHearingIsLinkedFlag());
        hearingEntity.getCaseHearingRequests().add(caseHearingRequestEntity);
        return hearingEntity;
    }

    public HearingEntity modelToEntity(DeleteHearingRequest hearingRequest,
                                       HearingEntity hearingEntity,
                                       Integer requestVersion,
                                       CaseHearingRequestEntity caseHearingRequestEntity) {
        caseHearingRequestEntity = caseHearingRequestMapper.modelToEntity(
            hearingRequest, hearingEntity, requestVersion, caseHearingRequestEntity);
        hearingEntity.setStatus(CANCELLATION_REQUESTED);
        hearingEntity.getCaseHearingRequests().add(caseHearingRequestEntity);
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

    public static Integer roundUpDuration(Integer duration) {
        return (duration + ROUND_VALUE - 1) / ROUND_VALUE * ROUND_VALUE;
    }
}

