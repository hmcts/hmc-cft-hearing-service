package uk.gov.hmcts.reform.hmc.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.domain.model.enums.DeleteHearingStatus;
import uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType;
import uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedGroupNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedHearingGroupNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedHearingNotValidForUnlinkingException;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.LinkHearingDetails;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;

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

import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_GROUP_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_PLACEHOLDER;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ORDER_NOT_UNIQUE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_REQUEST_ALREADY_LINKED;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_REQUEST_CANNOT_BE_LINKED;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INSUFFICIENT_REQUEST_IDS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_DELETE_HEARING_GROUP_HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_DELETE_HEARING_GROUP_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_GROUP_LINK_TYPE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ORDER;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_LINKED_GROUP_REQUEST_ID_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_STATE_FOR_HEARING_REQUEST;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_STATE_FOR_LINKED_GROUP;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_STATE_FOR_UNLINKING_HEARING_REQUEST;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.LINKED_GROUP_ID_EMPTY;

@Slf4j
@Component
public class LinkedHearingValidator extends HearingIdValidator {

    private static final List<String> invalidDeleteGroupStatuses = Arrays.asList("PENDING", "ERROR");

    protected final LinkedGroupDetailsRepository linkedGroupDetailsRepository;

    @Autowired
    public LinkedHearingValidator(HearingRepository hearingRepository,
                                  LinkedGroupDetailsRepository linkedGroupDetailsRepository) {
        super(hearingRepository);
        this.linkedGroupDetailsRepository = linkedGroupDetailsRepository;
    }

    /**
     * validate Request id.
     * @param requestId request id
     */
    protected final void validateRequestId(String requestId, String errorMessage) {
        if (requestId == null) {
            throw new BadRequestException(LINKED_GROUP_ID_EMPTY);
        } else {
            Long answer = linkedGroupDetailsRepository.isFoundForRequestId(requestId);
            if (null == answer || answer.intValue() == 0) {
                throw new LinkedGroupNotFoundException(requestId, errorMessage);
            }
        }
    }

    public void validateHearingLinkGroupRequestForUpdate(String requestId,
                                                          HearingLinkGroupRequest hearingLinkGroupRequest) {
        validateRequestId(requestId, INVALID_LINKED_GROUP_REQUEST_ID_DETAILS);
        validateHearingLinkGroupRequest(hearingLinkGroupRequest, requestId);
        List<LinkHearingDetails> linkedHearingDetailsListPayload = hearingLinkGroupRequest.getHearingsInGroup();
        validateLinkedHearingsForUpdate(requestId, linkedHearingDetailsListPayload);
    }

    /**
     * validate linked hearings to be updated.
     * @param requestId requestId
     * @param linkHearingDetailsListPayload linkHearingDetails from payload
     */
    protected final void validateLinkedHearingsForUpdate(String requestId,
                                                         List<LinkHearingDetails> linkHearingDetailsListPayload) {
        // get existing data linkedHearingDetails
        List<HearingEntity> linkedHearingDetailsListExisting
                = hearingRepository.findByRequestId(requestId);

        // get obsolete linkedHearingDetails
        List<HearingEntity> linkedHearingDetailsListObsolete =
                extractObsoleteLinkedHearings(linkHearingDetailsListPayload, linkedHearingDetailsListExisting);

        // validate and get errors, if any, for obsolete linkedHearingDetails
        List<String> errors = validateObsoleteLinkedHearings(linkedHearingDetailsListObsolete);

        // if errors then throw BadRequest with error list
        if (!errors.isEmpty()) {
            throw new LinkedHearingNotValidForUnlinkingException(errors);
        }

    }

    /**
     * extract the obsolete LinkedHearingDetails.
     * @param hearingDetailsListPayload  payload LinkedHearingDetails
     * @param hearingDetailsListExisting existing in db LinkedHearingDetails
     * @return obsoleteLinkedHearingDetails the obsolete LinkedHearingDetails
     */
    protected final List<HearingEntity> extractObsoleteLinkedHearings(
            List<LinkHearingDetails> hearingDetailsListPayload,
            List<HearingEntity> hearingDetailsListExisting) {
        // build list of hearing ids
        List<String> payloadHearingIds = new ArrayList<>();
        hearingDetailsListPayload.forEach(e -> payloadHearingIds.add(e.getHearingId()));

        // deduce obsolete Linked Hearing Details
        List<HearingEntity> obsoleteLinkedHearingDetails = new ArrayList<>();
        hearingDetailsListExisting.forEach(e -> {
            if (!payloadHearingIds.contains(e.getId().toString())) {
                obsoleteLinkedHearingDetails.add(e);
            }
        });

        return obsoleteLinkedHearingDetails;

    }

    /**
     * validate obsolete Linked Hearings.
     * @param obsoleteLinkedHearings obsolete linked hearing details
     * @return errorMessages error messages
     */
    protected final List<String> validateObsoleteLinkedHearings(
        List<HearingEntity> obsoleteLinkedHearings) {
        List<String> errorMessages = new ArrayList<>();
        obsoleteLinkedHearings.forEach(e -> {
            if (!PutHearingStatus.isValid((e.getStatus()))) {
                errorMessages.add(INVALID_STATE_FOR_UNLINKING_HEARING_REQUEST
                        .replace(HEARING_ID_PLACEHOLDER, e.getId().toString()));
            }
        });
        return errorMessages;
    }

