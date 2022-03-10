package uk.gov.hmcts.reform.hmc.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingDetailsEntity;
import uk.gov.hmcts.reform.hmc.domain.model.enums.DeleteHearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedHearingGroupNotFoundException;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingDetailsRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_GROUP_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_DELETE_HEARING_GROUP_DAY_IN_THE_PAST;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_DELETE_HEARING_GROUP_HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_DELETE_HEARING_GROUP_STATUS;

@Service
public class LinkedHearingGroupService {
    private static final List<String> invalidDeleteGroupStatuses = Arrays.asList("PENDING", "ERROR");

    private final LinkedHearingDetailsRepository linkedHearingDetailsRepository;

    public LinkedHearingGroupService(LinkedHearingDetailsRepository linkedHearingDetailsRepository) {
        this.linkedHearingDetailsRepository = linkedHearingDetailsRepository;
    }

    public void deleteLinkedHearingGroup(Long hearingGroupId) {

        List<LinkedHearingDetailsEntity> linkedHearingDetails = validateHearingGroupPresent(hearingGroupId);
        validateHearingGroupStatus(linkedHearingDetails);
        validateUnlinkingHearingsStatus(linkedHearingDetails);
        validateUnlinkingHearingsWillNotHaveStartDateInThePast(linkedHearingDetails);

        deleteFromLinkedGroupDetails(hearingGroupId);
    }

    private void validateUnlinkingHearingsWillNotHaveStartDateInThePast(
        List<LinkedHearingDetailsEntity> linkedHearingDetails) {

        linkedHearingDetails.stream()
            .map(LinkedHearingDetailsEntity::getHearing)
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

    private void validateHearingGroupStatus(List<LinkedHearingDetailsEntity> linkedHearingDetails) {
        String groupStatus = linkedHearingDetails.get(0).getLinkedGroup().getStatus();
        if (invalidDeleteGroupStatuses.stream().anyMatch(e -> e.equals(groupStatus))) {
            throw new BadRequestException(format(INVALID_DELETE_HEARING_GROUP_STATUS, groupStatus));
        }
    }

    private List<LinkedHearingDetailsEntity> validateHearingGroupPresent(Long hearingGroupId) {
        List<LinkedHearingDetailsEntity> linkedHearingDetails =
            linkedHearingDetailsRepository.getLinkedHearingDetails(hearingGroupId);
        if (linkedHearingDetails.isEmpty()) {
            throw new LinkedHearingGroupNotFoundException(hearingGroupId, HEARING_GROUP_ID_NOT_FOUND);
        }
        return linkedHearingDetails;
    }

    private void validateUnlinkingHearingsStatus(List<LinkedHearingDetailsEntity> linkedHearingDetails) {
        List<HearingEntity> unlinkInvalidStatusHearings = linkedHearingDetails.stream()
            .map(LinkedHearingDetailsEntity::getHearing)
            .filter(h -> !DeleteHearingStatus.isValid(h.getStatus()))
            .collect(Collectors.toList());

        if (!unlinkInvalidStatusHearings.isEmpty()) {
            throw new BadRequestException(
                format(INVALID_DELETE_HEARING_GROUP_HEARING_STATUS, unlinkInvalidStatusHearings.get(0).getId()));
        }
    }

    private void deleteFromLinkedGroupDetails(Long hearingGroupId) {
        linkedHearingDetailsRepository.deleteHearingGroup(hearingGroupId);
    }
}
