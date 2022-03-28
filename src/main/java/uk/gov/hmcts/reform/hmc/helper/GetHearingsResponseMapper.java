package uk.gov.hmcts.reform.hmc.helper;

import lombok.val;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.*;
import uk.gov.hmcts.reform.hmc.model.*;

import java.util.ArrayList;
import java.util.List;

@Component
public class GetHearingsResponseMapper extends GetHearingResponseCommonCode {

    public GetHearingsResponse toHearingsResponse(String caseRef, List<CaseHearingRequestEntity> entities) {
        GetHearingsResponse getHearingsResponse = new GetHearingsResponse();
        getHearingsResponse.setCaseRef(caseRef);
        if (!entities.isEmpty()) {
            getHearingsResponse.setHmctsServiceId(entities.get(0).getHmctsServiceCode());
            setCaseHearings(entities, getHearingsResponse);
        } else {
            getHearingsResponse.setCaseHearings(new ArrayList<>());
        }
        return getHearingsResponse;

    }

    private void setCaseHearings(List<CaseHearingRequestEntity> entities, GetHearingsResponse getHearingsResponse) {
        List<CaseHearing> caseHearingList = new ArrayList<>();
        for (CaseHearingRequestEntity entity : entities) {
            CaseHearing caseHearing = getCaseHearing(entity);
            List<HearingResponseEntity> hearingResponses = getHearingResponseEntities(entity, caseHearing);
            setHearingDaySchedule(caseHearingList, caseHearing, hearingResponses);
        }
        getHearingsResponse.setCaseHearings(caseHearingList);
    }

    private void setHearingDaySchedule(List<CaseHearing> caseHearingList, CaseHearing caseHearing,
                                       List<HearingResponseEntity> hearingResponses) {
        List<HearingDaySchedule> scheduleList = new ArrayList<>();

        for (HearingResponseEntity hearingResponseEntity : hearingResponses) {
            List<HearingDayDetailsEntity> hearingDayDetailEntities = hearingResponseEntity.getHearingDayDetails();
            if (!hearingDayDetailEntities.isEmpty()) {
                for (HearingDayDetailsEntity detailEntity : hearingDayDetailEntities) {
                    HearingDaySchedule hearingDaySchedule = setHearingDayScheduleDetails(detailEntity);
                    setHearingJudgeAndPanelMemberIds(detailEntity.getHearingDayPanel().get(0), hearingDaySchedule);
                    setAttendeeDetails(detailEntity.getHearingAttendeeDetails(), hearingDaySchedule);
                    scheduleList.add(hearingDaySchedule);
                }
            }
            caseHearing.setHearingDaySchedule(scheduleList);
            caseHearingList.add(caseHearing);
        }
    }

    private List<HearingResponseEntity> getHearingResponseEntities(CaseHearingRequestEntity entity,
                                                                   CaseHearing caseHearing) {
        List<HearingResponseEntity> hearingResponses = entity.getHearing().getHearingResponses();
        setHearingResponseDetails(caseHearing, hearingResponses);
        return hearingResponses;
    }

    private CaseHearing getCaseHearing(CaseHearingRequestEntity entity) {
        CaseHearing caseHearing = new CaseHearing();
        caseHearing.setHearingId(entity.getHearing().getId());
        caseHearing.setHearingRequestDateTime(entity.getHearingRequestReceivedDateTime());
        caseHearing.setHearingType(entity.getHearingType());
        caseHearing.setHmcStatus(entity.getHearing().getStatus());
        return caseHearing;
    }

    private void setHearingResponseDetails(CaseHearing caseHearing, List<HearingResponseEntity> entities) {
        for (HearingResponseEntity hearingResponseEntity : entities) {
            caseHearing.setLastResponseReceivedDateTime(hearingResponseEntity.getRequestTimeStamp());
            caseHearing.setResponseVersion(hearingResponseEntity.getHearingResponseId());
            caseHearing.setHearingListingStatus(hearingResponseEntity.getListingStatus());
            caseHearing.setListAssistCaseStatus(hearingResponseEntity.getListingCaseStatus());
        }
    }

