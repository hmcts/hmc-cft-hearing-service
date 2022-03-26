package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.CaseCategoriesEntity;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.IndividualDetailEntity;
import uk.gov.hmcts.reform.hmc.data.NonStandardDurationsEntity;
import uk.gov.hmcts.reform.hmc.data.PanelRequirementsEntity;
import uk.gov.hmcts.reform.hmc.data.RequiredFacilitiesEntity;
import uk.gov.hmcts.reform.hmc.data.RequiredLocationsEntity;
import uk.gov.hmcts.reform.hmc.data.UnavailabilityEntity;
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.GetHearingResponse;
import uk.gov.hmcts.reform.hmc.model.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
import uk.gov.hmcts.reform.hmc.model.HearingWindow;
import uk.gov.hmcts.reform.hmc.model.IndividualDetails;
import uk.gov.hmcts.reform.hmc.model.OrganisationDetails;
import uk.gov.hmcts.reform.hmc.model.PanelRequirements;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.PartyType;
import uk.gov.hmcts.reform.hmc.model.RelatedParty;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityDow;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityRanges;
import uk.gov.hmcts.reform.hmc.model.hmi.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.hmi.RequestDetails;

import java.util.ArrayList;
import java.util.List;

@Component
public class GetHearingResponseMapper extends GetHearingResponseCommonCode {

    public GetHearingResponse toHearingResponse(HearingEntity hearingEntity) {
        GetHearingResponse getHearingResponse = new GetHearingResponse();
        getHearingResponse.setRequestDetails(setRequestDetails(hearingEntity));
        getHearingResponse.setHearingDetails(setHearingDetails(hearingEntity));
        getHearingResponse.setCaseDetails(setCaseDetails(hearingEntity));
        getHearingResponse.setPartyDetails(setPartyDetails(hearingEntity));
        getHearingResponse.setHearingResponse(setHearingResponse(hearingEntity));
        return getHearingResponse;
    }

    private RequestDetails setRequestDetails(HearingEntity hearingEntity) {
        RequestDetails requestDetails = new RequestDetails();
        CaseHearingRequestEntity caseHearingRequestEntity = hearingEntity.getLatestCaseHearingRequest();
        requestDetails.setStatus(hearingEntity.getStatus());
        requestDetails.setTimestamp(caseHearingRequestEntity.getHearingRequestReceivedDateTime());
        requestDetails.setVersionNumber(caseHearingRequestEntity.getVersionNumber());
        return requestDetails;
    }

    private CaseDetails setCaseDetails(HearingEntity hearingEntity) {
        CaseDetails caseDetails = new CaseDetails();
        CaseHearingRequestEntity caseHearingRequestEntity = hearingEntity.getLatestCaseHearingRequest();
        caseDetails.setHmctsServiceCode(caseHearingRequestEntity.getHmctsServiceCode());
        caseDetails.setCaseRef(caseHearingRequestEntity.getCaseReference());
        caseDetails.setExternalCaseReference(caseHearingRequestEntity.getExternalCaseReference());
        caseDetails.setCaseDeepLink(caseHearingRequestEntity.getCaseUrlContextPath());
        caseDetails.setHmctsInternalCaseName(caseHearingRequestEntity.getHmctsInternalCaseName());
        caseDetails.setPublicCaseName(caseHearingRequestEntity.getPublicCaseName());
        caseDetails.setCaseAdditionalSecurityFlag(
            caseHearingRequestEntity.getAdditionalSecurityRequiredFlag());
        caseDetails.setCaseInterpreterRequiredFlag(
            caseHearingRequestEntity.getInterpreterBookingRequiredFlag());
        caseDetails.setCaseCategories(setCaseCategories(hearingEntity));
        caseDetails.setCaseManagementLocationCode(caseHearingRequestEntity.getOwningLocationId());
        caseDetails.setCaseRestrictedFlag(caseHearingRequestEntity.getCaseRestrictedFlag());
        caseDetails.setCaseSlaStartDate(caseHearingRequestEntity.getCaseSlaStartDate());
        return caseDetails;
    }

