package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingChannelsEntity;
import uk.gov.hmcts.reform.hmc.data.NonStandardDurationsEntity;
import uk.gov.hmcts.reform.hmc.data.PanelAuthorisationRequirementsEntity;
import uk.gov.hmcts.reform.hmc.data.PanelRequirementsEntity;
import uk.gov.hmcts.reform.hmc.data.PanelSpecialismsEntity;
import uk.gov.hmcts.reform.hmc.data.PanelUserRequirementsEntity;
import uk.gov.hmcts.reform.hmc.data.RequiredFacilitiesEntity;
import uk.gov.hmcts.reform.hmc.data.RequiredLocationsEntity;
import uk.gov.hmcts.reform.hmc.model.HearingChannel;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
import uk.gov.hmcts.reform.hmc.model.PanelPreference;
import uk.gov.hmcts.reform.hmc.model.PanelRequirements;

import java.util.List;

@Component
public class HearingDetailsMapper {

    private final NonStandardDurationsMapper nonStandardDurationsMapper;

    private final RequiredLocationsMapper requiredLocationsMapper;

    private final RequiredFacilitiesMapper requiredFacilitiesMapper;

    private PanelRequirementsMapper panelRequirementsMapper;

    private PanelAuthorisationRequirementsMapper panelAuthorisationRequirementsMapper;

    private PanelSpecialismsMapper panelSpecialismsMapper;

    private PanelUserRequirementsMapper panelUserRequirementsMapper;

    private HearingChannelsMapper hearingChannelsMapper;

    @Autowired
    public HearingDetailsMapper(NonStandardDurationsMapper nonStandardDurationsMapper,
                                RequiredLocationsMapper requiredLocationsMapper,
                                RequiredFacilitiesMapper requiredFacilitiesMapper,
                                PanelRequirementsMapper panelRequirementsMapper,
                                PanelAuthorisationRequirementsMapper panelAuthorisationRequirementsMapper,
                                PanelSpecialismsMapper panelSpecialismsMapper,
                                PanelUserRequirementsMapper panelUserRequirementsMapper,
                                HearingChannelsMapper hearingChannelsMapper) {
        this.nonStandardDurationsMapper = nonStandardDurationsMapper;
        this.requiredLocationsMapper = requiredLocationsMapper;
        this.requiredFacilitiesMapper = requiredFacilitiesMapper;
        this.panelRequirementsMapper = panelRequirementsMapper;
        this.panelAuthorisationRequirementsMapper = panelAuthorisationRequirementsMapper;
        this.panelSpecialismsMapper = panelSpecialismsMapper;
        this.panelUserRequirementsMapper = panelUserRequirementsMapper;
        this.hearingChannelsMapper = hearingChannelsMapper;
    }

    public void mapHearingDetails(HearingDetails hearingDetails, CaseHearingRequestEntity caseHearingRequestEntity) {
        setRequiredLocations(hearingDetails.getHearingLocations(), caseHearingRequestEntity);
        setPanelDetails(hearingDetails, caseHearingRequestEntity);
        setHearingChannels(hearingDetails.getHearingChannels(), caseHearingRequestEntity);

        if (hearingDetails.getFacilitiesRequired() != null) {
            setRequiredFacilities(hearingDetails.getFacilitiesRequired(), caseHearingRequestEntity);
        }
        if (hearingDetails.getNonStandardHearingDurationReasons() != null) {
            setNonStandardDurations(
                hearingDetails.getNonStandardHearingDurationReasons(),
                caseHearingRequestEntity
            );
        }
    }

    private void setPanelDetails(HearingDetails hearingDetails, CaseHearingRequestEntity caseHearingRequestEntity) {
        if (hearingDetails.getPanelRequirements().getRoleType() != null) {
            setPanelRequirements(hearingDetails.getPanelRequirements().getRoleType(), caseHearingRequestEntity);
        }
        setPanelAuthorisationRequirements(hearingDetails.getPanelRequirements(), caseHearingRequestEntity);
        if (hearingDetails.getPanelRequirements().getPanelSpecialisms() != null) {
            setPanelSpecialisms(hearingDetails.getPanelRequirements().getPanelSpecialisms(), caseHearingRequestEntity);
        }
        if (hearingDetails.getPanelRequirements().getPanelPreferences() != null) {
            setPanelUserRequirements(
                hearingDetails.getPanelRequirements().getPanelPreferences(),
                caseHearingRequestEntity
            );
        }
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

    private void setPanelAuthorisationRequirements(PanelRequirements panelRequirements,
                                                   CaseHearingRequestEntity caseHearingRequestEntity) {
        final List<PanelAuthorisationRequirementsEntity> panelRequirementsEntities =
            panelAuthorisationRequirementsMapper.modelToEntity(panelRequirements, caseHearingRequestEntity);
        caseHearingRequestEntity.setPanelAuthorisationRequirements(panelRequirementsEntities);
    }

    private void setPanelRequirements(List<String> roleTypes, CaseHearingRequestEntity caseHearingRequestEntity) {
        final List<PanelRequirementsEntity> panelRequirementsEntities =
            panelRequirementsMapper.modelToEntity(roleTypes, caseHearingRequestEntity);
        caseHearingRequestEntity.setPanelRequirements(panelRequirementsEntities);
    }

    private void setRequiredFacilities(List<String> facilities,
                                       CaseHearingRequestEntity caseHearingRequestEntity) {
        final List<RequiredFacilitiesEntity> requiredFacilitiesEntities =
            requiredFacilitiesMapper.modelToEntity(facilities, caseHearingRequestEntity);
        caseHearingRequestEntity.setRequiredFacilities(requiredFacilitiesEntities);
    }

    private void setNonStandardDurations(List<String> durations, CaseHearingRequestEntity caseHearingRequestEntity) {
        final List<NonStandardDurationsEntity> nonStandardDurationsEntities =
            nonStandardDurationsMapper.modelToEntity(durations, caseHearingRequestEntity);
        caseHearingRequestEntity.setNonStandardDurations(nonStandardDurationsEntities);
    }

    private void setRequiredLocations(List<HearingLocation> hearingLocations,
                                      CaseHearingRequestEntity caseHearingRequestEntity) {
        final List<RequiredLocationsEntity> requiredLocationsEntities =
            requiredLocationsMapper.modelToEntity(hearingLocations, caseHearingRequestEntity);
        caseHearingRequestEntity.setRequiredLocations(requiredLocationsEntities);
    }

    private void setHearingChannels(List<HearingChannel> hearingChannels,
                                    CaseHearingRequestEntity caseHearingRequestEntity) {
        final List<HearingChannelsEntity> hearingChannelsEntities =
            hearingChannelsMapper.modelToEntity(hearingChannels, caseHearingRequestEntity);
        caseHearingRequestEntity.setHearingChannels(hearingChannelsEntities);
    }
}
