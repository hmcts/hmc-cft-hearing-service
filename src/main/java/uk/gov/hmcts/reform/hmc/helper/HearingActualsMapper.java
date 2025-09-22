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
import uk.gov.hmcts.reform.hmc.model.HearingActual;
import uk.gov.hmcts.reform.hmc.model.HearingResultType;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HearingActualsMapper {

    public ActualHearingEntity toActualHearingEntity(HearingActual request) {
        ActualHearingEntity actualHearing = new ActualHearingEntity();

        if (null != request.getHearingOutcome()) {
            if (null != request.getHearingOutcome().getHearingType()) {
                actualHearing.setActualHearingType(request.getHearingOutcome().getHearingType());
            }
            if (null != request.getHearingOutcome().getHearingFinalFlag()) {
                actualHearing.setActualHearingIsFinalFlag(request.getHearingOutcome().getHearingFinalFlag());
            }
            if (null != request.getHearingOutcome().getHearingResult()) {
                actualHearing.setHearingResultType(HearingResultType.getByLabel(
                        request.getHearingOutcome().getHearingResult()));
            }
            if (null != request.getHearingOutcome().getHearingResultReasonType()) {
                actualHearing.setHearingResultReasonType(request.getHearingOutcome().getHearingResultReasonType());
            }
            if (null != request.getHearingOutcome().getHearingResultDate()) {
                actualHearing.setHearingResultDate(request.getHearingOutcome().getHearingResultDate());
            }
        }
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
        actualHearingDayEntity.setNotRequired(actualHearingDay.getNotRequired());
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
        actualHearingPartyEntities.sort(Comparator.comparing(ActualHearingPartyEntity::getActualPartyRoleType,
                                                           Comparator.nullsLast(String::compareTo))
                                            .thenComparing(ActualHearingPartyEntity::getActualPartyId,
                                                           Comparator.nullsLast(Long::compareTo)));
        createActualPartyRelationshipDetailEntity(actualHearingPartyEntities, actualDayParties);
        return actualHearingPartyEntities;
    }

    private ActualHearingPartyEntity toActualHearingPartyEntity(ActualHearingDayParties actualHearingDayParty,
                                                                ActualHearingDayEntity dayEntity) {
        ActualHearingPartyEntity partyEntity = new ActualHearingPartyEntity();

        setOrGeneratePartyId(actualHearingDayParty, partyEntity);
        partyEntity.setActualPartyRoleType(actualHearingDayParty.getPartyRole());
        partyEntity.setDidNotAttendFlag(actualHearingDayParty.getDidNotAttendFlag());
        partyEntity.setActualAttendeeIndividualDetail(createIndividualDetail(actualHearingDayParty, partyEntity));
        partyEntity.setActualHearingDay(dayEntity);
        return partyEntity;
    }

    private void setOrGeneratePartyId(ActualHearingDayParties actualHearingDayParty,
                                      ActualHearingPartyEntity partyEntity) {
        if (actualHearingDayParty.getActualPartyId() == null) {
            if (actualHearingDayParty.getIndividualDetails() != null) {
                partyEntity.setPartyId(String.valueOf(actualHearingDayParty.hashCode()));
                actualHearingDayParty.setActualPartyId(String.valueOf(actualHearingDayParty.hashCode()));
            } else {
                partyEntity.setPartyId(String.valueOf(actualHearingDayParty.getActualOrganisationName().hashCode()));
                actualHearingDayParty.setActualPartyId(String.valueOf(actualHearingDayParty
                                                                          .getActualOrganisationName().hashCode()));
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

                if (actualHearingDayParty.getActualPartyId() != null) {
                    ActualHearingPartyEntity sourceEntity =
                        getHearingPartyEntityByReference(
                            actualHearingDayParty.getActualPartyId(),
                            hearingPartyEntities
                        );

                    ActualPartyRelationshipDetailEntity partyRelationshipDetail = ActualPartyRelationshipDetailEntity
                        .builder()
                        .targetActualParty(matchingHearingPartyEntity)
                        .sourceActualParty(sourceEntity)
                        .build();

                    sourceEntity.setActualPartyRelationshipDetail(List.of(partyRelationshipDetail));
                }
            }
        }
        return hearingPartyEntities;
    }

    private ActualHearingPartyEntity getHearingPartyEntityByReference(
        String partyId, List<ActualHearingPartyEntity> hearingPartyEntities) {
        final List<ActualHearingPartyEntity> matchingHearingPartyEntities = hearingPartyEntities.stream()
            .filter(hearingPartyEntity -> partyId.equals(hearingPartyEntity.getPartyId()))
            .collect(Collectors.toList());

        if (matchingHearingPartyEntities.size() != 1) {
            throw new BadRequestException(
                String.format("Cannot find unique PartyID with value %s", partyId));
        }
        return matchingHearingPartyEntities.get(0);
    }

    private ActualAttendeeIndividualDetailEntity createIndividualDetail(
        ActualHearingDayParties actualHearingDayParty, ActualHearingPartyEntity partyEntity) {
        ActualAttendeeIndividualDetailEntity individualDetailEntity = new ActualAttendeeIndividualDetailEntity();

        ActualHearingDayPartyDetail individualDetails = actualHearingDayParty.getIndividualDetails();
        if (individualDetails != null) {
            individualDetailEntity.setFirstName(individualDetails.getFirstName());
            individualDetailEntity.setLastName(individualDetails.getLastName());
        }
        individualDetailEntity.setPartyOrganisationName(actualHearingDayParty.getActualOrganisationName());
        individualDetailEntity.setPartyActualSubChannelType(actualHearingDayParty.getPartyChannelSubType());
        individualDetailEntity.setActualHearingParty(partyEntity);

        return individualDetailEntity;
    }
}