    public HearingActualResponse toHearingActualResponse(HearingEntity hearingEntity) {
        val hearingResponseEntity = hearingEntity.getHearingResponses();
        val response = new HearingActualResponse();
        response.setHmcStatus(hearingEntity.getStatus());
        response.setCaseDetails(setCaseDetails(hearingEntity));
        setHearingPlanned(hearingEntity, response);
        setHearingActuals(hearingEntity, response);
        return response;
    }

    private void setHearingActuals(HearingEntity hearingEntity, HearingActualResponse response) {
        val hearingResponses = hearingEntity.getHearingResponses();

        hearingResponses.stream().map(hearingResponse -> {
            return hearingResponse.getHearingResponseId();
        });
    }

    private void setHearingPlanned(HearingEntity hearingEntity, HearingActualResponse response) {
        val caseHearingRequestEntity = hearingEntity.getCaseHearingRequest();
        val hearingPlanned = new HearingPlanned();
        hearingPlanned.setPlannedHearingType(caseHearingRequestEntity.getHearingType());
        hearingPlanned.setPlannedHearingDays(getPlannedHearingDays(hearingEntity));
        response.setHearingPlanned(hearingPlanned);
    }

    private List<PlannedHearingDays> getPlannedHearingDays(HearingEntity hearingEntity) {

        List<PlannedHearingDays> plannedHearingDays = new ArrayList<>();

        val hearingResponses = hearingEntity.getHearingResponses();

        for (HearingResponseEntity hearingResponseEntity : hearingResponses) {
            List<HearingDayDetailsEntity> hearingDayDetailEntities = hearingResponseEntity.getHearingDayDetails();
            if (!hearingDayDetailEntities.isEmpty()) {
                for (HearingDayDetailsEntity detailEntity : hearingDayDetailEntities) {

                    PlannedHearingDays plannedHearingDay = new PlannedHearingDays();
                    plannedHearingDay.setPlannedStartTime(detailEntity.getStartDateTime());
                    plannedHearingDay.setPlannedEndTime(detailEntity.getEndDateTime());
                    plannedHearingDay.setParties(setPartyDetails(hearingEntity));
                    plannedHearingDays.add(plannedHearingDay);
                }
            }

        }
    return plannedHearingDays;
    }

    private ArrayList<PartyDetails> setPartyDetails(HearingEntity hearingEntity) {
        ArrayList<PartyDetails> partyDetailsList = new ArrayList<>();
        for (HearingPartyEntity hearingPartyEntity : hearingEntity.getCaseHearingRequest().getHearingParties()) {
            PartyDetails partyDetails = new PartyDetails();
            partyDetails.setPartyID(hearingPartyEntity.getPartyReference());
            partyDetails.setPartyType(hearingPartyEntity.getPartyType().getLabel());
            partyDetails.setPartyRole(hearingPartyEntity.getPartyRoleType());
            if (PartyType.IND.getLabel().equals(hearingPartyEntity.getPartyType().getLabel())) {
                partyDetails.setIndividualDetails(setIndividualDetails(hearingPartyEntity).get(0));
            } else {
                partyDetails.setOrganisationDetails(setOrganisationDetails(hearingPartyEntity));
            }
            setUnavailability(hearingPartyEntity, partyDetails);
            partyDetailsList.add(partyDetails);
        }
        return partyDetailsList;
    }