    protected void validateHearingLinkGroupRequest(HearingLinkGroupRequest hearingLinkGroupRequest, String requestId) {
        checkLinkedGroupInActiveStatus(requestId);

        hearingLinkGroupRequest.getHearingsInGroup().forEach(details -> {
            checkSufficientRequestIds(hearingLinkGroupRequest, details);
            log.debug("hearingId: {}", details.getHearingId());
            validateHearingId(Long.valueOf(details.getHearingId()), HEARING_ID_NOT_FOUND);
            Optional<HearingEntity> hearingEntity = getHearing(Long.valueOf(details.getHearingId()));

            if (hearingEntity.isPresent()) {
                checkHearingRequestAllowsLinking(hearingEntity);
                checkHearingRequestIsNotInAnotherGroup(details, requestId);
                checkValidStateForHearingRequest(hearingEntity, details);
                checkHearingOrderIsUnique(hearingLinkGroupRequest, details);
            }
        });
    }

    protected void checkSufficientRequestIds(HearingLinkGroupRequest hearingLinkGroupRequest,
                                           LinkHearingDetails details) {
        int occurrences = getIdOccurrences(hearingLinkGroupRequest.getHearingsInGroup(), details.getHearingId());
        if (occurrences > 1) {
            throw new BadRequestException(INSUFFICIENT_REQUEST_IDS);
        }
    }

    protected void checkHearingRequestAllowsLinking(Optional<HearingEntity> hearingEntity) {
        if (hearingEntity.isEmpty() || Boolean.FALSE.equals(hearingEntity.get().getIsLinkedFlag())) {
            throw new BadRequestException(HEARING_REQUEST_CANNOT_BE_LINKED);
        }
    }

    protected void checkLinkedGroupInActiveStatus(String requestId) {
        LinkedGroupDetails linkedGroupDetails =
                linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(requestId);
        if (null != linkedGroupDetails
                && !linkedGroupDetails.getStatus().equals("ACTIVE")) {
            throw new BadRequestException(INVALID_STATE_FOR_LINKED_GROUP.replace("<state>",
                    linkedGroupDetails.getStatus()));
        }
    }

    protected void checkHearingRequestIsNotInAnotherGroup(LinkHearingDetails details,
                                                        String requestId) {
        Optional<HearingEntity> hearing =
                hearingRepository.findById(Long.parseLong(details.getHearingId()));
        if (hearing.isPresent()
            && ((null == requestId && hearing.get().getLinkedGroupDetails() != null)
            || (null != requestId && !hearing.get().getLinkedGroupDetails().getRequestId()
                    .equals(requestId)))) {
            throw new BadRequestException(HEARING_REQUEST_ALREADY_LINKED);
        }
    }


    protected void checkValidStateForHearingRequest(Optional<HearingEntity> hearingEntity,
                                                    LinkHearingDetails details) {
        if (hearingEntity.isEmpty()
            || !PutHearingStatus.isValid(hearingEntity.get().getStatus())
            || (hearingEntity.get().hasHearingResponses()
            && filterHearingResponses(hearingEntity.get()).isBefore(LocalDate.now()))) {
            throw new BadRequestException(
                INVALID_STATE_FOR_HEARING_REQUEST.replace(HEARING_ID_PLACEHOLDER, details.getHearingId()));
        }
    }

    protected void checkHearingOrderIsUnique(HearingLinkGroupRequest hearingLinkGroupRequest,
                                           LinkHearingDetails details) {
        //hman-55 step 4.4 / hman-56 step 6.4
        log.info(hearingLinkGroupRequest.toString());

        LinkType value = LinkType.getByLabel(hearingLinkGroupRequest.getGroupDetails().getGroupLinkType());
        if (value == null) {
            throw new BadRequestException(
                    INVALID_GROUP_LINK_TYPE
                            .replace("<linkType>",
                                    hearingLinkGroupRequest.getGroupDetails().getGroupLinkType()));
        }
        if (LinkType.ORDERED.equals(value)) {
            if (details.getHearingOrder() == 0) {
                throw new BadRequestException(INVALID_HEARING_ORDER);
            }
            int counter = getOrderOccurrences(
                    hearingLinkGroupRequest.getHearingsInGroup(),
                    details.getHearingOrder()
            );
            if (counter > 1) {
                throw new BadRequestException(HEARING_ORDER_NOT_UNIQUE);
            }
        }
    }

    protected int getOrderOccurrences(List<LinkHearingDetails> hearingDetails, int value) {
        List<Integer> list = new ArrayList<>();
        hearingDetails.forEach(lo -> {
            if (lo.getHearingOrder() < 1) {
                throw new BadRequestException("Valid order not provided for hearing id " + lo.getHearingId());
            }
            list.add(lo.getHearingOrder());
        });
        return Collections.frequency(list, value);
    }

