package uk.gov.hmcts.reform.hmc.helper;

import lombok.val;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.hmc.data.ActualAttendeeIndividualDetailEntity;
import uk.gov.hmcts.reform.hmc.data.ActualHearingDayEntity;
import uk.gov.hmcts.reform.hmc.data.ActualHearingDayPausesEntity;
import uk.gov.hmcts.reform.hmc.data.ActualHearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.HearingAttendeeDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.IndividualDetailEntity;
import uk.gov.hmcts.reform.hmc.model.PartyType;
import uk.gov.hmcts.reform.hmc.model.hearingactuals.ActualDayParty;
import uk.gov.hmcts.reform.hmc.model.hearingactuals.ActualHearingDays;
import uk.gov.hmcts.reform.hmc.model.hearingactuals.ActualIndividualDetails;
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
import java.util.Comparator;
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

        val hearingResponses = hearingEntity.getLatestHearingResponse();
        if (hearingResponses.isPresent() && hearingResponses.get().getActualHearingEntity() != null) {
            HearingResponseEntity hearingResponse = hearingResponses.get();
            if (hearingResponse.getActualHearingEntity() != null) {
                final var hearingOutcome = getHearingOutcome(hearingResponse);
                val hearingActual = new HearingActual();
                hearingActual.setHearingOutcome(hearingOutcome);
                getActualHearingDays(hearingResponse, hearingActual);

                response.setHearingActuals(hearingActual);
            }
        }

    }

    private static HearingOutcome getHearingOutcome(HearingResponseEntity hearingResponse) {
        val hearingOutcome = new HearingOutcome();
        hearingOutcome.setHearingType(hearingResponse.getActualHearingEntity().getActualHearingType());
        hearingOutcome.setHearingFinalFlag(
            hearingResponse.getActualHearingEntity().getActualHearingIsFinalFlag());
        hearingOutcome.setHearingResult(hearingResponse.getActualHearingEntity().getHearingResultType());
        hearingOutcome.setHearingResultReasonType(hearingResponse
                                                      .getActualHearingEntity().getHearingResultReasonType());
        hearingOutcome.setHearingResultDate(hearingResponse
                                                .getActualHearingEntity().getHearingResultDate());
        return hearingOutcome;
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
            actualHearingDay.setNotRequired(actualHearingDayEntity.getNotRequired());
            actualHearingDays.add(actualHearingDay);
        }
        actualHearingDays.sort(Comparator.comparing(ActualHearingDays::getHearingDate,
                                                    Comparator.nullsLast(Comparator.naturalOrder())));
        hearingActual.setActualHearingDays(actualHearingDays);
    }

    private void setActualDayParties(ActualHearingDayEntity actualHearingDayEntity,
                                     ActualHearingDays actualHearingDay) {
        List<ActualDayParty> actualDayParties = new ArrayList<>();
        for (ActualHearingPartyEntity actualHearingPartyEntity : actualHearingDayEntity.getActualHearingParty()) {
            ActualDayParty actualDayParty = new ActualDayParty();
            actualDayParty.setActualPartyId(actualHearingPartyEntity.getPartyId());
            actualDayParty.setPartyRole(actualHearingPartyEntity.getActualPartyRoleType());
            actualDayParty.setDidNotAttendFlag(actualHearingPartyEntity.getDidNotAttendFlag());
            if (!CollectionUtils.isEmpty(actualHearingPartyEntity.getActualPartyRelationshipDetail())) {
                // Only one represented party can be returned, so use the first
                actualDayParty.setRepresentedParty(actualHearingPartyEntity
                                                       .getActualPartyRelationshipDetail().getFirst()
                                                       .getTargetActualParty()
                                                       .getPartyId());
            }

            ActualAttendeeIndividualDetailEntity individualDetailEntity = actualHearingPartyEntity
                .getActualAttendeeIndividualDetail();
            actualDayParty.setPartyChannelSubType(individualDetailEntity.getPartyActualSubChannelType());

            ActualIndividualDetails individualDetails = new ActualIndividualDetails();
            individualDetails.setFirstName(individualDetailEntity.getFirstName());
            individualDetails.setLastName(individualDetailEntity.getLastName());
            actualDayParty.setActualIndividualDetails(individualDetails);
            actualDayParty.setActualOrganisationName(individualDetailEntity.getPartyOrganisationName());

            actualDayParties.add(actualDayParty);
        }
        actualDayParties.sort(Comparator.comparing(ActualDayParty::getPartyRole,
                                                   Comparator.nullsLast(Comparator.naturalOrder()))
                                  .thenComparing(ActualDayParty::getActualPartyId,
                                                 Comparator.nullsLast(Comparator.naturalOrder())));
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
        pauseDateTimes.sort(Comparator.comparing(PauseDateTimes::getPauseStartTime,
                                                 Comparator.nullsLast(Comparator.naturalOrder())));
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
                    plannedHearingDay.setParties(setPartyDetails(hearingEntity, hearingDayDetailsEntity));
                    plannedHearingDays.add(plannedHearingDay);
                }
            }
        }
        plannedHearingDays.sort(Comparator.comparing(PlannedHearingDays::getPlannedStartTime,
                                                     Comparator.nullsLast(Comparator.naturalOrder())));
        return plannedHearingDays;
    }

    private ArrayList<Party> setPartyDetails(HearingEntity hearingEntity,
                                             HearingDayDetailsEntity hearingDayDetailsEntity) {

        ArrayList<Party> partyDetailsList = new ArrayList<>();
        for (HearingAttendeeDetailsEntity hearingAttendeeDetails
            : hearingDayDetailsEntity.getHearingAttendeeDetails()) {

            //Check if there is at least one match for party reference/id in hearing attendee and hearing party
            HearingPartyEntity hearingPartyEntity = hearingEntity.getLatestCaseHearingRequest().getHearingParties()
                .stream()
                .filter(x -> x.getPartyReference().equals(hearingAttendeeDetails.getPartyId()))
                .findFirst()
                .orElse(null);

            //Check if Case Hearing Id match in the Case Hearing Request and Hearing Party
            boolean caseHearingIdMatches = hearingPartyEntity != null
                && hearingEntity.getLatestCaseHearingRequest().getCaseHearingID()
                    .equals(hearingPartyEntity.getCaseHearing().getCaseHearingID());

            Party partyDetails = new Party();
            partyDetails.setPartyID(hearingAttendeeDetails.getPartyId());
            partyDetails.setPartyChannelSubType(hearingAttendeeDetails.getPartySubChannelType());
            if (hearingPartyEntity != null && caseHearingIdMatches) {
                partyDetails.setPartyRole(hearingPartyEntity.getPartyRoleType());
                partyDetails.setPartyType(hearingPartyEntity.getPartyType().getLabel());
                if (PartyType.IND.getLabel().equals(hearingPartyEntity.getPartyType().getLabel())) {
                    partyDetails.setIndividualDetails(setIndividualDetails(hearingPartyEntity));
                } else {
                    partyDetails.setOrganisationDetails(setOrganisationDetails(hearingPartyEntity));
                }
            }
            partyDetailsList.add(partyDetails);
        }
        partyDetailsList.sort(Comparator.comparing(Party::getPartyRole, Comparator.nullsLast(Comparator.naturalOrder()))
                                  .thenComparing(Party::getPartyChannelSubType,
                                                 Comparator.nullsLast(Comparator.naturalOrder()))
                                  .thenComparing(Party::getPartyID, Comparator.nullsLast(Comparator.naturalOrder())));
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

    private IndividualDetails setIndividualDetails(HearingPartyEntity hearingPartyEntity) {
        IndividualDetails individualDetails = null;
        if (hearingPartyEntity.getIndividualDetailEntity() != null) {
            individualDetails = new IndividualDetails();
            IndividualDetailEntity individualDetailEntity = hearingPartyEntity.getIndividualDetailEntity();
            individualDetails.setTitle(individualDetailEntity.getTitle());
            individualDetails.setFirstName(individualDetailEntity.getFirstName());
            individualDetails.setLastName(individualDetailEntity.getLastName());
        }
        return individualDetails;
    }
}
