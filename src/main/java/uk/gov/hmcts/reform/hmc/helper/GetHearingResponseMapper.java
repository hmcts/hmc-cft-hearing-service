package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.CaseCategoriesEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.IndividualDetailEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingDetailsAudit;
import uk.gov.hmcts.reform.hmc.data.NonStandardDurationsEntity;
import uk.gov.hmcts.reform.hmc.data.PanelRequirementsEntity;
import uk.gov.hmcts.reform.hmc.data.RequiredFacilitiesEntity;
import uk.gov.hmcts.reform.hmc.data.RequiredLocationsEntity;
import uk.gov.hmcts.reform.hmc.data.UnavailabilityEntity;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ListAssistCaseStatus;
import uk.gov.hmcts.reform.hmc.domain.model.enums.ListingStatus;
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
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingDetailsRepository;

import java.util.ArrayList;
import java.util.List;

@Component
public class GetHearingResponseMapper extends GetHearingResponseCommonCode {

    private LinkedHearingDetailsRepository linkedHearingDetailsRepository;

    @Autowired
    public GetHearingResponseMapper(LinkedHearingDetailsRepository linkedHearingDetailsRepository) {
        this.linkedHearingDetailsRepository = linkedHearingDetailsRepository;
    }


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
        requestDetails.setHearingRequestId(hearingEntity.getId().toString());
        requestDetails.setHearingGroupRequestId(getRequestId(hearingEntity.getId()));
        requestDetails.setStatus(hearingEntity.getStatus());
        requestDetails.setTimestamp(hearingEntity.getCaseHearingRequest().getHearingRequestReceivedDateTime());
        requestDetails.setVersionNumber(hearingEntity.getCaseHearingRequest().getVersionNumber());
        return requestDetails;
    }

    private String getRequestId(Long hearingId) {
        LinkedHearingDetailsAudit linkedHearingDetails =
                linkedHearingDetailsRepository.getLinkedHearingDetailsByHearingId(hearingId);
        if (null != linkedHearingDetails && null != linkedHearingDetails.getLinkedGroup()) {
            return linkedHearingDetails.getLinkedGroup().getRequestId();
        }
        return null;
    }

    private CaseDetails setCaseDetails(HearingEntity hearingEntity) {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setHmctsServiceCode(hearingEntity.getCaseHearingRequest().getHmctsServiceID());
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
                IndividualDetails individualDetails = createIndividualDetail(hearingPartyEntity,
                                                        individualDetailEntity);
                individualDetailsArrayList.add(individualDetails);
            }
        }
        return individualDetailsArrayList;
    }

    private IndividualDetails createIndividualDetail(HearingPartyEntity hearingPartyEntity,
                                                     IndividualDetailEntity individualDetailEntity) {

        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setTitle(individualDetailEntity.getTitle());
        individualDetails.setFirstName(individualDetailEntity.getFirstName());
        individualDetails.setLastName(individualDetailEntity.getLastName());
        individualDetails.setPreferredHearingChannel(individualDetailEntity.getChannelType());
        individualDetails.setInterpreterLanguage(individualDetailEntity.getInterpreterLanguage());
        if (null != hearingPartyEntity.getReasonableAdjustmentsEntity()
                && !hearingPartyEntity.getReasonableAdjustmentsEntity().isEmpty()) {
            individualDetails.setReasonableAdjustments(
                    List.of(hearingPartyEntity.getReasonableAdjustmentsEntity().get(0)
                            .getReasonableAdjustmentCode()));
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
        return individualDetails;
    }

    private ArrayList<HearingResponse> setHearingResponse(HearingEntity hearingEntity) {
        ArrayList<HearingResponse> hearingResponses = new ArrayList<>();
        for (HearingResponseEntity hearingResponseEntity : hearingEntity.getHearingResponses()) {
            HearingResponse hearingResponse = new HearingResponse();
            hearingResponse.setListAssistTransactionID(
                 hearingResponseEntity.getListAssistTransactionId());
            hearingResponse.setReceivedDateTime(hearingResponseEntity.getRequestTimeStamp());
            hearingResponse.setResponseVersion(hearingResponseEntity.getHearingResponseId());
            hearingResponse.setLaCaseStatus(ListAssistCaseStatus.getLabel(
                    hearingResponseEntity.getListingCaseStatus()));
            hearingResponse.setListingStatus(ListingStatus.getLabel(hearingResponseEntity.getListingStatus()));
            hearingResponse.setHearingCancellationReason(hearingResponseEntity.getCancellationReasonType());
            setHearingDaySchedule(hearingResponse, List.of(hearingResponseEntity));
            hearingResponses.add(hearingResponse);
        }
        return hearingResponses;
    }


    private HearingDetails setHearingDetails(HearingEntity hearingEntity) {
        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutoListFlag(hearingEntity.getCaseHearingRequest().getAutoListFlag());
        hearingDetails.setHearingType(hearingEntity.getCaseHearingRequest().getHearingType());
        hearingDetails.setHearingWindow(setHearingWindow(hearingEntity));
        hearingDetails.setDuration(hearingEntity.getCaseHearingRequest().getRequiredDurationInMinutes());
        hearingDetails.setNonStandardHearingDurationReasons(setHearingPriorityType(hearingEntity));
        hearingDetails.setHearingPriorityType(hearingEntity.getCaseHearingRequest().getHearingPriorityType());
        hearingDetails.setNumberOfPhysicalAttendees(
            hearingEntity.getCaseHearingRequest().getNumberOfPhysicalAttendees());
        hearingDetails.setHearingInWelshFlag(hearingEntity.getCaseHearingRequest().getHearingInWelshFlag());
        hearingDetails.setHearingLocations(setHearingLocations(hearingEntity));
        hearingDetails.setFacilitiesRequired(setFacilityType(hearingEntity));
        hearingDetails.setListingComments(hearingEntity.getCaseHearingRequest().getListingComments());
        hearingDetails.setHearingRequester(hearingEntity.getCaseHearingRequest().getRequester());
        hearingDetails.setPrivateHearingRequiredFlag(
            hearingEntity.getCaseHearingRequest().getPrivateHearingRequiredFlag());
        hearingDetails.setLeadJudgeContractType(hearingEntity.getCaseHearingRequest().getLeadJudgeContractType());
        hearingDetails.setPanelRequirements(setPanelRequirements(hearingEntity));
        hearingDetails.setHearingIsLinkedFlag(hearingEntity.getIsLinkedFlag());
        return hearingDetails;
    }

    private ArrayList<String> setHearingPriorityType(HearingEntity hearingEntity) {
        ArrayList<String> hearingPriorityType = new ArrayList<>();
        if (null != hearingEntity.getCaseHearingRequest().getNonStandardDurations()
                && !hearingEntity.getCaseHearingRequest().getNonStandardDurations().isEmpty()) {
            for (NonStandardDurationsEntity nonStandardDurationsEntity
                    : hearingEntity.getCaseHearingRequest().getNonStandardDurations()) {
                hearingPriorityType.add(nonStandardDurationsEntity.getNonStandardHearingDurationReasonType());
            }
        }
        return hearingPriorityType;
    }

    private PanelRequirements setPanelRequirements(HearingEntity hearingEntity) {
        PanelRequirements panelRequirement = new PanelRequirements();
        if (null != hearingEntity.getCaseHearingRequest().getPanelRequirements()
                && !hearingEntity.getCaseHearingRequest().getPanelRequirements().isEmpty()) {
            for (PanelRequirementsEntity panelRequirementsEntity
                    : hearingEntity.getCaseHearingRequest().getPanelRequirements()) {
                panelRequirement.setRoleType(List.of(panelRequirementsEntity.getRoleType()));
            }
        }
        return panelRequirement;
    }

    private ArrayList<String> setFacilityType(HearingEntity hearingEntity) {
        ArrayList<String> facilityType = new ArrayList<>();
        if (null != hearingEntity.getCaseHearingRequest().getRequiredFacilities()
                && !hearingEntity.getCaseHearingRequest().getRequiredFacilities().isEmpty()) {
            for (RequiredFacilitiesEntity requiredFacilitiesEntity
                    : hearingEntity.getCaseHearingRequest().getRequiredFacilities()) {
                facilityType.add(requiredFacilitiesEntity.getFacilityType());
            }
        }
        return facilityType;
    }

    private ArrayList<HearingLocation> setHearingLocations(HearingEntity hearingEntity) {
        ArrayList<HearingLocation> hearingLocations = new ArrayList<>();
        if (null != hearingEntity.getCaseHearingRequest().getRequiredLocations()
                && !hearingEntity.getCaseHearingRequest().getRequiredLocations().isEmpty()) {
            for (RequiredLocationsEntity requiredLocationsEntity
                    : hearingEntity.getCaseHearingRequest().getRequiredLocations()) {
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
        hearingWindow.setHearingWindowStartDateRange(
            hearingEntity.getCaseHearingRequest().getHearingWindowStartDateRange());
        hearingWindow.setHearingWindowEndDateRange(
            hearingEntity.getCaseHearingRequest().getHearingWindowEndDateRange());
        hearingWindow.setFirstDateTimeMustBe(hearingEntity.getCaseHearingRequest().getFirstDateTimeOfHearingMustBe());
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
                    if (!detailEntity.getHearingDayPanel().isEmpty()) {
                        setHearingJudgeAndPanelMemberIds(detailEntity.getHearingDayPanel().get(0), hearingDaySchedule);
                    }
                    setAttendeeDetails(detailEntity.getHearingAttendeeDetails(), hearingDaySchedule);
                    scheduleList.add(hearingDaySchedule);
                }
            }
            caseHearing.setHearingDaySchedule(scheduleList);
        }
    }

}
