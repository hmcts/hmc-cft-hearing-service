package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.hmc.data.ActualAttendeeIndividualDetailEntity;
import uk.gov.hmcts.reform.hmc.data.ActualHearingDayEntity;
import uk.gov.hmcts.reform.hmc.data.ActualHearingDayPausesEntity;
import uk.gov.hmcts.reform.hmc.data.ActualHearingEntity;
import uk.gov.hmcts.reform.hmc.data.ActualHearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.ActualPartyRelationshipDetailEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
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

        List<ActualHearingPartyEntity> actualHearingPartyEntities = actualDayParties.stream()
            .map(actualHearingDayParty -> toActualHearingPartyEntity(
                actualHearingDayParty,
                dayEntity
            ))
            .collect(Collectors.toList());
        createActualPartyRelationshipDetailEntity(actualHearingPartyEntities, actualDayParties);
        return actualHearingPartyEntities;
    }

    private ActualHearingPartyEntity toActualHearingPartyEntity(ActualHearingDayParties actualHearingDayParty,
                                                                ActualHearingDayEntity dayEntity) {
        ActualHearingPartyEntity partyEntity = new ActualHearingPartyEntity();

        setOrGeneratePartyId(actualHearingDayParty, partyEntity);
        partyEntity.setActualPartyRoleType(actualHearingDayParty.getPartyRole());
        partyEntity.setDidNotAttendFlag(actualHearingDayParty.getDidNotAttendFlag() != null
                                            ? actualHearingDayParty.getDidNotAttendFlag() : false);
        partyEntity.setActualAttendeeIndividualDetail(createIndividualDetail(actualHearingDayParty, partyEntity));
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

    private List<ActualHearingPartyEntity> createActualPartyRelationshipDetailEntity(
        List<ActualHearingPartyEntity> hearingPartyEntities, List<ActualHearingDayParties> actualDayParties) {
        for (ActualHearingDayParties actualHearingDayParty : actualDayParties) {
            String representedPartyId = actualHearingDayParty.getRepresentedParty();
            if (representedPartyId != null) {
                ActualHearingPartyEntity matchingHearingPartyEntity =
                    getHearingPartyEntityByReference(representedPartyId, hearingPartyEntities);

                ActualHearingPartyEntity sourceEntity =
                    hearingPartyEntities.stream()
                        .filter(actualHearingPartyEntity ->
                                    actualHearingDayParty.getRepresentedParty().equals(representedPartyId))
                        .collect(Collectors.toList()).get(0);

                ActualPartyRelationshipDetailEntity partyRelationshipDetail = ActualPartyRelationshipDetailEntity
                    .builder()
                    .targetActualPartyId(matchingHearingPartyEntity)
                    .sourceActualPartyId(sourceEntity)
                    .build();

                sourceEntity.setSourcePartyRelationshipDetail(List.of(partyRelationshipDetail));
            }
        }
        return hearingPartyEntities;
    }

    private ActualHearingPartyEntity getHearingPartyEntityByReference(
        String representedPartyId, List<ActualHearingPartyEntity> hearingPartyEntities) {
        final List<ActualHearingPartyEntity> matchingHearingPartyEntities = hearingPartyEntities.stream()
            .filter(hearingPartyEntity -> representedPartyId.equals(hearingPartyEntity.getPartyId()))
            .collect(Collectors.toList());

        if (matchingHearingPartyEntities.size() != 1) {
            throw new BadRequestException(
                String.format("Cannot find unique PartyID with value %s", representedPartyId));
        }
        return matchingHearingPartyEntities.get(0);
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
