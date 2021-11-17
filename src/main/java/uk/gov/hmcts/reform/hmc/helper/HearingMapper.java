package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.CaseCategoriesEntity;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.IndividualDetailEntity;
import uk.gov.hmcts.reform.hmc.data.NonStandardDurationsEntity;
import uk.gov.hmcts.reform.hmc.data.OrganisationDetailEntity;
import uk.gov.hmcts.reform.hmc.data.PanelAuthorisationRequirementsEntity;
import uk.gov.hmcts.reform.hmc.data.PanelRequirementsEntity;
import uk.gov.hmcts.reform.hmc.data.PanelSpecialismsEntity;
import uk.gov.hmcts.reform.hmc.data.PanelUserRequirementsEntity;
import uk.gov.hmcts.reform.hmc.data.ReasonableAdjustmentsEntity;
import uk.gov.hmcts.reform.hmc.data.RequiredFacilitiesEntity;
import uk.gov.hmcts.reform.hmc.data.RequiredLocationsEntity;
import uk.gov.hmcts.reform.hmc.data.UnavailabilityEntity;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.IndividualDetails;
import uk.gov.hmcts.reform.hmc.model.OrganisationDetails;
import uk.gov.hmcts.reform.hmc.model.PanelPreference;
import uk.gov.hmcts.reform.hmc.model.PanelRequirements;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;

import java.util.List;

@Component
public class HearingMapper {

    private final CaseHearingRequestMapper caseHearingRequestMapper;

    private final NonStandardDurationsMapper nonStandardDurationsMapper;

    private final RequiredLocationsMapper requiredLocationsMapper;

    private final RequiredFacilitiesMapper requiredFacilitiesMapper;

    private final CaseCategoriesMapper caseCategoriesMapper;

    private final HearingPartyMapper hearingPartyMapper;

    private IndividualDetailMapper individualDetailMapper;

    private OrganisationDetailMapper organisationDetailMapper;

    private UnAvailabilityDetailMapper unAvailabilityDetailMapper;

    private ReasonableAdjustmentMapper reasonableAdjustmentMapper;

    private PanelRequirementsMapper panelRequirementsMapper;

    private PanelAuthorisationRequirementsMapper panelAuthorisationRequirementsMapper;

    private PanelSpecialismsMapper panelSpecialismsMapper;

    private PanelUserRequirementsMapper panelUserRequirementsMapper;

    public HearingMapper(CaseHearingRequestMapper caseHearingRequestMapper,
                         NonStandardDurationsMapper nonStandardDurationsMapper,
                         RequiredLocationsMapper requiredLocationsMapper,
                         RequiredFacilitiesMapper requiredFacilitiesMapper,
                         CaseCategoriesMapper caseCategoriesMapper,
                         HearingPartyMapper hearingPartyMapper,
                         IndividualDetailMapper individualDetailMapper,
                         OrganisationDetailMapper organisationDetailMapper,
                         UnAvailabilityDetailMapper unAvailabilityDetailMapper,
                         ReasonableAdjustmentMapper reasonableAdjustmentMapper,
                         PanelRequirementsMapper panelRequirementsMapper,
                         PanelAuthorisationRequirementsMapper panelAuthorisationRequirementsMapper,
                         PanelSpecialismsMapper panelSpecialismsMapper,
                         PanelUserRequirementsMapper panelUserRequirementsMapper) {
        this.caseHearingRequestMapper = caseHearingRequestMapper;
        this.nonStandardDurationsMapper = nonStandardDurationsMapper;
        this.requiredLocationsMapper = requiredLocationsMapper;
        this.requiredFacilitiesMapper = requiredFacilitiesMapper;
        this.caseCategoriesMapper = caseCategoriesMapper;
        this.hearingPartyMapper = hearingPartyMapper;
        this.individualDetailMapper = individualDetailMapper;
        this.organisationDetailMapper = organisationDetailMapper;
        this.unAvailabilityDetailMapper = unAvailabilityDetailMapper;
        this.reasonableAdjustmentMapper = reasonableAdjustmentMapper;
        this.panelRequirementsMapper = panelRequirementsMapper;
        this.panelAuthorisationRequirementsMapper = panelAuthorisationRequirementsMapper;
        this.panelSpecialismsMapper = panelSpecialismsMapper;
        this.panelUserRequirementsMapper = panelUserRequirementsMapper;
    }

