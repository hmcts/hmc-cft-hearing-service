package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.CancellationReasonsEntity;
import uk.gov.hmcts.reform.hmc.data.CaseCategoriesEntity;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class CaseHearingRequestMapper {

    private final CaseCategoriesMapper caseCategoriesMapper;

    private final Clock utcClock;

    @Autowired
    public CaseHearingRequestMapper(CaseCategoriesMapper caseCategoriesMapper,
                                    Clock utcClock) {
        this.caseCategoriesMapper = caseCategoriesMapper;
        this.utcClock = utcClock;
    }

    public CaseHearingRequestEntity modelToEntity(HearingRequest hearingRequest,
                                                  HearingEntity hearingEntity,
                                                  Integer requestVersion) {
        final CaseHearingRequestEntity caseHearingRequestEntity = new CaseHearingRequestEntity();
        HearingDetails hearingDetails = hearingRequest.getHearingDetails();
        CaseDetails caseDetails = hearingRequest.getCaseDetails();
        caseHearingRequestEntity.setAutoListFlag(hearingDetails.getAutoListFlag());
        caseHearingRequestEntity.setHearingType(hearingDetails.getHearingType());
        caseHearingRequestEntity.setRequiredDurationInMinutes(hearingDetails.getDuration());
        caseHearingRequestEntity.setHearingPriorityType(hearingDetails.getHearingPriorityType());
        caseHearingRequestEntity.setNumberOfPhysicalAttendees(hearingDetails.getNumberOfPhysicalAttendees());
        caseHearingRequestEntity.setHearingInWelshFlag(hearingDetails.getHearingInWelshFlag());
        caseHearingRequestEntity.setPrivateHearingRequiredFlag(hearingDetails.getPrivateHearingRequiredFlag());
        caseHearingRequestEntity.setLeadJudgeContractType(hearingDetails.getLeadJudgeContractType());
        caseHearingRequestEntity.setFirstDateTimeOfHearingMustBe(hearingDetails.getHearingWindow()
                                                                     .getFirstDateTimeMustBe());
        caseHearingRequestEntity.setHmctsServiceCode(caseDetails.getHmctsServiceCode());
        caseHearingRequestEntity.setCaseReference(caseDetails.getCaseRef());
        caseHearingRequestEntity.setExternalCaseReference(caseDetails.getExternalCaseReference());
        caseHearingRequestEntity.setCaseUrlContextPath(caseDetails.getCaseDeepLink());
        caseHearingRequestEntity.setHmctsInternalCaseName(caseDetails.getHmctsInternalCaseName());
        caseHearingRequestEntity.setPublicCaseName(caseDetails.getPublicCaseName());
        caseHearingRequestEntity.setAdditionalSecurityRequiredFlag(caseDetails.getCaseAdditionalSecurityFlag());
        caseHearingRequestEntity.setOwningLocationId(caseDetails.getCaseManagementLocationCode());
        caseHearingRequestEntity.setCaseRestrictedFlag(caseDetails.getCaseRestrictedFlag());
        caseHearingRequestEntity.setCaseSlaStartDate(caseDetails.getCaseSlaStartDate());
        caseHearingRequestEntity.setVersionNumber(requestVersion);
        caseHearingRequestEntity.setInterpreterBookingRequiredFlag(caseDetails.getCaseInterpreterRequiredFlag());
        caseHearingRequestEntity.setListingComments(hearingDetails.getListingComments());
        caseHearingRequestEntity.setRequester(hearingDetails.getHearingRequester());
        caseHearingRequestEntity.setHearingWindowStartDateRange(hearingDetails.getHearingWindow()
                                                                    .getDateRangeStart());
        caseHearingRequestEntity.setHearingWindowEndDateRange(hearingDetails.getHearingWindow()
                                                                  .getDateRangeEnd());
        caseHearingRequestEntity.setHearingRequestReceivedDateTime(currentTime());
        caseHearingRequestEntity.setHearing(hearingEntity);
        return caseHearingRequestEntity;
    }

    public CaseHearingRequestEntity modelToEntity(DeleteHearingRequest deleteHearingRequest,
                                                  HearingEntity hearingEntity,
                                                  Integer requestVersion) {
        CaseHearingRequestEntity caseHearingRequestEntity = new CaseHearingRequestEntity();
        caseHearingRequestEntity.setVersionNumber(requestVersion);
        caseHearingRequestEntity.setHearingRequestReceivedDateTime(currentTime());
        caseHearingRequestEntity.setCancellationReason(setCancellationReasonsEntity(deleteHearingRequest
            .getCancellationReasonCode(), caseHearingRequestEntity));
        caseHearingRequestEntity.setHearing(hearingEntity);
        return caseHearingRequestEntity;
    }

    public void mapCaseCategories(List<CaseCategory> caseCategories,
                                  CaseHearingRequestEntity caseHearingRequestEntity) {
        final List<CaseCategoriesEntity> caseCategoriesEntities =
            caseCategoriesMapper.modelToEntity(caseCategories, caseHearingRequestEntity);
        caseHearingRequestEntity.setCaseCategories(caseCategoriesEntities);
    }

    private CancellationReasonsEntity setCancellationReasonsEntity(String cancellationReasonCode,
                                                                   CaseHearingRequestEntity caseHearingRequestEntity) {
        final CancellationReasonsEntity cancellationReasonsEntity = new CancellationReasonsEntity();
        cancellationReasonsEntity.setCaseHearing(caseHearingRequestEntity);
        cancellationReasonsEntity.setCancellationReasonType(cancellationReasonCode);
        return cancellationReasonsEntity;
    }

    private LocalDateTime currentTime() {
        return LocalDateTime.now(utcClock);
    }
}