    private ArrayList<CaseCategory> setCaseCategories(HearingEntity hearingEntity) {
        ArrayList<CaseCategory> caseCategories = new ArrayList<>();
        CaseHearingRequestEntity caseHearingRequestEntity = hearingEntity.getLatestCaseHearingRequest();
        if (null != caseHearingRequestEntity.getCaseCategories()
                && !caseHearingRequestEntity.getCaseCategories().isEmpty()) {
            for (CaseCategoriesEntity caseCategoriesEntity :
                    caseHearingRequestEntity.getCaseCategories()) {
                CaseCategory caseCategory = new CaseCategory();
                caseCategory.setCategoryType(caseCategoriesEntity.getCategoryType().getLabel());
                caseCategory.setCategoryValue(caseCategoriesEntity.getCaseCategoryValue());
                caseCategories.add(caseCategory);
            }
        }
        return caseCategories;
    }

    private ArrayList<PartyDetails> setPartyDetails(HearingEntity hearingEntity) {
        ArrayList<PartyDetails> partyDetailsList = new ArrayList<>();
        CaseHearingRequestEntity caseHearingRequestEntity = hearingEntity.getLatestCaseHearingRequest();
        for (HearingPartyEntity hearingPartyEntity : caseHearingRequestEntity.getHearingParties()) {
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


    private HearingResponse setHearingResponse(HearingEntity hearingEntity) {
        HearingResponseEntity hearingResponseEntity = hearingEntity.getLatestHearingResponse().orElseThrow();
        HearingResponse hearingResponse = new HearingResponse();
        hearingResponse.setListAssistTransactionID(
            hearingResponseEntity.getHearingResponseId());
        hearingResponse.setReceivedDateTime(hearingResponseEntity.getRequestTimeStamp());
        hearingResponse.setResponseVersion(hearingResponseEntity.getHearingResponseId());
        hearingResponse.setLaCaseStatus(hearingResponseEntity.getListingCaseStatus());
        hearingResponse.setListingStatus(hearingResponseEntity.getListingStatus());
        setHearingDaySchedule(hearingResponse, List.of(hearingResponseEntity));
        return hearingResponse;
    }


    private HearingDetails setHearingDetails(HearingEntity hearingEntity) {
        HearingDetails hearingDetails = new HearingDetails();
        CaseHearingRequestEntity caseHearingRequestEntity = hearingEntity.getLatestCaseHearingRequest();
        hearingDetails.setAutoListFlag(caseHearingRequestEntity.getAutoListFlag());
        hearingDetails.setHearingType(caseHearingRequestEntity.getHearingType());
        hearingDetails.setHearingWindow(setHearingWindow(hearingEntity));
        hearingDetails.setDuration(caseHearingRequestEntity.getRequiredDurationInMinutes());
        hearingDetails.setNonStandardHearingDurationReasons(setHearingPriorityType(hearingEntity));
        hearingDetails.setHearingPriorityType(caseHearingRequestEntity.getHearingPriorityType());
        hearingDetails.setNumberOfPhysicalAttendees(
            caseHearingRequestEntity.getNumberOfPhysicalAttendees());
        hearingDetails.setHearingInWelshFlag(caseHearingRequestEntity.getHearingInWelshFlag());
        hearingDetails.setHearingLocations(setHearingLocations(hearingEntity));
        hearingDetails.setFacilitiesRequired(setFacilityType(hearingEntity));
        hearingDetails.setListingComments(caseHearingRequestEntity.getListingComments());
        hearingDetails.setHearingRequester(caseHearingRequestEntity.getRequester());
        hearingDetails.setPrivateHearingRequiredFlag(
            caseHearingRequestEntity.getPrivateHearingRequiredFlag());
        hearingDetails.setLeadJudgeContractType(caseHearingRequestEntity.getLeadJudgeContractType());
        hearingDetails.setPanelRequirements(setPanelRequirements(hearingEntity));
        hearingDetails.setHearingIsLinkedFlag(hearingEntity.getIsLinkedFlag());
        return hearingDetails;
    }

    private ArrayList<String> setHearingPriorityType(HearingEntity hearingEntity) {
        ArrayList<String> hearingPriorityType = new ArrayList<>();
        CaseHearingRequestEntity caseHearingRequestEntity = hearingEntity.getLatestCaseHearingRequest();
        if (null != caseHearingRequestEntity.getNonStandardDurations()
                && !caseHearingRequestEntity.getNonStandardDurations().isEmpty()) {
            for (NonStandardDurationsEntity nonStandardDurationsEntity
                    : caseHearingRequestEntity.getNonStandardDurations()) {
                hearingPriorityType.add(nonStandardDurationsEntity.getNonStandardHearingDurationReasonType());
            }
        }
        return hearingPriorityType;
    }

    private PanelRequirements setPanelRequirements(HearingEntity hearingEntity) {
        PanelRequirements panelRequirement = new PanelRequirements();
        CaseHearingRequestEntity caseHearingRequestEntity = hearingEntity.getLatestCaseHearingRequest();
        if (null != caseHearingRequestEntity.getPanelRequirements()
                && !caseHearingRequestEntity.getPanelRequirements().isEmpty()) {
            for (PanelRequirementsEntity panelRequirementsEntity
                    : caseHearingRequestEntity.getPanelRequirements()) {
                panelRequirement.setRoleType(List.of(panelRequirementsEntity.getRoleType()));
            }
        }
        return panelRequirement;
    }

    private ArrayList<String> setFacilityType(HearingEntity hearingEntity) {
        ArrayList<String> facilityType = new ArrayList<>();
        CaseHearingRequestEntity caseHearingRequestEntity = hearingEntity.getLatestCaseHearingRequest();
        if (null != caseHearingRequestEntity.getRequiredFacilities()
                && !caseHearingRequestEntity.getRequiredFacilities().isEmpty()) {
            for (RequiredFacilitiesEntity requiredFacilitiesEntity
                    : caseHearingRequestEntity.getRequiredFacilities()) {
                facilityType.add(requiredFacilitiesEntity.getFacilityType());
            }
        }
        return facilityType;
    }

    private ArrayList<HearingLocation> setHearingLocations(HearingEntity hearingEntity) {
        ArrayList<HearingLocation> hearingLocations = new ArrayList<>();
        CaseHearingRequestEntity caseHearingRequestEntity = hearingEntity.getLatestCaseHearingRequest();
        if (null != caseHearingRequestEntity.getRequiredLocations()
                && !caseHearingRequestEntity.getRequiredLocations().isEmpty()) {
            for (RequiredLocationsEntity requiredLocationsEntity
                    : caseHearingRequestEntity.getRequiredLocations()) {
                HearingLocation hearingLocation = new HearingLocation();
                hearingLocation.setLocationId(requiredLocationsEntity.getLocationId().getLabel());
                hearingLocation.setLocationType(requiredLocationsEntity.getLocationLevelType());
                hearingLocations.add(hearingLocation);
            }
        }
        return hearingLocations;
    }

    private HearingWindow setHearingWindow(HearingEntity hearingEntity) {
        HearingWindow hearingWindow = new HearingWindow();
        CaseHearingRequestEntity caseHearingRequestEntity = hearingEntity.getLatestCaseHearingRequest();
        hearingWindow.setHearingWindowStartDateRange(
            caseHearingRequestEntity.getHearingWindowStartDateRange());
        hearingWindow.setHearingWindowEndDateRange(
            caseHearingRequestEntity.getHearingWindowEndDateRange());
        hearingWindow.setFirstDateTimeMustBe(caseHearingRequestEntity.getFirstDateTimeOfHearingMustBe());
        return hearingWindow;
    }

    private void setHearingDaySchedule(HearingResponse caseHearing,
                                       List<HearingResponseEntity> hearingResponses) {
        List<HearingDaySchedule> scheduleList = new ArrayList<>();

        for (HearingResponseEntity hearingResponseEntity : hearingResponses) {
            List<HearingDayDetailsEntity> hearingDayDetailEntities = hearingResponseEntity.getHearingDayDetails();
            if (null != hearingDayDetailEntities && !hearingDayDetailEntities.isEmpty()) {
                for (HearingDayDetailsEntity detailEntity : hearingDayDetailEntities) {
                    HearingDaySchedule hearingDaySchedule = setHearingDayScheduleDetails(detailEntity);
                    setHearingJudgeAndPanelMemberIds(detailEntity.getHearingDayPanel().get(0), hearingDaySchedule);
                    setAttendeeDetails(detailEntity.getHearingAttendeeDetails(), hearingDaySchedule);
                    scheduleList.add(hearingDaySchedule);
                }
            }
            caseHearing.setHearingDaySchedule(scheduleList);
        }
    }

}
