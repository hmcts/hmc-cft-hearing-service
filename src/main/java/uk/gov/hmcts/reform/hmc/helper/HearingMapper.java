package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.CaseCategoriesEntity;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.NonStandardDurationsEntity;
import uk.gov.hmcts.reform.hmc.data.RequiredFacilitiesEntity;
import uk.gov.hmcts.reform.hmc.data.RequiredLocationsEntity;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
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

    public HearingMapper(CaseHearingRequestMapper caseHearingRequestMapper,
                         NonStandardDurationsMapper nonStandardDurationsMapper,
                         RequiredLocationsMapper requiredLocationsMapper,
                         RequiredFacilitiesMapper requiredFacilitiesMapper,
                         CaseCategoriesMapper caseCategoriesMapper,
                         HearingPartyMapper hearingPartyMapper) {
        this.caseHearingRequestMapper = caseHearingRequestMapper;
        this.nonStandardDurationsMapper = nonStandardDurationsMapper;
        this.requiredLocationsMapper = requiredLocationsMapper;
        this.requiredFacilitiesMapper = requiredFacilitiesMapper;
        this.caseCategoriesMapper = caseCategoriesMapper;
        this.hearingPartyMapper = hearingPartyMapper;
    }

    public HearingEntity modelToEntity(String status, HearingRequest hearingRequest) {
        final HearingEntity hearingEntity = new HearingEntity();
        CaseHearingRequestEntity caseHearingRequestEntity = caseHearingRequestMapper.modelToEntity(
            hearingRequest, hearingEntity);

        setNonStandardDurations(hearingRequest.getHearingDetails().getNonStandardHearingDurationReasons(), caseHearingRequestEntity);
        // setRequiredLocations(hearingRequest.getHearingDetails().getHearingLocations(), caseHearingRequestEntity);
        if (hearingRequest.getHearingDetails().getFacilitiesRequired() != null) {
            // TO DO not inserting
            setRequiredFacilities(hearingRequest, caseHearingRequestEntity);
        }
        setCaseCategories(hearingRequest, caseHearingRequestEntity);
        setHearingParty(hearingRequest.getPartyDetails(), caseHearingRequestEntity);
        hearingEntity.setStatus(status);
        hearingEntity.setCaseHearingRequest(caseHearingRequestEntity);
        return hearingEntity;
    }

    private void setHearingParty(List<PartyDetails> partyDetails, CaseHearingRequestEntity caseHearingRequestEntity) {
        final List<HearingPartyEntity> hearingPartyEntities = hearingPartyMapper.
            modelToEntity(partyDetails, caseHearingRequestEntity);
        caseHearingRequestEntity.setHearingParties(hearingPartyEntities);
    }

    private void setRequiredFacilities(HearingRequest hearingRequest,
                                       CaseHearingRequestEntity caseHearingRequestEntity) {
        final List<RequiredFacilitiesEntity> requiredFacilitiesEntities = requiredFacilitiesMapper.
            modelToEntity(hearingRequest, caseHearingRequestEntity);
        caseHearingRequestEntity.setRequiredFacilities(requiredFacilitiesEntities);
    }

    private void setNonStandardDurations(List<String> durations, CaseHearingRequestEntity caseHearingRequestEntity) {
        final List<NonStandardDurationsEntity> nonStandardDurationsEntities = nonStandardDurationsMapper.
            modelToEntity(durations,
                          caseHearingRequestEntity);
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

