package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.ActualAttendeeIndividualDetailEntity;
import uk.gov.hmcts.reform.hmc.data.ActualHearingDayEntity;
import uk.gov.hmcts.reform.hmc.data.ActualHearingDayPausesEntity;
import uk.gov.hmcts.reform.hmc.data.ActualHearingEntity;
import uk.gov.hmcts.reform.hmc.data.ActualHearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.ActualPartyRelationshipDetailEntity;
import uk.gov.hmcts.reform.hmc.model.ActualHearingDay;
import uk.gov.hmcts.reform.hmc.model.ActualHearingDayParties;
import uk.gov.hmcts.reform.hmc.model.ActualHearingDayPartyDetail;
import uk.gov.hmcts.reform.hmc.model.ActualHearingDayPauseDayTime;
import uk.gov.hmcts.reform.hmc.model.ActualHearingOrganisationDetail;
import uk.gov.hmcts.reform.hmc.model.HearingActual;
import uk.gov.hmcts.reform.hmc.model.HearingResultType;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class HearingActualsMapper {

    public ActualHearingEntity toActualHearingEntity(HearingActual request) {
        ActualHearingEntity actualHearing = new ActualHearingEntity();

        actualHearing.setActualHearingType(request.getHearingOutcome().getHearingType());
        actualHearing.setActualHearingIsFinalFlag(request.getHearingOutcome().getHearingFinalFlag());
        actualHearing.setHearingResultType(HearingResultType.getByLabel(
            request.getHearingOutcome().getHearingResult()));
        actualHearing.setHearingResultReasonType(request.getHearingOutcome().getHearingResultReasonType());
        actualHearing.setHearingResultDate(request.getHearingOutcome().getHearingResultDate().atStartOfDay());
        actualHearing.setActualHearingDay(toActualHearingDayEntities(request.getActualHearingDays()));

        return actualHearing;
    }

    private List<ActualHearingDayEntity> toActualHearingDayEntities(List<ActualHearingDay> actualHearingDay) {
        return actualHearingDay.stream()
            .map(this::toActualHearingDayEntity)
            .collect(Collectors.toList());
    }

    private ActualHearingDayEntity toActualHearingDayEntity(ActualHearingDay actualHearingDay) {
        ActualHearingDayEntity actualHearingDayEntity = new ActualHearingDayEntity();

        actualHearingDayEntity.setHearingDate(actualHearingDay.getHearingDate());
        actualHearingDayEntity.setStartDateTime(actualHearingDay.getHearingStartTime());
        actualHearingDayEntity.setEndDateTime(actualHearingDay.getHearingEndTime());
        actualHearingDayEntity.setActualHearingDayPauses(
            toActualHearingDayPausesEntities(actualHearingDay.getPauseDateTimes()));
        actualHearingDayEntity.setActualHearingParty(
            toActualHearingPartyEntities(actualHearingDay.getActualDayParties()));

        return actualHearingDayEntity;
    }

    private List<ActualHearingDayPausesEntity> toActualHearingDayPausesEntities(
        List<ActualHearingDayPauseDayTime> actualHearingDayPauseDayTimes) {
        return actualHearingDayPauseDayTimes.stream()
            .map(this::toActualHearingDayPausesEntity)
            .collect(Collectors.toList());
    }

    private ActualHearingDayPausesEntity toActualHearingDayPausesEntity(ActualHearingDayPauseDayTime dayPauseDayTime) {
        ActualHearingDayPausesEntity dayPausesEntity = new ActualHearingDayPausesEntity();
        dayPausesEntity.setPauseDateTime(dayPauseDayTime.getPauseStartTime());
        dayPausesEntity.setResumeDateTime(dayPauseDayTime.getPauseEndTime());
        return dayPausesEntity;
    }

    private List<ActualHearingPartyEntity> toActualHearingPartyEntities(
        List<ActualHearingDayParties> actualDayParties) {
        return actualDayParties.stream()
            .map(this::toActualHearingPartyEntity)
            .collect(Collectors.toList());
    }

    private ActualHearingPartyEntity toActualHearingPartyEntity(ActualHearingDayParties actualHearingDayParty) {
        ActualHearingPartyEntity partyEntity = new ActualHearingPartyEntity();
        if (actualHearingDayParty.getActualPartyId() != null) {
            partyEntity.setActualPartyId(actualHearingDayParty.getActualPartyId());
        }
        partyEntity.setActualPartyRoleType(actualHearingDayParty.getPartyRole());
        partyEntity.setDidNotAttendFlag(actualHearingDayParty.getDidNotAttendFlag() != null
                                            ? actualHearingDayParty.getDidNotAttendFlag() : false);
        partyEntity.setActualAttendeeIndividualDetail(createIndividualDetail(actualHearingDayParty));
        partyEntity.setActualPartyRelationshipDetail(createActualPartyRelationshipDetailEntity(partyEntity));

        return partyEntity;
    }

    private List<ActualPartyRelationshipDetailEntity> createActualPartyRelationshipDetailEntity(
        ActualHearingPartyEntity partyEntity) {
        ActualPartyRelationshipDetailEntity partyRelationshipDetailEntity = new ActualPartyRelationshipDetailEntity();
        partyRelationshipDetailEntity.setActualHearingParty(partyEntity);
        partyRelationshipDetailEntity.setTargetActualPartyId(partyEntity.getActualPartyId());
        return List.of(partyRelationshipDetailEntity);
    }

    private List<ActualAttendeeIndividualDetailEntity> createIndividualDetail(
        ActualHearingDayParties actualHearingDayParty) {
        ActualAttendeeIndividualDetailEntity individualDetailEntity = new ActualAttendeeIndividualDetailEntity();

        ActualHearingDayPartyDetail individualDetails = actualHearingDayParty.getIndividualDetails();
        if (individualDetails != null) {
            individualDetailEntity.setFirstName(individualDetails.getFirstName());
            individualDetailEntity.setLastName(individualDetails.getLastName());
        }
        ActualHearingOrganisationDetail organisationDetails = actualHearingDayParty.getOrganisationDetails();
        if (organisationDetails != null) {
            individualDetailEntity.setPartyOrganisationName(organisationDetails.getName());
        }
        individualDetailEntity.setPartyActualSubChannelType(actualHearingDayParty.getPartyChannelSubType());

        return List.of(individualDetailEntity);
    }
}