    private void setUnavailability(HearingPartyEntity hearingPartyEntity, PartyDetails partyDetails) {
        ArrayList<UnavailabilityDow> unavailabilityDowArrayList = new ArrayList<>();
        ArrayList<UnavailabilityRanges> unavailabilityRangesArrayList = new ArrayList<>();
        if (null != hearingPartyEntity.getUnavailabilityEntity()
            && !hearingPartyEntity.getUnavailabilityEntity().isEmpty()) {
            for (UnavailabilityEntity unavailabilityEntity : hearingPartyEntity.getUnavailabilityEntity()) {
                addUnavailabilityDow(unavailabilityEntity, unavailabilityDowArrayList);
                addUnavailabilityRange(unavailabilityEntity, unavailabilityRangesArrayList);
            }
        }
        partyDetails.setUnavailabilityDow(unavailabilityDowArrayList);
        partyDetails.setUnavailabilityRanges(unavailabilityRangesArrayList);
    }

    private void addUnavailabilityRange(UnavailabilityEntity unavailabilityEntity,
                                        ArrayList<UnavailabilityRanges> unavailabilityRangesArrayList) {
        if (null != unavailabilityEntity.getStartDate() || null != unavailabilityEntity.getEndDate()) {
            UnavailabilityRanges unavailabilityRanges = new UnavailabilityRanges();
            if (null != unavailabilityEntity.getEndDate()) {
                unavailabilityRanges.setUnavailableToDate(unavailabilityEntity.getEndDate());
            }
            if (null != unavailabilityEntity.getStartDate()) {
                unavailabilityRanges.setUnavailableFromDate(unavailabilityEntity.getStartDate());
            }
            unavailabilityRangesArrayList.add(unavailabilityRanges);
        }
    }

    private void addUnavailabilityDow(UnavailabilityEntity unavailabilityEntity,
                                      ArrayList<UnavailabilityDow> unavailabilityDowArrayList) {
        if (null != unavailabilityEntity.getDayOfWeekUnavailableType()
            || null != unavailabilityEntity.getDayOfWeekUnavailable()) {
            UnavailabilityDow unavailabilityDow = new UnavailabilityDow();
            if (null != unavailabilityEntity.getDayOfWeekUnavailableType()) {
                unavailabilityDow.setDowUnavailabilityType(
                    unavailabilityEntity.getDayOfWeekUnavailableType().getLabel());
            }
            if (null != unavailabilityEntity.getDayOfWeekUnavailable()) {
                unavailabilityDow.setDow(
                    unavailabilityEntity.getDayOfWeekUnavailable().getLabel());
            }
            unavailabilityDowArrayList.add(unavailabilityDow);
        }
    }


    private OrganisationDetails setOrganisationDetails(HearingPartyEntity hearingPartyEntity) {
        OrganisationDetails organisationDetails = new OrganisationDetails();
        if (hearingPartyEntity.getOrganisationDetailEntity() != null) {
            organisationDetails.setName(hearingPartyEntity.getOrganisationDetailEntity().getOrganisationName());
            organisationDetails.setOrganisationType(
                hearingPartyEntity.getOrganisationDetailEntity().getOrganisationTypeCode());
            organisationDetails.setCftOrganisationID(
                hearingPartyEntity.getOrganisationDetailEntity().getHmctsOrganisationReference());
        }
        return organisationDetails;
    }