    public HearingEntity modelToEntity(String status, HearingRequest hearingRequest) {
        final HearingEntity hearingEntity = new HearingEntity();
        CaseHearingRequestEntity caseHearingRequestEntity = caseHearingRequestMapper.modelToEntity(
            hearingRequest, hearingEntity);

        setNonStandardDurations(
            hearingRequest.getHearingDetails().getNonStandardHearingDurationReasons(),
            caseHearingRequestEntity
        );
        if (hearingRequest.getHearingDetails().getFacilitiesRequired() != null) {
            setRequiredFacilities(hearingRequest, caseHearingRequestEntity);
        }
        setCaseCategories(hearingRequest, caseHearingRequestEntity);
        setRequiredLocations(hearingRequest.getHearingDetails().getHearingLocations(), caseHearingRequestEntity);
        if(hearingRequest.getHearingDetails().getPanelRequirements().getRoleType() != null) {
            setPanelRequirements(hearingRequest.getHearingDetails().getPanelRequirements().getRoleType(),
                                 caseHearingRequestEntity);
        }
        setPanelAutorisationRequirements(hearingRequest.getHearingDetails().getPanelRequirements(),
                                         caseHearingRequestEntity );
        if(hearingRequest.getHearingDetails().getPanelRequirements().getPanelSpecialisms() != null) {
            setPanelSpecialisms(hearingRequest.getHearingDetails().getPanelRequirements().getPanelSpecialisms(),
                                 caseHearingRequestEntity);
        }
        if(hearingRequest.getHearingDetails().getPanelRequirements().getPanelPreferences() != null) {
            setPanelUserRequirements(hearingRequest.getHearingDetails().getPanelRequirements().getPanelPreferences(),
                                caseHearingRequestEntity);
        }


      /*  if (hearingRequest.getPartyDetails() != null) {
            List<HearingPartyEntity> hearingPartyEntities = new ArrayList<>();
            for (PartyDetails partyDetail : hearingRequest.getPartyDetails()) {
                HearingPartyEntity hearingPartyEntity = setHearingPartyDetails(partyDetail, caseHearingRequestEntity);
                hearingPartyEntities.add(hearingPartyEntity);

                if (partyDetail.getIndividualDetails() != null) {
                    setIndividualDetails(partyDetail.getIndividualDetails(), hearingPartyEntity);

                    if (partyDetail.getIndividualDetails().getReasonableAdjustments() != null) {
                       setReasonableAdjustments(partyDetail.getIndividualDetails().getReasonableAdjustments(), hearingPartyEntity );
                    }
                }
                if (partyDetail.getOrganisationDetails() != null) {
                    setOrganisationDetails(partyDetail.getOrganisationDetails(), hearingPartyEntity);
                }
                if (partyDetail.getUnavailabilityDow() != null || partyDetail.getUnavailabilityRanges() != null) {
                    setUnavailabilityDetails(partyDetail, hearingPartyEntity);
                }
            }
            caseHearingRequestEntity.setHearingParties(hearingPartyEntities);
        }*/

        hearingEntity.setStatus(status);
        hearingEntity.setCaseHearingRequest(caseHearingRequestEntity);
        return hearingEntity;
    }

    private void setPanelUserRequirements(List<PanelPreference> panelPreferences,
                                          CaseHearingRequestEntity caseHearingRequestEntity) {
        final List<PanelUserRequirementsEntity> panelUserRequirementsEntities =
            panelUserRequirementsMapper.modelToEntity(panelPreferences, caseHearingRequestEntity);
        caseHearingRequestEntity.setPanelUserRequirements(panelUserRequirementsEntities);
    }

    private void setPanelSpecialisms(List<String> panelSpecialisms, CaseHearingRequestEntity caseHearingRequestEntity) {
        final List<PanelSpecialismsEntity> panelSpecialismsEntities =
            panelSpecialismsMapper.modelToEntity(panelSpecialisms, caseHearingRequestEntity);
        caseHearingRequestEntity.setPanelSpecialisms(panelSpecialismsEntities);
    }

