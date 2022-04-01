package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.domain.model.enums.DeleteHearingStatus;
import uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType;
import uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedHearingGroupNotFoundException;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.LinkHearingDetails;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;

import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_GROUP_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_DELETE_HEARING_GROUP_HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_DELETE_HEARING_GROUP_STATUS;

@Service
@Component
@Slf4j
@Transactional
public class LinkedHearingGroupServiceImpl extends HearingIdValidator implements LinkedHearingGroupService {

    private static final List<String> invalidDeleteGroupStatuses = Arrays.asList("PENDING", "ERROR");

    private final LinkedGroupDetailsRepository linkedGroupDetailsRepository;

    @Autowired
    public LinkedHearingGroupServiceImpl(LinkedGroupDetailsRepository linkedGroupDetailsRepository,
                                         HearingRepository hearingRepository) {
        super(hearingRepository);
        this.linkedGroupDetailsRepository = linkedGroupDetailsRepository;
    }

    @Override
    public void linkHearing(HearingLinkGroupRequest hearingLinkGroupRequest) {
        validateHearingLinkGroupRequest(hearingLinkGroupRequest);
    }

    @Override
    public void deleteLinkedHearingGroup(Long hearingGroupId) {

        validateHearingGroup(hearingGroupId);
        List<HearingEntity> linkedGroupHearings = hearingRepository.findByLinkedGroupId(hearingGroupId);
        validateUnlinkingHearingsStatus(linkedGroupHearings);
        validateUnlinkingHearingsWillNotHaveStartDateInThePast(linkedGroupHearings);

        deleteFromLinkedGroupDetails(linkedGroupHearings, hearingGroupId);
    }

    private void validateHearingLinkGroupRequest(HearingLinkGroupRequest hearingLinkGroupRequest) {
        //hman -55 step 4 / hman-56 step 6
        hearingLinkGroupRequest.getHearingsInGroup().forEach(details -> {
            //hman-55 step 3 / hman-56 step 5
            int occurrences = getIdOccurrences(hearingLinkGroupRequest.getHearingsInGroup(), details.getHearingId());
            if (occurrences > 1) {
                throw new BadRequestException("001 Insufficient requestIds");
            }

            validateHearingId(Long.valueOf(details.getHearingId()), HEARING_ID_NOT_FOUND);
            Optional<HearingEntity> hearingEntity = hearingRepository
                .findById(Long.valueOf(details.getHearingId()));

            if (hearingEntity.isPresent()) {
                //hman-55 step 4.1 / hman-56 step 6.1
                if (!hearingEntity.get().getIsLinkedFlag().booleanValue()) {
                    throw new BadRequestException("002 hearing request isLinked is False");
                }

                //hearing id  in linkedHearingDetails check if it's in a group
                //hman-55 step 4.2 / hman-56 step 6.2
                if (hearingEntity.get().getLinkedGroupDetails() != null) {
                    throw new BadRequestException("003 hearing request already in a group");
                }

                //hman-55 step 4.3 / hamn-56 step 6.3
                if (!PutHearingStatus.isValid(hearingEntity.get().getStatus())
                    || filterHearingResponses(hearingEntity.get()).isBefore(LocalDate.now())) {
                    throw new BadRequestException("004 Invalid state for hearing request "
                                                      + details.getHearingId());
                }

                //hman-55 step 4.4 / hman-56 step 6.4
                LinkType value = LinkType.getByLabel(hearingLinkGroupRequest.getGroupDetails().getGroupLinkType());
                if (value == null) {
                    throw new BadRequestException("Invalid value for GroupLinkType");
                }
                if (LinkType.ORDERED.equals(value)) {
                    if (details.getHearingOrder() == 0) {
                        throw new BadRequestException("Hearing order must exist and be greater than 0");
                    }
                    int counter = getOrderOccurrences(
                        hearingLinkGroupRequest.getHearingsInGroup(),
                        details.getHearingOrder()
                    );
                    if (counter > 1) {
                        throw new BadRequestException("005 Hearing Order is not unique");
                    }
                }
            }
        });
    }

    private int getOrderOccurrences(List<LinkHearingDetails> hearingDetails, int value) {
        List<Integer> list = new ArrayList<>();
        hearingDetails.forEach(lo -> list.add(lo.getHearingOrder()));
        int occurrences = Collections.frequency(list, value);
        return occurrences;
    }

    private int getIdOccurrences(List<LinkHearingDetails> hearingDetails, String value) {
        List<String> list = new ArrayList<>();
        hearingDetails.forEach(lo -> list.add(lo.getHearingId()));
        int occurrences = Collections.frequency(list, value);
        return occurrences;
    }

    private LocalDate filterHearingResponses(HearingEntity hearingEntity) {
        Optional<HearingResponseEntity> hearingResponse = hearingEntity.getHearingResponseForLatestRequest();
        return getLowestDate(hearingResponse.orElseThrow(() -> new BadRequestException("bad request")));
    }

    private LocalDate getLowestDate(HearingResponseEntity hearingResponse) {
        return hearingResponse.getEarliestHearingDayDetails()
            .orElseThrow(() -> new BadRequestException("bad request")).getStartDateTime().toLocalDate();
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
            .filter(h -> !h.getHearingResponses().isEmpty())
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
                        INVALID_DELETE_HEARING_GROUP_HEARING_STATUS,
                        hearing.getId()
                    ));
                }
            });
    }

    private List<HearingResponseEntity> getLatestVersionHearingResponses(HearingEntity hearing) {
        Optional<Map.Entry<Integer, List<HearingResponseEntity>>> max = hearing.getHearingResponses().stream()
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
