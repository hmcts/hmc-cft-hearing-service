package uk.gov.hmcts.reform.hmc.helper;

import lombok.val;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.ActualAttendeeIndividualDetailEntity;
import uk.gov.hmcts.reform.hmc.data.ActualHearingDayEntity;
import uk.gov.hmcts.reform.hmc.data.ActualHearingDayPausesEntity;
import uk.gov.hmcts.reform.hmc.data.ActualHearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.ActualPartyRelationshipDetailEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.IndividualDetailEntity;
import uk.gov.hmcts.reform.hmc.model.PartyType;
import uk.gov.hmcts.reform.hmc.model.hearingactuals.ActualDayParty;
import uk.gov.hmcts.reform.hmc.model.hearingactuals.ActualHearingDays;
import uk.gov.hmcts.reform.hmc.model.hearingactuals.ActualIndividualDetails;
import uk.gov.hmcts.reform.hmc.model.hearingactuals.ActualOrganisationDetails;
import uk.gov.hmcts.reform.hmc.model.hearingactuals.HearingActual;
import uk.gov.hmcts.reform.hmc.model.hearingactuals.HearingActualResponse;
import uk.gov.hmcts.reform.hmc.model.hearingactuals.HearingOutcome;
import uk.gov.hmcts.reform.hmc.model.hearingactuals.HearingPlanned;
import uk.gov.hmcts.reform.hmc.model.hearingactuals.IndividualDetails;
import uk.gov.hmcts.reform.hmc.model.hearingactuals.OrganisationDetails;
import uk.gov.hmcts.reform.hmc.model.hearingactuals.Party;
import uk.gov.hmcts.reform.hmc.model.hearingactuals.PauseDateTimes;
import uk.gov.hmcts.reform.hmc.model.hearingactuals.PlannedHearingDays;

import java.util.ArrayList;
import java.util.List;

@Component
public class GetHearingActualsResponseMapper extends GetHearingResponseCommonCode {

    public HearingActualResponse toHearingActualResponse(HearingEntity hearingEntity) {
        val response = new HearingActualResponse();
        response.setHmcStatus(hearingEntity.getDerivedHearingStatus());
        response.setCaseDetails(setCaseDetails(hearingEntity));
        setHearingPlanned(hearingEntity, response);
        setHearingActuals(hearingEntity, response);
        return response;
    }

    private void setHearingActuals(HearingEntity hearingEntity, HearingActualResponse response) {
        val hearingActual = new HearingActual();

        val hearingResponses = hearingEntity.getLatestHearingResponse();
        if (hearingResponses.isPresent() && hearingResponses.get().getActualHearingEntity() != null) {
            HearingResponseEntity hearingResponse = hearingResponses.get();
            val hearingOutcome = new HearingOutcome();
            hearingOutcome.setHearingType(hearingResponse.getActualHearingEntity().getActualHearingType());
            hearingOutcome.setHearingFinalFlag(hearingResponse.getActualHearingEntity().getActualHearingIsFinalFlag());
            hearingOutcome.setHearingResult(hearingResponse.getActualHearingEntity().getHearingResultType());
            hearingOutcome.setHearingResultReasonType(hearingResponse
                                                          .getActualHearingEntity().getHearingResultReasonType());
            hearingOutcome.setHearingResultDate(hearingResponse
                                                    .getActualHearingEntity().getHearingResultDate());
            hearingActual.setHearingOutcome(hearingOutcome);
            getActualHearingDays(hearingResponse, hearingActual);
        }
        response.setHearingActuals(hearingActual);

    }

    private void getActualHearingDays(HearingResponseEntity hearingResponse, HearingActual hearingActual) {
        List<ActualHearingDays> actualHearingDays = new ArrayList<>();
        for (ActualHearingDayEntity actualHearingDayEntity :
            hearingResponse.getActualHearingEntity().getActualHearingDay()) {
            ActualHearingDays actualHearingDay = new ActualHearingDays();
            actualHearingDay.setHearingDate(actualHearingDayEntity.getHearingDate());
            actualHearingDay.setHearingStartTime(actualHearingDayEntity.getStartDateTime());
            actualHearingDay.setHearingEndTime(actualHearingDayEntity.getEndDateTime());
            setPauseDateTimes(actualHearingDayEntity, actualHearingDay);
            setActualDayParties(actualHearingDayEntity, actualHearingDay);
            actualHearingDays.add(actualHearingDay);
        }
        hearingActual.setActualHearingDays(actualHearingDays);
    }