    private void setPanelAutorisationRequirements(PanelRequirements panelRequirements,
                                                  CaseHearingRequestEntity caseHearingRequestEntity) {
        final List<PanelAuthorisationRequirementsEntity> panelRequirementsEntities =
            panelAuthorisationRequirementsMapper.modelToEntity(panelRequirements, caseHearingRequestEntity);
        caseHearingRequestEntity.setPanelAuthorisationRequirements(panelRequirementsEntities);
    }

    private void setPanelRequirements(List<String> roleTypes, CaseHearingRequestEntity caseHearingRequestEntity) {
        final List<PanelRequirementsEntity> panelRequirementsEntities = panelRequirementsMapper.
            modelToEntity(roleTypes, caseHearingRequestEntity);
        caseHearingRequestEntity.setPanelRequirements(panelRequirementsEntities);
    }

    private void setReasonableAdjustments(List<String> reasonableAdjustments, HearingPartyEntity hearingPartyEntity) {
        final List<ReasonableAdjustmentsEntity> reasonableAdjustmentsEntities = reasonableAdjustmentMapper.
            modelToEntity(reasonableAdjustments, hearingPartyEntity);
        hearingPartyEntity.setReasonableAdjustmentsEntity(reasonableAdjustmentsEntities);
    }

    private void setUnavailabilityDetails(PartyDetails partyDetail, HearingPartyEntity hearingPartyEntity) {

        final List<UnavailabilityEntity> unavailabilityEntities = unAvailabilityDetailMapper.
            modelToEntity(partyDetail, hearingPartyEntity);
        hearingPartyEntity.setUnavailabilityEntity(unavailabilityEntities);
    }

    private void setOrganisationDetails(OrganisationDetails organisationDetails,
                                        HearingPartyEntity hearingPartyEntity) {
        final OrganisationDetailEntity organisationDetailEntity = organisationDetailMapper.
            modelToEntity(organisationDetails, hearingPartyEntity);
        hearingPartyEntity.setOrganisationDetailEntity(organisationDetailEntity);
    }

    private void setIndividualDetails(IndividualDetails individualDetails, HearingPartyEntity hearingPartyEntity) {
        final IndividualDetailEntity individualDetailEntity = individualDetailMapper.
            modelToEntity(individualDetails, hearingPartyEntity);
        hearingPartyEntity.setIndividualDetailEntity(individualDetailEntity);
    }

    private HearingPartyEntity setHearingPartyDetails(PartyDetails partyDetail,
                                                      CaseHearingRequestEntity caseHearingRequestEntity) {
        return hearingPartyMapper.modelToEntity(partyDetail, caseHearingRequestEntity);
    }

    private void setRequiredFacilities(HearingRequest hearingRequest,
                                       CaseHearingRequestEntity caseHearingRequestEntity) {
        final List<RequiredFacilitiesEntity> requiredFacilitiesEntities = requiredFacilitiesMapper.
            modelToEntity(hearingRequest, caseHearingRequestEntity);
        caseHearingRequestEntity.setRequiredFacilities(requiredFacilitiesEntities);
    }

    private void setNonStandardDurations(List<String> durations, CaseHearingRequestEntity caseHearingRequestEntity) {
        final List<NonStandardDurationsEntity> nonStandardDurationsEntities = nonStandardDurationsMapper.
            modelToEntity(durations, caseHearingRequestEntity);
        caseHearingRequestEntity.setNonStandardDurations(nonStandardDurationsEntities);
    }

    private void setRequiredLocations(List<HearingLocation> hearingLocations,
                                      CaseHearingRequestEntity caseHearingRequestEntity) {
        final List<RequiredLocationsEntity> requiredLocationsEntities = requiredLocationsMapper.
            modelToEntity(hearingLocations, caseHearingRequestEntity);
        caseHearingRequestEntity.setRequiredLocations(requiredLocationsEntities);
    }

    private void setCaseCategories(HearingRequest hearingRequest,
                                   CaseHearingRequestEntity caseHearingRequestEntity) {
        final List<CaseCategoriesEntity> caseCategoriesEntities = caseCategoriesMapper.
            modelToEntity(hearingRequest, caseHearingRequestEntity);
        caseHearingRequestEntity.setCaseCategories(caseCategoriesEntities);
    }

}

