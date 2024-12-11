package uk.gov.hmcts.reform.hmc.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.client.hmi.ListingReasonCode;
import uk.gov.hmcts.reform.hmc.data.CancellationReasonsEntity;
import uk.gov.hmcts.reform.hmc.data.CaseCategoriesEntity;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
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
                                                  Integer requestVersion,
                                                  boolean reasonableMatch,
                                                  boolean facilitiesMatch) {


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
        caseHearingRequestEntity.setHearingRequestReceivedDateTime(currentTime());
        caseHearingRequestEntity.setHearing(hearingEntity);
        caseHearingRequestEntity.setIsAPanelFlag(getIsAPanelFlagBoolean(hearingDetails.getIsAPanelFlag()));
        if (hearingDetails.getHearingWindow() != null) {
            caseHearingRequestEntity.setFirstDateTimeOfHearingMustBe(hearingDetails.getHearingWindow()
                                                                         .getFirstDateTimeMustBe());
            caseHearingRequestEntity.setHearingWindowStartDateRange(hearingDetails.getHearingWindow()
                                                                        .getDateRangeStart());
            caseHearingRequestEntity.setHearingWindowEndDateRange(hearingDetails.getHearingWindow()
                                                                      .getDateRangeEnd());
        }

        if (Boolean.TRUE.equals(hearingDetails.getAutoListFlag()) && !(reasonableMatch && facilitiesMatch)) {
            caseHearingRequestEntity.setAutoListFlag(false);
            caseHearingRequestEntity.setListingAutoChangeReasonCode(ListingReasonCode.NO_MAPPING_AVAILABLE.getLabel());
        }

        if (hearingDetails.getListingAutoChangeReasonCode() != null) {
            if (Boolean.FALSE.equals(hearingDetails.getAutoListFlag())) {
                caseHearingRequestEntity.setListingAutoChangeReasonCode(
                    hearingDetails.getListingAutoChangeReasonCode());
            } else {
                throw new BadRequestException(ValidationError.MUST_BE_FALSE_IF_YOU_SUPPLY_A_CHANGE_REASONCODE);
            }
        }

        return caseHearingRequestEntity;
    }

    public CaseHearingRequestEntity modelToEntity(DeleteHearingRequest deleteHearingRequest,
                                                  HearingEntity hearingEntity,
                                                  Integer requestVersion,
                                                  CaseHearingRequestEntity caseHearingCurrent) {
        CaseHearingRequestEntity caseHearingRequestNew = new CaseHearingRequestEntity();
        try {
            caseHearingRequestNew = (CaseHearingRequestEntity) caseHearingCurrent.clone();
        } catch (CloneNotSupportedException e) {
            log.error("Error while reading the response:{}", e.getMessage());
        }
        caseHearingRequestNew.setVersionNumber(requestVersion);
        caseHearingRequestNew.setCancellationReasons(setCancellationReasonsEntities(
            deleteHearingRequest
                .getCancellationReasonCodes(),
            caseHearingRequestNew
        ));
        caseHearingRequestNew.setHearingRequestReceivedDateTime(currentTime());
        caseHearingRequestNew.setHearing(hearingEntity);
        return caseHearingRequestNew;
    }

    public void mapCaseCategories(List<CaseCategory> caseCategories,
                                  CaseHearingRequestEntity caseHearingRequestEntity) {
        final List<CaseCategoriesEntity> caseCategoriesEntities =
            caseCategoriesMapper.modelToEntity(caseCategories, caseHearingRequestEntity);
        caseHearingRequestEntity.setCaseCategories(caseCategoriesEntities);
    }

    private List<CancellationReasonsEntity> setCancellationReasonsEntities(List<String> cancellationReasonCodes,
                                                                           CaseHearingRequestEntity
                                                                                   caseHearingRequestEntity) {
        return cancellationReasonCodes.stream().map(cancellationReasonCode -> {
            CancellationReasonsEntity cancellationReasonsEntity = new CancellationReasonsEntity();
            cancellationReasonsEntity.setCaseHearing(caseHearingRequestEntity);
            cancellationReasonsEntity.setCancellationReasonType(cancellationReasonCode);
            return cancellationReasonsEntity;
        }).toList();
    }

    private Boolean getIsAPanelFlagBoolean(Object isAPanelFlag) {
        if (isAPanelFlag.equals(Boolean.TRUE) || isAPanelFlag.equals("true")) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private LocalDateTime currentTime() {
        return LocalDateTime.now(utcClock);
    }
}








