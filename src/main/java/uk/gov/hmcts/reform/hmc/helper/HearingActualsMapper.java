package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
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
        actualHearing.setHearingResultDate(request.getHearingOutcome().getHearingResultDate());
        actualHearing.setActualHearingDay(toActualHearingDayEntities(request.getActualHearingDays(), actualHearing));

        return actualHearing;
    }

    private List<ActualHearingDayEntity> toActualHearingDayEntities(List<ActualHearingDay> actualHearingDay,
                                                                   ActualHearingEntity actualHearing) {
        return actualHearingDay.stream()
            .map((ActualHearingDay day) -> toActualHearingDayEntity(day, actualHearing))
            .collect(Collectors.toList());
    }

    private ActualHearingDayEntity toActualHearingDayEntity(ActualHearingDay actualHearingDay,
                                                            ActualHearingEntity actualHearing) {
        ActualHearingDayEntity actualHearingDayEntity = new ActualHearingDayEntity();

        actualHearingDayEntity.setHearingDate(actualHearingDay.getHearingDate());
        actualHearingDayEntity.setStartDateTime(actualHearingDay.getHearingStartTime());
        actualHearingDayEntity.setEndDateTime(actualHearingDay.getHearingEndTime());
        actualHearingDayEntity.setActualHearingDayPauses(
            toActualHearingDayPausesEntities(actualHearingDay.getPauseDateTimes(), actualHearingDayEntity));
        actualHearingDayEntity.setActualHearingParty(
            toActualHearingPartyEntities(actualHearingDay.getActualDayParties(), actualHearingDayEntity));
        actualHearingDayEntity.setActualHearing(actualHearing);
        return actualHearingDayEntity;
    }

    private List<ActualHearingDayPausesEntity> toActualHearingDayPausesEntities(
        List<ActualHearingDayPauseDayTime> dayPauseDayTimes, ActualHearingDayEntity dayEntity) {
        if (CollectionUtils.isEmpty(dayPauseDayTimes)) {
            return List.of();
        }
        return dayPauseDayTimes.stream()
            .map(dayPauseDayTime -> toActualHearingDayPausesEntity(dayPauseDayTime, dayEntity))
            .collect(Collectors.toList());
    }

    private ActualHearingDayPausesEntity toActualHearingDayPausesEntity(ActualHearingDayPauseDayTime dayPauseDayTime,
                                                                        ActualHearingDayEntity dayEntity) {
        ActualHearingDayPausesEntity dayPausesEntity = new ActualHearingDayPausesEntity();
        dayPausesEntity.setPauseDateTime(dayPauseDayTime.getPauseStartTime());
        dayPausesEntity.setResumeDateTime(dayPauseDayTime.getPauseEndTime());
        dayPausesEntity.setActualHearingDay(dayEntity);
        return dayPausesEntity;
    }

    private List<ActualHearingPartyEntity> toActualHearingPartyEntities(
        List<ActualHearingDayParties> actualDayParties, ActualHearingDayEntity dayEntity) {
        if (CollectionUtils.isEmpty(actualDayParties)) {
            return List.of();
        }
        return actualDayParties.stream()
            .map(actualHearingDayParty -> toActualHearingPartyEntity(actualHearingDayParty, dayEntity))
            .collect(Collectors.toList());
    }

    private ActualHearingPartyEntity toActualHearingPartyEntity(ActualHearingDayParties actualHearingDayParty,
                                                                ActualHearingDayEntity dayEntity) {
        ActualHearingPartyEntity partyEntity = new ActualHearingPartyEntity();

        setOrGeneratePartyId(actualHearingDayParty, partyEntity);
        partyEntity.setActualPartyRoleType(actualHearingDayParty.getPartyRole());
        partyEntity.setDidNotAttendFlag(actualHearingDayParty.getDidNotAttendFlag() != null
                                            ? actualHearingDayParty.getDidNotAttendFlag() : false);
        partyEntity.setActualAttendeeIndividualDetail(createIndividualDetail(actualHearingDayParty, partyEntity));
        if (actualHearingDayParty.getRepresentedParty() != null) {
            partyEntity.setActualPartyRelationshipDetail(
                createActualPartyRelationshipDetailEntity(partyEntity, actualHearingDayParty.getRepresentedParty()));
        }
        partyEntity.setActualHearingDay(dayEntity);
        return partyEntity;
    }

    private void setOrGeneratePartyId(ActualHearingDayParties actualHearingDayParty,
                                      ActualHearingPartyEntity partyEntity) {
        if (actualHearingDayParty.getActualPartyId() == null) {
            if (actualHearingDayParty.getIndividualDetails() != null) {
                partyEntity.setPartyId(String.valueOf(actualHearingDayParty.getIndividualDetails().hashCode()));
            } else if (actualHearingDayParty.getOrganisationDetails() != null) {
                partyEntity.setPartyId(String.valueOf(actualHearingDayParty.getOrganisationDetails().hashCode()));
            }
        } else {
            partyEntity.setPartyId(actualHearingDayParty.getActualPartyId());
        }
    }

    private List<ActualPartyRelationshipDetailEntity> createActualPartyRelationshipDetailEntity(
        ActualHearingPartyEntity partyEntity, String representedPartyId) {
        ActualPartyRelationshipDetailEntity partyRelationshipDetailEntity = new ActualPartyRelationshipDetailEntity();
        partyRelationshipDetailEntity.setActualHearingParty(partyEntity);
        partyRelationshipDetailEntity.setTargetActualPartyId(Long.parseLong(representedPartyId));
        return List.of(partyRelationshipDetailEntity);
    }

    private List<ActualAttendeeIndividualDetailEntity> createIndividualDetail(
        ActualHearingDayParties actualHearingDayParty, ActualHearingPartyEntity partyEntity) {
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
        individualDetailEntity.setActualHearingParty(partyEntity);

        return List.of(individualDetailEntity);
    }
}