    protected int getIdOccurrences(List<LinkHearingDetails> hearingDetails, String value) {
        List<String> list = new ArrayList<>();
        hearingDetails.forEach(lo -> list.add(lo.getHearingId()));
        return Collections.frequency(list, value);
    }

    protected void validateHearingGroup(Long hearingGroupId) {
        Optional<LinkedGroupDetails> linkedGroupDetailsOptional = linkedGroupDetailsRepository.findById(hearingGroupId);
        validateHearingGroupPresent(hearingGroupId, linkedGroupDetailsOptional);
        validateHearingGroupStatus(linkedGroupDetailsOptional);
    }

    protected void validateHearingGroupPresent(Long hearingGroupId, Optional<LinkedGroupDetails> linkedGroupDetails) {
        if (linkedGroupDetails.isEmpty()) {
            throw new LinkedHearingGroupNotFoundException(hearingGroupId, HEARING_GROUP_ID_NOT_FOUND);
        }
    }

    protected void validateHearingGroupStatus(Optional<LinkedGroupDetails> linkedGroupDetails) {
        String groupStatus = null;
        if (linkedGroupDetails.isPresent()) {
            groupStatus = linkedGroupDetails.get().getStatus();
        }
        if (linkedGroupDetails.isEmpty()
            || invalidDeleteGroupStatuses.stream().anyMatch(e -> e.equals(linkedGroupDetails.get().getStatus()))) {
            throw new BadRequestException(format(INVALID_DELETE_HEARING_GROUP_STATUS, groupStatus));
        }
    }

    protected void validateUnlinkingHearingsStatus(List<HearingEntity> hearings) {
        List<HearingEntity> unlinkInvalidStatusHearings = hearings.stream()
                .filter(h -> !DeleteHearingStatus.isValid(h.getStatus()))
                .collect(Collectors.toList());

        if (!unlinkInvalidStatusHearings.isEmpty()) {
            throw new BadRequestException(
                    format(INVALID_DELETE_HEARING_GROUP_HEARING_STATUS, unlinkInvalidStatusHearings.get(0).getId()));
        }
    }

    protected void validateUnlinkingHearingsWillNotHaveStartDateInThePast(List<HearingEntity> hearings) {
        hearings.stream()
                .filter(h -> !h.getHearingResponses().isEmpty())
                .forEach(hearing -> {
                    List<HearingResponseEntity> latestVersionHearingResponses
                            = getLatestVersionHearingResponses(hearing);

                    Optional<HearingResponseEntity> mostRecentLatestVersionHearingResponse
                            = latestVersionHearingResponses
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

    protected List<HearingResponseEntity> getLatestVersionHearingResponses(HearingEntity hearing) {
        Optional<Map.Entry<Integer, List<HearingResponseEntity>>> max = hearing.getHearingResponses().stream()
                .collect(groupingBy(HearingResponseEntity::getRequestVersion))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByKey());

        return max.isPresent() ? max.get().getValue() : List.of();
    }

    protected void deleteFromLinkedGroupDetails(List<HearingEntity> linkedGroupHearings, Long hearingGroupId) {
        linkedGroupHearings.forEach(hearingEntity -> {
            // TODO: unlink hearingEntity from the group and persist - https://tools.hmcts.net/jira/browse/HMAN-96
        });
        linkedGroupDetailsRepository.deleteHearingGroup(hearingGroupId);
        // TODO: call ListAssist - https://tools.hmcts.net/jira/browse/HMAN-97
    }

    public LocalDate filterHearingResponses(HearingEntity hearingEntity) {
        log.debug("hearing id: {}", hearingEntity.getId());
        Optional<HearingResponseEntity> hearingResponse = hearingEntity.getHearingResponseForLatestRequest();
        if (log.isDebugEnabled()) {
            if (hearingResponse.isPresent()) {
                log.debug("hearing response: {} : {}",
                        hearingResponse.get().getHearingResponseId(),
                        hearingResponse.get().getRequestTimeStamp());
            } else {
                log.debug("No hearing response found");
            }
        }
        return getLowestDate(hearingResponse.orElseThrow(() ->
                new BadRequestException(
                        INVALID_STATE_FOR_HEARING_REQUEST
                                .replace(HEARING_ID_PLACEHOLDER, hearingEntity.getId()
                        + " no lowest date for given version"))));
    }

    public LocalDate getLowestDate(HearingResponseEntity hearingResponse) {
        Optional<HearingDayDetailsEntity> hearingDayDetails = hearingResponse.getEarliestHearingDayDetails();
        if (log.isDebugEnabled()) {
            if (hearingDayDetails.isPresent()) {
                log.debug("hearing day details: {} : {}",
                        hearingDayDetails.get().getHearingDayId(),
                        hearingDayDetails.get().getStartDateTime());
            } else {
                log.debug("No hearing day details found");
            }
        }

        return hearingDayDetails
                .orElseThrow(() -> new BadRequestException(
                        INVALID_STATE_FOR_HEARING_REQUEST
                                .replace(HEARING_ID_PLACEHOLDER, hearingResponse.getHearing().getId().toString())
                                + " valid hearingDayDetails not found"))
                .getStartDateTime().toLocalDate();
    }

}
