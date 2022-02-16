package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;
import uk.gov.hmcts.reform.hmc.data.ContactDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.IndividualDetailEntity;
import uk.gov.hmcts.reform.hmc.data.OrganisationDetailEntity;
import uk.gov.hmcts.reform.hmc.data.ReasonableAdjustmentsEntity;
import uk.gov.hmcts.reform.hmc.data.UnavailabilityEntity;
import uk.gov.hmcts.reform.hmc.model.IndividualDetails;
import uk.gov.hmcts.reform.hmc.model.OrganisationDetails;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;

import java.util.ArrayList;
import java.util.List;

@Component
public class PartyDetailMapper {

    private final HearingPartyMapper hearingPartyMapper;

    private OrganisationDetailMapper organisationDetailMapper;

    private UnAvailabilityDetailMapper unAvailabilityDetailMapper;

    private IndividualDetailMapper individualDetailMapper;

    private ContactDetailMapper contactDetailMapper;

    private ReasonableAdjustmentMapper reasonableAdjustmentMapper;

    @Autowired
    public PartyDetailMapper(HearingPartyMapper hearingPartyMapper,
                             OrganisationDetailMapper organisationDetailMapper,
                             UnAvailabilityDetailMapper unAvailabilityDetailMapper,
                             IndividualDetailMapper individualDetailMapper,
                             ContactDetailMapper contactDetailMapper,
                             ReasonableAdjustmentMapper reasonableAdjustmentMapper) {
        this.hearingPartyMapper = hearingPartyMapper;
        this.organisationDetailMapper = organisationDetailMapper;
        this.unAvailabilityDetailMapper = unAvailabilityDetailMapper;
        this.individualDetailMapper = individualDetailMapper;
        this.contactDetailMapper = contactDetailMapper;
        this.reasonableAdjustmentMapper = reasonableAdjustmentMapper;

    }

    public void mapPartyDetails(List<PartyDetails> partyDetails, CaseHearingRequestEntity caseHearingRequestEntity) {
        if (partyDetails != null) {
            List<HearingPartyEntity> hearingPartyEntities = new ArrayList<>();
            for (PartyDetails partyDetail : partyDetails) {
                HearingPartyEntity hearingPartyEntity = setHearingPartyDetails(partyDetail, caseHearingRequestEntity);
                setAllIndividualDetails(partyDetail, hearingPartyEntity);
                setOrganisationDetails(partyDetail.getOrganisationDetails(), hearingPartyEntity);
                setUnavailabilityDetails(partyDetail, hearingPartyEntity);
                hearingPartyEntities.add(hearingPartyEntity);
            }
            if (caseHearingRequestEntity.getCaseHearingID() != null) {
                caseHearingRequestEntity.getHearingParties().clear();
                caseHearingRequestEntity.getHearingParties().addAll(hearingPartyEntities);
            } else {
                caseHearingRequestEntity.setHearingParties(hearingPartyEntities);
            }
        }
    }

    private HearingPartyEntity setHearingPartyDetails(PartyDetails partyDetail,
                                                      CaseHearingRequestEntity caseHearingRequestEntity) {
        return hearingPartyMapper.modelToEntity(partyDetail, caseHearingRequestEntity);
    }

    private void setAllIndividualDetails(PartyDetails partyDetail, HearingPartyEntity hearingPartyEntity) {
        if (partyDetail.getIndividualDetails() != null) {
            IndividualDetails individualDetails = partyDetail.getIndividualDetails();
            setIndividualDetails(individualDetails, hearingPartyEntity);
            setReasonableAdjustments(individualDetails.getReasonableAdjustments(), hearingPartyEntity);
            setContactDetails(individualDetails, hearingPartyEntity);
        }
    }

    private void setOrganisationDetails(OrganisationDetails organisationDetails,
                                        HearingPartyEntity hearingPartyEntity) {
        if (organisationDetails != null) {
            final OrganisationDetailEntity organisationDetailEntity =
                organisationDetailMapper.modelToEntity(organisationDetails, hearingPartyEntity);
            hearingPartyEntity.setOrganisationDetailEntity(organisationDetailEntity);
        }
    }

    private void setUnavailabilityDetails(PartyDetails partyDetail, HearingPartyEntity hearingPartyEntity) {
        if (partyDetail.getUnavailabilityDow() != null || partyDetail.getUnavailabilityRanges() != null) {
            final List<UnavailabilityEntity> unavailabilityEntities =
                unAvailabilityDetailMapper.modelToEntity(partyDetail, hearingPartyEntity);
            if (hearingPartyEntity.getTechPartyId() != null) {
                hearingPartyEntity.getUnavailabilityEntity().clear();
                hearingPartyEntity.getUnavailabilityEntity().addAll(unavailabilityEntities);
            } else {
                hearingPartyEntity.setUnavailabilityEntity(unavailabilityEntities);
            }
        }
    }

    private void setIndividualDetails(IndividualDetails individualDetails, HearingPartyEntity hearingPartyEntity) {
        final List<IndividualDetailEntity> individualDetailEntity =
            individualDetailMapper.modelToEntity(individualDetails, hearingPartyEntity);
        if (hearingPartyEntity.getTechPartyId() != null) {
            hearingPartyEntity.getIndividualDetailEntity().clear();
            hearingPartyEntity.getIndividualDetailEntity().addAll(individualDetailEntity);
        } else {
            hearingPartyEntity.setIndividualDetailEntity(individualDetailEntity);
        }
    }

    private void setReasonableAdjustments(List<String> reasonableAdjustments, HearingPartyEntity hearingPartyEntity) {
        if (reasonableAdjustments != null) {
            final List<ReasonableAdjustmentsEntity> reasonableAdjustmentsEntities =
                reasonableAdjustmentMapper.modelToEntity(reasonableAdjustments, hearingPartyEntity);
            if (hearingPartyEntity.getTechPartyId() != null) {
                hearingPartyEntity.getReasonableAdjustmentsEntity().clear();
                hearingPartyEntity.getReasonableAdjustmentsEntity().addAll(reasonableAdjustmentsEntities);
            } else {
                hearingPartyEntity.setReasonableAdjustmentsEntity(reasonableAdjustmentsEntities);
            }
        }
    }

    private void setContactDetails(IndividualDetails individualDetails, HearingPartyEntity hearingPartyEntity) {
        final List<ContactDetailsEntity> contactDetailsEntity =
            contactDetailMapper.modelToEntity(individualDetails, hearingPartyEntity);
        if (hearingPartyEntity.getTechPartyId() != null) {
            hearingPartyEntity.getContactDetails().clear();
            hearingPartyEntity.getContactDetails().addAll(contactDetailsEntity);
        } else {
            hearingPartyEntity.setContactDetails(contactDetailsEntity);
        }

    }
}