    private void setActualDayParties(ActualHearingDayEntity actualHearingDayEntity,
                                     ActualHearingDays actualHearingDay) {
        List<ActualDayParty> actualDayParties = new ArrayList<>();
        for (ActualHearingPartyEntity actualHearingPartyEntity : actualHearingDayEntity.getActualHearingParty()) {
            ActualDayParty actualDayParty = new ActualDayParty();
            actualDayParty.setActualPartyId(actualHearingPartyEntity.getActualPartyId().intValue());
            actualDayParty.setPartyRole(actualHearingPartyEntity.getActualPartyRoleType());
            actualDayParty.setDidNotAttendFlag(actualHearingPartyEntity.getDidNotAttendFlag());
            for (ActualPartyRelationshipDetailEntity actualPartyRelationshipDetailEntity
                : actualHearingPartyEntity.getActualPartyRelationshipDetail()) {
                if (actualHearingPartyEntity.getActualPartyId()
                    .equals(actualPartyRelationshipDetailEntity.getActualHearingParty().getActualPartyId())) {
                    actualDayParty.setRepresentedParty(actualPartyRelationshipDetailEntity
                                                           .getTargetActualPartyId().toString());
                }
            }

            List<ActualIndividualDetails> individualDetailsList = new ArrayList<>();
            List<ActualOrganisationDetails> organisationDetailsList = new ArrayList<>();
            for (ActualAttendeeIndividualDetailEntity individualDetailEntity
                : actualHearingPartyEntity.getActualAttendeeIndividualDetail()) {
                actualDayParty.setPartyChannelSubType(individualDetailEntity.getPartyActualSubChannelType());
                if (individualDetailEntity.getPartyOrganisationName() == null) {
                    ActualIndividualDetails individualDetails = new ActualIndividualDetails();
                    individualDetails.setFirstName(individualDetailEntity.getFirstName());
                    individualDetails.setLastName(individualDetailEntity.getLastName());
                    individualDetailsList.add(individualDetails);

                } else {
                    ActualOrganisationDetails organisationDetails = new ActualOrganisationDetails();
                    organisationDetails.setName(individualDetailEntity.getPartyOrganisationName());
                    organisationDetailsList.add(organisationDetails);
                }
            }
            actualDayParty.setActualIndividualDetails(individualDetailsList);
            actualDayParty.setActualOrganisationDetails(organisationDetailsList);
            actualDayParties.add(actualDayParty);
        }
        actualHearingDay.setActualDayParties(actualDayParties);
    }

    private void setPauseDateTimes(ActualHearingDayEntity actualHearingDayEntity, ActualHearingDays actualHearingDay) {
        List<PauseDateTimes> pauseDateTimes = new ArrayList<>();
        for (ActualHearingDayPausesEntity actualHearingDayPauseEntity
            : actualHearingDayEntity.getActualHearingDayPauses()) {
            PauseDateTimes pauseDateTime = new PauseDateTimes();
            pauseDateTime.setPauseEndTime(actualHearingDayPauseEntity.getResumeDateTime());
            pauseDateTime.setPauseStartTime(actualHearingDayPauseEntity.getPauseDateTime());
            pauseDateTimes.add(pauseDateTime);
        }
        actualHearingDay.setPauseDateTimes(pauseDateTimes);
    }

    private void setHearingPlanned(HearingEntity hearingEntity, HearingActualResponse response) {
        val caseHearingRequestEntity = hearingEntity.getLatestCaseHearingRequest();
        val hearingPlanned = new HearingPlanned();
        hearingPlanned.setPlannedHearingType(caseHearingRequestEntity.getHearingType());
        hearingPlanned.setPlannedHearingDays(getPlannedHearingDays(hearingEntity));
        response.setHearingPlanned(hearingPlanned);
    }

    private List<PlannedHearingDays> getPlannedHearingDays(HearingEntity hearingEntity) {

        List<PlannedHearingDays> plannedHearingDays = new ArrayList<>();
        val hearingResponses = hearingEntity.getLatestHearingResponse();
        if (hearingResponses.isPresent()) {
            List<HearingDayDetailsEntity> hearingDayDetailEntities = hearingResponses.get().getHearingDayDetails();
            if (!hearingDayDetailEntities.isEmpty()) {
                for (HearingDayDetailsEntity hearingDayDetailsEntity : hearingDayDetailEntities) {
                    PlannedHearingDays plannedHearingDay = new PlannedHearingDays();
                    plannedHearingDay.setPlannedStartTime(hearingDayDetailsEntity.getStartDateTime());
                    plannedHearingDay.setPlannedEndTime(hearingDayDetailsEntity.getEndDateTime());
                    plannedHearingDay.setParties(setPartyDetails(hearingEntity));
                    plannedHearingDays.add(plannedHearingDay);
                }
            }

        }
        return plannedHearingDays;
    }

    private ArrayList<Party> setPartyDetails(HearingEntity hearingEntity) {

        ArrayList<Party> partyDetailsList = new ArrayList<>();
        for (HearingPartyEntity hearingPartyEntity : hearingEntity.getLatestCaseHearingRequest().getHearingParties()) {

            Party partyDetails = new Party();
            partyDetails.setPartyID(hearingPartyEntity.getPartyReference());
            partyDetails.setPartyRole(hearingPartyEntity.getPartyRoleType());
            //TBD separate ticket to be raised for subChannel
            partyDetails.setPartyChannelSubType(null);
            if (PartyType.IND.getLabel().equals(hearingPartyEntity.getPartyType().getLabel())) {
                partyDetails.setIndividualDetails(setIndividualDetails(hearingPartyEntity));
            } else {
                partyDetails.setOrganisationDetails(setOrganisationDetails(hearingPartyEntity));
            }
            partyDetailsList.add(partyDetails);
        }
        return partyDetailsList;
    }


    private OrganisationDetails setOrganisationDetails(HearingPartyEntity hearingPartyEntity) {
        OrganisationDetails organisationDetails = new OrganisationDetails();
        if (hearingPartyEntity.getOrganisationDetailEntity() != null) {
            organisationDetails.setName(hearingPartyEntity.getOrganisationDetailEntity().getOrganisationName());
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
                individualDetailsArrayList.add(individualDetails);
            }
        }
        return individualDetailsArrayList;
    }
}
