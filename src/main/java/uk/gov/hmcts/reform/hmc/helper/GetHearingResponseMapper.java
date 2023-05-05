package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.hmc.data.CancellationReasonsEntity;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.ContactDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingChannelsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.IndividualDetailEntity;
import uk.gov.hmcts.reform.hmc.data.NonStandardDurationsEntity;
import uk.gov.hmcts.reform.hmc.data.PanelAuthorisationRequirementsEntity;
import uk.gov.hmcts.reform.hmc.data.PanelRequirementsEntity;
import uk.gov.hmcts.reform.hmc.data.PanelSpecialismsEntity;
import uk.gov.hmcts.reform.hmc.data.PanelUserRequirementsEntity;
import uk.gov.hmcts.reform.hmc.data.ReasonableAdjustmentsEntity;
import uk.gov.hmcts.reform.hmc.data.RequiredFacilitiesEntity;
import uk.gov.hmcts.reform.hmc.data.RequiredLocationsEntity;
import uk.gov.hmcts.reform.hmc.data.UnavailabilityEntity;
import uk.gov.hmcts.reform.hmc.model.GetHearingResponse;
import uk.gov.hmcts.reform.hmc.model.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
import uk.gov.hmcts.reform.hmc.model.HearingWindow;
import uk.gov.hmcts.reform.hmc.model.IndividualDetails;
import uk.gov.hmcts.reform.hmc.model.OrganisationDetails;
import uk.gov.hmcts.reform.hmc.model.PanelPreference;
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
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.hmc.constants.Constants.EMAIL_TYPE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.UNAVAILABILITY_DOW_TYPE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.UNAVAILABILITY_RANGE_TYPE;

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
        requestDetails.setHearingRequestId(hearingEntity.getId().toString());
        requestDetails.setHearingGroupRequestId(getRequestId(hearingEntity));
        requestDetails.setStatus(hearingEntity.getDerivedHearingStatus());
        requestDetails.setTimestamp(caseHearingRequestEntity.getHearingRequestReceivedDateTime());
        requestDetails.setVersionNumber(caseHearingRequestEntity.getVersionNumber());
        if (null != caseHearingRequestEntity.getCancellationReasons()
            && !caseHearingRequestEntity.getCancellationReasons().isEmpty()) {
            List<String> cancelReasons = new ArrayList<>();
            for (CancellationReasonsEntity entity : caseHearingRequestEntity.getCancellationReasons()) {
                cancelReasons.add(entity.getCancellationReasonType());
            }
            requestDetails.setCancellationReasonCodes(cancelReasons);
        }
        return requestDetails;
    }

    private String getRequestId(HearingEntity hearingEntity) {
        if (null != hearingEntity && null != hearingEntity.getLinkedGroupDetails()) {
            return hearingEntity.getLinkedGroupDetails().getRequestId();
        }
        return null;
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
                partyDetails.setIndividualDetails(setIndividualDetails(hearingPartyEntity));
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
                if (unavailabilityEntity.getUnAvailabilityType().equalsIgnoreCase(UNAVAILABILITY_DOW_TYPE)) {
                    addUnavailabilityDow(unavailabilityEntity, unavailabilityDowArrayList);
                }
                if (unavailabilityEntity.getUnAvailabilityType().equalsIgnoreCase(UNAVAILABILITY_RANGE_TYPE)) {
                    addUnavailabilityRange(unavailabilityEntity, unavailabilityRangesArrayList);
                }
            }
        }
        partyDetails.setUnavailabilityDow(unavailabilityDowArrayList);
        partyDetails.setUnavailabilityRanges(unavailabilityRangesArrayList);
    }

    private void addUnavailabilityDow(UnavailabilityEntity unavailabilityEntity,
                                      ArrayList<UnavailabilityDow> unavailabilityDowArrayList) {
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

    private void addUnavailabilityRange(UnavailabilityEntity unavailabilityEntity,
                                        ArrayList<UnavailabilityRanges> unavailabilityRangesArrayList) {
        UnavailabilityRanges unavailabilityRanges = new UnavailabilityRanges();
        if (null != unavailabilityEntity.getEndDate()) {
            unavailabilityRanges.setUnavailableToDate(unavailabilityEntity.getEndDate());
        }
        if (null != unavailabilityEntity.getStartDate()) {
            unavailabilityRanges.setUnavailableFromDate(unavailabilityEntity.getStartDate());
        }
        if (null != unavailabilityEntity.getUnAvailabilityType()) {
            unavailabilityRanges.setUnavailabilityType(unavailabilityEntity.getDayOfWeekUnavailableType().getLabel());
        }

        unavailabilityRangesArrayList.add(unavailabilityRanges);
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

    private IndividualDetails setIndividualDetails(HearingPartyEntity hearingPartyEntity) {
        IndividualDetails individualDetails = null;
        if (hearingPartyEntity.getIndividualDetailEntity() != null) {
            IndividualDetailEntity individualDetailEntity = hearingPartyEntity.getIndividualDetailEntity();
            individualDetails = createIndividualDetail(hearingPartyEntity, individualDetailEntity);
        }
        return individualDetails;
    }

    private IndividualDetails createIndividualDetail(HearingPartyEntity hearingPartyEntity,
                                                     IndividualDetailEntity individualDetailEntity) {
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setTitle(individualDetailEntity.getTitle());
        individualDetails.setFirstName(individualDetailEntity.getFirstName());
        individualDetails.setLastName(individualDetailEntity.getLastName());
        individualDetails.setPreferredHearingChannel(individualDetailEntity.getChannelType());
        individualDetails.setInterpreterLanguage(individualDetailEntity.getInterpreterLanguage());
        individualDetails.setVulnerableFlag(individualDetailEntity.getVulnerableFlag());
        individualDetails.setVulnerabilityDetails(individualDetailEntity.getVulnerabilityDetails());
        individualDetails.setCustodyStatus(individualDetailEntity.getCustodyStatus());
        individualDetails.setOtherReasonableAdjustmentDetails(
            individualDetailEntity.getOtherReasonableAdjustmentDetails());
        setReasonableAdjustments(hearingPartyEntity, individualDetails);
        updateContactDetails(hearingPartyEntity, individualDetails);

        final List<RelatedParty> relatedParties = hearingPartyEntity.getPartyRelationshipDetailsEntity()
            .stream()
            .map(partyRelationshipDetailsEntity -> {
                RelatedParty relatedParty = new RelatedParty();
                relatedParty.setRelatedPartyID(partyRelationshipDetailsEntity.getTargetTechParty()
                                                   .getPartyReference());
                relatedParty.setRelationshipType(partyRelationshipDetailsEntity.getRelationshipType());
                return relatedParty;
            })
            .collect(Collectors.toList());

        individualDetails.setRelatedParties(relatedParties);
        return individualDetails;
    }

    private void updateContactDetails(HearingPartyEntity hearingPartyEntity, IndividualDetails individualDetails) {
        List<String> emails = new ArrayList<>();
        List<String> phoneNumbers = new ArrayList<>();
        if (null != hearingPartyEntity.getContactDetailsEntity()
            && !hearingPartyEntity.getContactDetailsEntity().isEmpty()) {
            for (ContactDetailsEntity contactDetailsEntity : hearingPartyEntity.getContactDetailsEntity()) {
                if (contactDetailsEntity.getContactType().equalsIgnoreCase(EMAIL_TYPE)) {
                    emails.add(contactDetailsEntity.getContactDetails());
                } else {
                    phoneNumbers.add(contactDetailsEntity.getContactDetails());
                }
            }
        }
        individualDetails.setHearingChannelEmail(emails);
        individualDetails.setHearingChannelPhone(phoneNumbers);
    }

    private HearingResponse setHearingResponse(HearingEntity hearingEntity) {
        HearingResponse hearingResponse = new HearingResponse();
        Optional<HearingResponseEntity> hearingResponseEntityOpt = hearingEntity.getLatestHearingResponse();
        if (hearingResponseEntityOpt.isPresent()) {
            HearingResponseEntity hearingResponseEntity = hearingResponseEntityOpt.get();
            hearingResponse.setListAssistTransactionID(
                hearingResponseEntity.getListAssistTransactionId());
            hearingResponse.setReceivedDateTime(hearingResponseEntity.getRequestTimeStamp());
            hearingResponse.setLaCaseStatus(hearingResponseEntity.getListingCaseStatus());
            if (hearingResponseEntity.getListingStatus() != null) {
                hearingResponse.setListingStatus(hearingResponseEntity.getListingStatus());
            }
            hearingResponse.setHearingCancellationReason(hearingResponseEntity.getCancellationReasonType());
            hearingResponse.setRequestVersion(hearingResponseEntity.getRequestVersion());
            setHearingDaySchedule(hearingResponse, List.of(hearingResponseEntity));
        }
        return hearingResponse;
    }


    private HearingDetails setHearingDetails(HearingEntity hearingEntity) {
        HearingDetails hearingDetails = new HearingDetails();
        CaseHearingRequestEntity caseHearingRequestEntity = hearingEntity.getLatestCaseHearingRequest();
        hearingDetails.setAutoListFlag(caseHearingRequestEntity.getAutoListFlag());
        hearingDetails.setListingAutoChangeReasonCode(caseHearingRequestEntity.getListingAutoChangeReasonCode());
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
        hearingDetails.setHearingChannels(setHearingChannel(caseHearingRequestEntity));
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

        setRoleTypes(caseHearingRequestEntity, panelRequirement);
        setPanelPreferences(caseHearingRequestEntity, panelRequirement);
        setPanelSpecialisms(caseHearingRequestEntity, panelRequirement);
        setAuthorisationTypes(caseHearingRequestEntity, panelRequirement);
        return panelRequirement;
    }

    private void setRoleTypes(CaseHearingRequestEntity caseHearingRequestEntity,
                              PanelRequirements panelRequirement) {
        ArrayList<String> roleTypes = new ArrayList<>();
        if (null != caseHearingRequestEntity.getPanelRequirements()
            && !caseHearingRequestEntity.getPanelRequirements().isEmpty()) {
            for (PanelRequirementsEntity panelRequirementsEntity
                : caseHearingRequestEntity.getPanelRequirements()) {
                roleTypes.add(panelRequirementsEntity.getRoleType());
            }
        }
        panelRequirement.setRoleType(roleTypes);
    }

    private void setPanelPreferences(CaseHearingRequestEntity caseHearingRequestEntity,
                                     PanelRequirements panelRequirement) {
        ArrayList<PanelPreference> panelPreferences = new ArrayList<>();
        if (null != caseHearingRequestEntity.getPanelUserRequirements()
            && !caseHearingRequestEntity.getPanelUserRequirements().isEmpty()) {
            for (PanelUserRequirementsEntity panelUserRequirements
                : caseHearingRequestEntity.getPanelUserRequirements()) {
                PanelPreference panelPreference = new PanelPreference();
                if (panelUserRequirements.getJudicialUserId() != null) {
                    panelPreference.setMemberID(panelUserRequirements.getJudicialUserId());
                }
                if (panelUserRequirements.getUserType() != null) {
                    panelPreference.setMemberType(panelUserRequirements.getUserType());
                }
                if (panelUserRequirements.getRequirementType() != null) {
                    panelPreference.setRequirementType(panelUserRequirements.getRequirementType().getLabel());
                }
                panelPreferences.add(panelPreference);
            }
        }
        panelRequirement.setPanelPreferences(panelPreferences);
    }

    private void setPanelSpecialisms(CaseHearingRequestEntity caseHearingRequestEntity,
                                     PanelRequirements panelRequirement) {
        ArrayList<String> panelSpecialisms = new ArrayList<>();
        if (null != caseHearingRequestEntity.getPanelSpecialisms()
            && !caseHearingRequestEntity.getPanelSpecialisms().isEmpty()) {
            for (PanelSpecialismsEntity panelRequirementsEntity
                : caseHearingRequestEntity.getPanelSpecialisms()) {
                panelSpecialisms.add(panelRequirementsEntity.getSpecialismType());
            }
        }
        panelRequirement.setPanelSpecialisms(panelSpecialisms);
    }

    private void setAuthorisationTypes(CaseHearingRequestEntity caseHearingRequestEntity,
                                       PanelRequirements panelRequirement) {
        ArrayList<String> authorisationTypes = new ArrayList<>();
        ArrayList<String> authorisationSubType = new ArrayList<>();
        if (null != caseHearingRequestEntity.getPanelAuthorisationRequirements()
            && !caseHearingRequestEntity.getPanelAuthorisationRequirements().isEmpty()) {
            for (PanelAuthorisationRequirementsEntity panelAuthorisationRequirements
                : caseHearingRequestEntity.getPanelAuthorisationRequirements()) {
                if (panelAuthorisationRequirements.getAuthorisationType() != null) {
                    authorisationTypes.add(panelAuthorisationRequirements.getAuthorisationType());
                }
            }
            for (PanelAuthorisationRequirementsEntity panelRequirementsEntity
                : caseHearingRequestEntity.getPanelAuthorisationRequirements()) {
                if (panelRequirementsEntity.getAuthorisationSubType() != null) {
                    authorisationSubType.add(panelRequirementsEntity.getAuthorisationSubType());
                }
            }
        }
        panelRequirement.setAuthorisationTypes(authorisationTypes);
        panelRequirement.setAuthorisationSubType(authorisationSubType);
    }

    private void setReasonableAdjustments(HearingPartyEntity hearingPartyEntity,
                                          IndividualDetails individualDetails) {
        List<String> reasonableAdjustmentCodeList = new ArrayList<>();
        if (hearingPartyEntity.getReasonableAdjustmentsEntity() != null
            && !hearingPartyEntity.getReasonableAdjustmentsEntity().isEmpty()) {
            for (ReasonableAdjustmentsEntity reasonableAdjustments
                : hearingPartyEntity.getReasonableAdjustmentsEntity()) {
                reasonableAdjustmentCodeList.add(reasonableAdjustments.getReasonableAdjustmentCode());
            }
        }
        individualDetails.setReasonableAdjustments(reasonableAdjustmentCodeList);
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
                hearingLocation.setLocationId(requiredLocationsEntity.getLocationId());
                hearingLocation.setLocationType(requiredLocationsEntity.getLocationLevelType().getLabel());
                hearingLocations.add(hearingLocation);
            }
        }
        return hearingLocations;
    }

    private List<String> setHearingChannel(CaseHearingRequestEntity caseHearingRequestEntity) {
        List<String> hearingChannels = new ArrayList<>();
        for (HearingChannelsEntity hearingChannelsEntity : caseHearingRequestEntity.getHearingChannels()) {
            hearingChannels.add(hearingChannelsEntity.getHearingChannelType());
        }

        return hearingChannels;
    }

    private HearingWindow setHearingWindow(HearingEntity hearingEntity) {
        HearingWindow hearingWindow = new HearingWindow();
        CaseHearingRequestEntity caseHearingRequestEntity = hearingEntity.getLatestCaseHearingRequest();
        hearingWindow.setDateRangeStart(
            caseHearingRequestEntity.getHearingWindowStartDateRange());
        hearingWindow.setDateRangeEnd(
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
                    if (!CollectionUtils.isEmpty(detailEntity.getHearingDayPanel())) {
                        setHearingJudgeAndPanelMemberIds(detailEntity.getHearingDayPanel(), hearingDaySchedule);
                    }
                    setAttendeeDetails(detailEntity.getHearingAttendeeDetails(), hearingDaySchedule);
                    scheduleList.add(hearingDaySchedule);
                }
            }
            caseHearing.setHearingDaySchedule(scheduleList);
        }
    }

}