    private ArrayList<IndividualDetails> setIndividualDetails(HearingPartyEntity hearingPartyEntity) {
        ArrayList<IndividualDetails> individualDetailsArrayList = new ArrayList<>();
        if (hearingPartyEntity.getIndividualDetailEntity() != null) {
            for (IndividualDetailEntity individualDetailEntity : hearingPartyEntity.getIndividualDetailEntity()) {
                IndividualDetails individualDetails = new IndividualDetails();
                individualDetails.setTitle(individualDetailEntity.getTitle());
                individualDetails.setFirstName(individualDetailEntity.getFirstName());
                individualDetails.setLastName(individualDetailEntity.getLastName());
                individualDetails.setPreferredHearingChannel(individualDetailEntity.getChannelType());
                individualDetails.setInterpreterLanguage(individualDetailEntity.getInterpreterLanguage());
                if (null != hearingPartyEntity.getReasonableAdjustmentsEntity()
                    && !hearingPartyEntity.getReasonableAdjustmentsEntity().isEmpty()) {
                    individualDetails.setReasonableAdjustments(
                        List.of(hearingPartyEntity.getReasonableAdjustmentsEntity().get(
                            0).getReasonableAdjustmentCode()));
                }
                individualDetails.setVulnerableFlag(individualDetailEntity.getVulnerableFlag());
                individualDetails.setVulnerabilityDetails(individualDetailEntity.getVulnerabilityDetails());
                if (null != hearingPartyEntity.getContactDetails()
                    && !hearingPartyEntity.getContactDetails().isEmpty()) {
                    if (hearingPartyEntity.getContactDetails().get(0).getContactDetails().contains("@")) {
                        individualDetails.setHearingChannelEmail(
                            hearingPartyEntity.getContactDetails().get(0).getContactDetails());
                    } else {
                        individualDetails.setHearingChannelPhone(
                            hearingPartyEntity.getContactDetails().get(0).getContactDetails());
                    }
                }
                RelatedParty relatedParty = new RelatedParty();
                relatedParty.setRelatedPartyID(individualDetailEntity.getRelatedPartyID());
                relatedParty.setRelationshipType(individualDetailEntity.getRelatedPartyRelationshipType());

                individualDetails.setRelatedParties(List.of(relatedParty));
                individualDetailsArrayList.add(individualDetails);
            }
        }
        return individualDetailsArrayList;
    }


    private CaseDetails setCaseDetails(HearingEntity hearingEntity) {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setHmctsServiceCode(hearingEntity.getCaseHearingRequest().getHmctsServiceCode());
        caseDetails.setCaseRef(hearingEntity.getCaseHearingRequest().getCaseReference());
        caseDetails.setExternalCaseReference(hearingEntity.getCaseHearingRequest().getExternalCaseReference());
        caseDetails.setCaseDeepLink(hearingEntity.getCaseHearingRequest().getCaseUrlContextPath());
        caseDetails.setHmctsInternalCaseName(hearingEntity.getCaseHearingRequest().getHmctsInternalCaseName());
        caseDetails.setPublicCaseName(hearingEntity.getCaseHearingRequest().getPublicCaseName());
        caseDetails.setCaseAdditionalSecurityFlag(
            hearingEntity.getCaseHearingRequest().getAdditionalSecurityRequiredFlag());
        caseDetails.setCaseInterpreterRequiredFlag(
            hearingEntity.getCaseHearingRequest().getInterpreterBookingRequiredFlag());
        caseDetails.setCaseCategories(setCaseCategories(hearingEntity));
        caseDetails.setCaseManagementLocationCode(hearingEntity.getCaseHearingRequest().getOwningLocationId());
        caseDetails.setCaseRestrictedFlag(hearingEntity.getCaseHearingRequest().getCaseRestrictedFlag());
        caseDetails.setCaseSlaStartDate(hearingEntity.getCaseHearingRequest().getCaseSlaStartDate());
        return caseDetails;
    }

    private ArrayList<CaseCategory> setCaseCategories(HearingEntity hearingEntity) {
        ArrayList<CaseCategory> caseCategories = new ArrayList<>();
        if (null != hearingEntity.getCaseHearingRequest().getCaseCategories()
            && !hearingEntity.getCaseHearingRequest().getCaseCategories().isEmpty()) {
            for (CaseCategoriesEntity caseCategoriesEntity :
                hearingEntity.getCaseHearingRequest().getCaseCategories()) {
                CaseCategory caseCategory = new CaseCategory();
                caseCategory.setCategoryType(caseCategoriesEntity.getCategoryType().getLabel());
                caseCategory.setCategoryValue(caseCategoriesEntity.getCaseCategoryValue());
                caseCategories.add(caseCategory);
            }
        }
        return caseCategories;
    }
}
