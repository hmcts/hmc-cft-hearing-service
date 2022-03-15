package uk.gov.hmcts.reform.hmc.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.domain.model.enums.DeleteHearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedHearingGroupNotFoundException;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;

import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_GROUP_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_DELETE_HEARING_GROUP_DAY_IN_THE_PAST;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_DELETE_HEARING_GROUP_HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_DELETE_HEARING_GROUP_STATUS;

@Service
@Transactional
public class LinkedHearingGroupService {
    private static final List<String> invalidDeleteGroupStatuses = Arrays.asList("PENDING", "ERROR");

    private final LinkedGroupDetailsRepository linkedGroupDetailsRepository;
    private final HearingRepository hearingRepository;

    public LinkedHearingGroupService(LinkedGroupDetailsRepository linkedGroupDetailsRepository,
                                     HearingRepository hearingRepository) {
        this.linkedGroupDetailsRepository = linkedGroupDetailsRepository;
        this.hearingRepository = hearingRepository;
    }

    public void deleteLinkedHearingGroup(Long hearingGroupId) {

        validateHearingGroup(hearingGroupId);
        List<HearingEntity> linkedGroupHearings = hearingRepository.findByLinkedGroupId(hearingGroupId);
        validateUnlinkingHearingsStatus(linkedGroupHearings);
        validateUnlinkingHearingsWillNotHaveStartDateInThePast(linkedGroupHearings);

        deleteFromLinkedGroupDetails(linkedGroupHearings, hearingGroupId);
    }

    private void validateHearingGroup(Long hearingGroupId) {
        Optional<LinkedGroupDetails> linkedGroupDetailsOptional = linkedGroupDetailsRepository.findById(hearingGroupId);
        validateHearingGroupPresent(hearingGroupId, linkedGroupDetailsOptional);
        validateHearingGroupStatus(linkedGroupDetailsOptional.get());
    }

    private void validateHearingGroupPresent(Long hearingGroupId, Optional<LinkedGroupDetails> linkedGroupDetails) {
        if (linkedGroupDetails.isEmpty()) {
            throw new LinkedHearingGroupNotFoundException(hearingGroupId, HEARING_GROUP_ID_NOT_FOUND);
        }
    }

    private void validateHearingGroupStatus(LinkedGroupDetails linkedGroupDetails) {
        String groupStatus = linkedGroupDetails.getStatus();
        if (invalidDeleteGroupStatuses.stream().anyMatch(e -> e.equals(groupStatus))) {
            throw new BadRequestException(format(INVALID_DELETE_HEARING_GROUP_STATUS, groupStatus));
        }
    }

    private void validateUnlinkingHearingsStatus(List<HearingEntity> linkedHearings) {
        List<HearingEntity> unlinkInvalidStatusHearings = linkedHearings.stream()
            .filter(h -> !DeleteHearingStatus.isValid(h.getStatus()))
            .collect(Collectors.toList());

        if (!unlinkInvalidStatusHearings.isEmpty()) {
            throw new BadRequestException(
                format(INVALID_DELETE_HEARING_GROUP_HEARING_STATUS, unlinkInvalidStatusHearings.get(0).getId()));
        }
    }

    private void validateUnlinkingHearingsWillNotHaveStartDateInThePast(List<HearingEntity> linkedHearings) {

        linkedHearings.stream()
            .filter(h -> h.getHearingResponses().size() > 0)
            .forEach(hearing -> {
                List<HearingResponseEntity> latestVersionHearingResponses = getLatestVersionHearingResponses(hearing);

                Optional<HearingResponseEntity> mostRecentLatestVersionHearingResponse = latestVersionHearingResponses
                    .stream().max(Comparator.comparing(HearingResponseEntity::getRequestTimeStamp));

                boolean hasHearingDateInThePast = mostRecentLatestVersionHearingResponse.isPresent()
                    && mostRecentLatestVersionHearingResponse.get()
                    .getHearingDayDetails().stream()
                    .anyMatch(dayTime -> dayTime.getStartDateTime().isBefore(LocalDateTime.now()));

                if (hasHearingDateInThePast) {
                    throw new BadRequestException(format(
                        INVALID_DELETE_HEARING_GROUP_DAY_IN_THE_PAST,
                        hearing.getId()
                    ));
                }
            });
    }

    private List<HearingResponseEntity> getLatestVersionHearingResponses(HearingEntity hearing) {
        Optional<Map.Entry<String, List<HearingResponseEntity>>> max = hearing.getHearingResponses().stream()
            .collect(groupingBy(HearingResponseEntity::getRequestVersion))
            .entrySet()
            .stream()
            .max(Map.Entry.comparingByKey());

        return max.isPresent() ? max.get().getValue() : List.of();
    }

    private void deleteFromLinkedGroupDetails(List<HearingEntity> linkedGroupHearings, Long hearingGroupId) {
        linkedGroupHearings.forEach(hearingEntity -> {
            // TODO: unlink hearingEntity from the group and persist - https://tools.hmcts.net/jira/browse/HMAN-96
        });
        linkedGroupDetailsRepository.deleteHearingGroup(hearingGroupId);
        // TODO: call ListAssist - https://tools.hmcts.net/jira/browse/HMAN-97
    }
}
