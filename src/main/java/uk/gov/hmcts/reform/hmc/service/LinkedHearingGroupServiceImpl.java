package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetailsAudit;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingDetailsAudit;
import uk.gov.hmcts.reform.hmc.domain.model.enums.DeleteHearingStatus;
import uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType;
import uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedHearingGroupNotFoundException;
import uk.gov.hmcts.reform.hmc.helper.HearingMapper;
import uk.gov.hmcts.reform.hmc.helper.LinkedGroupDetailsAuditMapper;
import uk.gov.hmcts.reform.hmc.helper.LinkedHearingDetailsAuditMapper;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.LinkHearingDetails;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsAuditRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingDetailsAuditRepository;
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
import static uk.gov.hmcts.reform.hmc.constants.Constants.PENDING;
import static uk.gov.hmcts.reform.hmc.constants.Constants.VERSION_NUMBER;
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

    private final LinkedHearingDetailsAuditRepository linkedHearingDetailsAuditRepository;

    private final LinkedGroupDetailsAuditRepository linkedGroupDetailsAuditRepository;

    private final LinkedGroupDetailsAuditMapper linkedGroupDetailsAuditMapper;

    private final LinkedHearingDetailsAuditMapper linkedHearingDetailsAuditMapper;

    private final HearingMapper hearingMapper;

    @Autowired
    public LinkedHearingGroupServiceImpl(LinkedGroupDetailsRepository linkedGroupDetailsRepository,
                                         HearingRepository hearingRepository,
                                         LinkedHearingDetailsAuditRepository linkedHearingDetailsAuditRepository,
                                         LinkedGroupDetailsAuditRepository linkedGroupDetailsAuditRepository,
                                         LinkedGroupDetailsAuditMapper linkedGroupDetailsAuditMapper,
                                         LinkedHearingDetailsAuditMapper linkedHearingDetailsAuditMapper,
                                         HearingMapper hearingMapper) {
        super(hearingRepository);
        this.linkedGroupDetailsRepository = linkedGroupDetailsRepository;
        this.linkedHearingDetailsAuditRepository = linkedHearingDetailsAuditRepository;
        this.linkedGroupDetailsAuditRepository = linkedGroupDetailsAuditRepository;
        this.linkedGroupDetailsAuditMapper = linkedGroupDetailsAuditMapper;
        this.linkedHearingDetailsAuditMapper = linkedHearingDetailsAuditMapper;
        this.hearingMapper = hearingMapper;
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
        Integer version = hearingEntity.getLatestRequestVersion();
        Optional<HearingResponseEntity> hearingResponse = hearingEntity
            .getHearingResponses().stream().filter(hearingResponseEntity ->
                                                       hearingResponseEntity.getResponseVersion().equals(version))
            .collect(Collectors.toList()).stream()
            .max(Comparator.comparing(hearingResponseEntity -> hearingResponseEntity.getRequestTimeStamp()));

        return getLowestDate(hearingResponse.orElseThrow(() -> new BadRequestException("bad request")));
    }

    private LocalDate getLowestDate(HearingResponseEntity hearingResponse) {
        Optional<HearingDayDetailsEntity> hearingDayDetails = hearingResponse.getHearingDayDetails()
            .stream().min(Comparator.comparing(hearingDayDetailsEntity -> hearingDayDetailsEntity.getStartDateTime()));

        return hearingDayDetails
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
                        INVALID_DELETE_HEARING_GROUP_HEARING_STATUS,
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
        LinkedGroupDetails linkedGroupDetails = linkedGroupHearings.get(0).getLinkedGroupDetails();
        setLinkedGroupDetails(linkedGroupDetails);
        saveLinkedGroupDetailsAudit(linkedGroupDetails);
        linkedGroupHearings.forEach(hearingEntity -> {
            saveLinkedHearingDetailsAudit(hearingEntity);
            setHearingDetails(hearingEntity);
        });
        linkedGroupDetailsRepository.deleteHearingGroup(hearingGroupId);
        // TODO: call ListAssist - https://tools.hmcts.net/jira/browse/HMAN-97
    }

    private void setLinkedGroupDetails(LinkedGroupDetails linkedGroupDetails) {
        Long versionNumber = linkedGroupDetails.getLinkedGroupLatestVersion();
        linkedGroupDetails.setLinkedGroupLatestVersion(versionNumber + VERSION_NUMBER);
        linkedGroupDetails.setStatus(PENDING);
    }

    private void saveLinkedGroupDetailsAudit(LinkedGroupDetails linkedGroupDetails) {
        LinkedGroupDetailsAudit linkedGroupDetailsAudit = linkedGroupDetailsAuditMapper
            .modelToEntity(linkedGroupDetails);
        linkedGroupDetailsAuditRepository.save(linkedGroupDetailsAudit);
    }

    private void saveLinkedHearingDetailsAudit(HearingEntity hearingEntity) {
        LinkedHearingDetailsAudit linkedHearingDetailsAuditEntity = linkedHearingDetailsAuditMapper
            .modelToEntity(hearingEntity);
        linkedHearingDetailsAuditRepository.save(linkedHearingDetailsAuditEntity);
    }

    private void setHearingDetails(HearingEntity entity) {
        HearingEntity hearingEntity = hearingMapper.setHearingForLinkedHearing(entity);
        hearingRepository.save(hearingEntity);
    }
}
