package uk.gov.hmcts.reform.hmc.validator;

import com.microsoft.applicationinsights.core.dependencies.google.common.base.Enums;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedGroupNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedHearingNotValidForUnlinkingException;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.LinkHearingDetails;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingDetailsRepository;

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
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARINGS_IN_GROUP_SIZE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ORDER_NOT_UNIQUE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_REQUEST_ALREADY_LINKED;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_REQUEST_CANNOT_BE_LINKED;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_DELETE_HEARING_GROUP_HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_DELETE_HEARING_GROUP_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_GROUP_LINK_TYPE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ORDER;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_LINKED_GROUP_REQUEST_ID_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_STATE_FOR_HEARING_REQUEST;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_STATE_FOR_LINKED_GROUP;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_STATE_FOR_UNLINKING_HEARING_REQUEST;

@Slf4j
@Component
public class LinkedHearingValidator {

    private static final List<String> invalidDeleteGroupStatuses = Arrays.asList("PENDING", "ERROR");

    protected final HearingIdValidator hearingIdValidator;
    protected final HearingRepository hearingRepository;
    protected final LinkedGroupDetailsRepository linkedGroupDetailsRepository;
    protected final LinkedHearingDetailsRepository linkedHearingDetailsRepository;
    private static final String HEARING_ID_PLACEHOLDER = "<hearingId>";

    @Autowired
    public LinkedHearingValidator(HearingIdValidator hearingIdValidator,
                                  HearingRepository hearingRepository,
                                  LinkedGroupDetailsRepository linkedGroupDetailsRepository,
                                  LinkedHearingDetailsRepository linkedHearingDetailsRepository) {
        this.hearingRepository = hearingRepository;
        this.linkedGroupDetailsRepository = linkedGroupDetailsRepository;
        this.hearingIdValidator = hearingIdValidator;
        this.linkedHearingDetailsRepository = linkedHearingDetailsRepository;
    }

    /**
     * validate Request id.
     * @param requestId request id
     */
    public void validateRequestId(String requestId, String errorMessage) {
        if (!StringUtils.isNumeric(requestId)) {
            log.debug("Null or non numeric value: {}", requestId);
            throw new BadRequestException(errorMessage);
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
    public final void validateLinkedHearingsForUpdate(String requestId,
                                                         List<LinkHearingDetails> linkHearingDetailsListPayload) {
        // get existing data linkedHearingDetails
        List<HearingEntity> linkedHearingDetailsListExisting
                = hearingRepository.findByRequestId(requestId);

        // get obsolete linkedHearingDetails
        List<HearingEntity> linkedHearingDetailsListObsolete =
                extractObsoleteLinkedHearings(linkHearingDetailsListPayload, linkedHearingDetailsListExisting);

        // validate and get errors, if invalid status for obsolete linkedHearingDetails
        List<String> errors = validateObsoleteLinkedHearings(linkedHearingDetailsListObsolete);

        // if errors then throw BadRequest with error list
        if (!errors.isEmpty()) {
            throw new LinkedHearingNotValidForUnlinkingException(errors);
        }

        // validate obsolete linkedHearingDetails if hearing is in the past
        validateUnlinkingHearingsWillNotHaveStartDateInThePast(linkedHearingDetailsListObsolete);

    }

    /**
     * extract the obsolete LinkedHearingDetails.
     * @param hearingDetailsListPayload  payload LinkedHearingDetails
     * @param hearingDetailsListExisting existing in db LinkedHearingDetails
     * @return obsoleteLinkedHearingDetails the obsolete LinkedHearingDetails
     */
    public final List<HearingEntity> extractObsoleteLinkedHearings(
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
    public final List<String> validateObsoleteLinkedHearings(
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

    public void validateHearingLinkGroupRequest(HearingLinkGroupRequest hearingLinkGroupRequest, String requestId) {
        checkLinkedGroupInActiveStatus(requestId);

        hearingLinkGroupRequest.getHearingsInGroup().forEach(details -> {
            checkSufficientRequestIds(hearingLinkGroupRequest, details);
            log.debug("hearingId: {}", details.getHearingId());
            hearingIdValidator.validateHearingId(Long.valueOf(details.getHearingId()), HEARING_ID_NOT_FOUND);
            Optional<HearingEntity> hearingEntity = hearingRepository.findById(Long.valueOf(details.getHearingId()));

            if (hearingEntity.isPresent()) {
                checkHearingRequestAllowsLinking(hearingEntity);
                checkHearingRequestIsNotInAnotherGroup(details, requestId);
                checkValidStateForHearingRequest(hearingEntity, details);
                checkHearingOrderIsUnique(hearingLinkGroupRequest, details);
            }
        });
    }

    public void checkSufficientRequestIds(HearingLinkGroupRequest hearingLinkGroupRequest,
                                           LinkHearingDetails details) {
        int occurrences = getIdOccurrences(hearingLinkGroupRequest.getHearingsInGroup(), details.getHearingId());
        if (occurrences > 1) {
            throw new BadRequestException(HEARINGS_IN_GROUP_SIZE);
        }
    }

    public void checkHearingRequestAllowsLinking(Optional<HearingEntity> hearingEntity) {
        if (hearingEntity.isEmpty() || Boolean.FALSE.equals(hearingEntity.get().getIsLinkedFlag())) {
            throw new BadRequestException(HEARING_REQUEST_CANNOT_BE_LINKED);
        }
    }

    public void checkLinkedGroupInActiveStatus(String requestId) {
        LinkedGroupDetails linkedGroupDetails =
                linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(requestId);
        if (null != linkedGroupDetails
                && !linkedGroupDetails.getStatus().equals("ACTIVE")) {
            throw new BadRequestException(INVALID_STATE_FOR_LINKED_GROUP.replace("<state>",
                    linkedGroupDetails.getStatus()));
        }
    }

    public void checkHearingRequestIsNotInAnotherGroup(LinkHearingDetails details,
                                                        String requestId) {
        Optional<HearingEntity> hearing =
                hearingRepository.findById(Long.parseLong(details.getHearingId()));
        // if hearing is present AND
        // (POST and hearing already linked
        // OR PUT and hearing is is not linked to current group/request)
        // then throw error
        if (hearing.isPresent()
            && ((null == requestId && null != hearing.get().getLinkedGroupDetails())
            || (null != requestId && null != hearing.get().getLinkedGroupDetails()
                && !hearing.get().getLinkedGroupDetails().getRequestId()
                    .equals(requestId)))) {
            throw new BadRequestException(HEARING_REQUEST_ALREADY_LINKED);
        }
    }


    public void checkValidStateForHearingRequest(Optional<HearingEntity> hearingEntity,
                                                 LinkHearingDetails details) {
        if (hearingEntity.isEmpty()
            || !PutHearingStatus.isValid(hearingEntity.get().getStatus())
            || (hearingEntity.get().hasHearingResponses()
            && filterHearingResponses(hearingEntity.get()).isBefore(LocalDate.now()))) {
            throw new BadRequestException(
                INVALID_STATE_FOR_HEARING_REQUEST.replace(HEARING_ID_PLACEHOLDER, details.getHearingId()));
        }
    }

    public void checkHearingOrderIsUnique(HearingLinkGroupRequest hearingLinkGroupRequest,
                                           LinkHearingDetails details) {
        //hman-55 step 4.4 / hman-56 step 6.4
        log.debug(hearingLinkGroupRequest.toString());

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

    public int getOrderOccurrences(List<LinkHearingDetails> hearingDetails, int value) {
        List<Integer> list = new ArrayList<>();
        hearingDetails.forEach(lo -> {
            if (lo.getHearingOrder() < 1) {
                throw new BadRequestException("Valid order not provided for hearing id " + lo.getHearingId());
            }
            list.add(lo.getHearingOrder());
        });
        return Collections.frequency(list, value);
    }

    public int getIdOccurrences(List<LinkHearingDetails> hearingDetails, String value) {
        List<String> list = new ArrayList<>();
        hearingDetails.forEach(lo -> list.add(lo.getHearingId()));
        return Collections.frequency(list, value);
    }

    public Long validateHearingGroup(String requestId) {
        Long linkedGroupId = validateRequestIdForDelete(requestId, INVALID_LINKED_GROUP_REQUEST_ID_DETAILS);
        LinkedGroupDetails linkedGroupDetailsOptional = linkedGroupDetailsRepository
            .getLinkedGroupDetailsByRequestId(requestId);
        validateHearingGroupStatus(linkedGroupDetailsOptional);
        return linkedGroupId;
    }

    public final Long validateRequestIdForDelete(String requestId, String errorMessage) {
        if (!StringUtils.isNumeric(requestId)) {
            log.debug("Null or non numeric value: {}", requestId);
            throw new BadRequestException(errorMessage);
        } else {
            Long answer = linkedGroupDetailsRepository.isFoundForRequestId(requestId);
            if (null == answer || answer.intValue() == 0) {
                throw new LinkedGroupNotFoundException(requestId,    errorMessage);
            } else {
                return answer;
            }
        }
    }

    public void validateHearingGroupStatus(LinkedGroupDetails linkedGroupDetails) {
        String groupStatus = null;
        if (linkedGroupDetails != null) {
            groupStatus = linkedGroupDetails.getStatus();
        }
        if (linkedGroupDetails == null
            || invalidDeleteGroupStatuses.stream().anyMatch(e -> e.equals(linkedGroupDetails.getStatus()))) {
            throw new BadRequestException(format(INVALID_DELETE_HEARING_GROUP_STATUS, groupStatus));
        }
    }

    public void validateUnlinkingHearingsStatus(List<HearingEntity> hearings) {
        List<HearingEntity> unlinkInvalidStatusHearings = hearings.stream()
                .filter(h -> !DeleteHearingStatus.isValid(h.getStatus()))
                .collect(Collectors.toList());

        if (!unlinkInvalidStatusHearings.isEmpty()) {
            throw new BadRequestException(
                    format(INVALID_DELETE_HEARING_GROUP_HEARING_STATUS, unlinkInvalidStatusHearings.get(0).getId()));
        }
    }

    public void validateUnlinkingHearingsWillNotHaveStartDateInThePast(List<HearingEntity> hearings) {
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

    public List<HearingResponseEntity> getLatestVersionHearingResponses(HearingEntity hearing) {
        Optional<Map.Entry<Integer, List<HearingResponseEntity>>> max = hearing.getHearingResponses().stream()
                .collect(groupingBy(HearingResponseEntity::getRequestVersion))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByKey());

        return max.isPresent() ? max.get().getValue() : List.of();
    }

    public LocalDate filterHearingResponses(HearingEntity hearingEntity) {
        log.debug("hearing id: {}", hearingEntity.getId());
        Optional<HearingResponseEntity> hearingResponse = hearingEntity.getHearingResponseForLatestRequestForUpdate();
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

    public void validateHearingActualsStatus(Long hearingId, String errorMessage) {
        String status = hearingRepository.getStatus(hearingId);
        DeleteHearingStatus deleteHearingStatus = Enums.getIfPresent(DeleteHearingStatus.class, status).orNull();
        if (deleteHearingStatus != null) {
            boolean isValidStatus = DeleteHearingStatus.isValidHearingActuals(deleteHearingStatus);
            LocalDate minStartDate = filterHearingResponses(hearingRepository.findById(hearingId)
                            .orElseThrow(() -> new HearingNotFoundException(
                                    hearingId,
                                    HEARING_ID_NOT_FOUND
                            )));
            LocalDate now = LocalDate.now();
            boolean isMinStartDatePast = minStartDate.isBefore(now) || minStartDate.equals(now);
            if (!(isValidStatus && isMinStartDatePast)) {
                throw new BadRequestException(errorMessage);
            }
        }
    }

    @Transactional
    public LinkedGroupDetails updateHearingWithLinkGroup(HearingLinkGroupRequest hearingLinkGroupRequest) {
        LinkedGroupDetails linkedGroupDetails = new LinkedGroupDetails();
        linkedGroupDetails.setRequestName(hearingLinkGroupRequest.getGroupDetails().getGroupName());
        linkedGroupDetails.setReasonForLink(hearingLinkGroupRequest.getGroupDetails().getGroupReason());
        linkedGroupDetails.setLinkType(LinkType.getByLabel(hearingLinkGroupRequest
                                                               .getGroupDetails().getGroupLinkType()));
        linkedGroupDetails.setLinkedComments(hearingLinkGroupRequest.getGroupDetails().getGroupComments());
        linkedGroupDetails.setStatus("PENDING");
        linkedGroupDetails.setRequestDateTime(LocalDateTime.now());
        linkedGroupDetails.setLinkedGroupLatestVersion(1L);
        LinkedGroupDetails linkedGroupDetailsSaved = linkedGroupDetailsRepository.save(linkedGroupDetails);

        hearingLinkGroupRequest.getHearingsInGroup()
            .forEach(linkHearingDetails -> {
                Optional<HearingEntity> hearing = hearingRepository
                    .findById(Long.valueOf(linkHearingDetails.getHearingId()));
                if (hearing.isPresent()) {
                    HearingEntity hearingToSave = hearing.get();
                    hearingToSave.setLinkedGroupDetails(linkedGroupDetailsSaved);
                    hearingToSave.setLinkedOrder(getHearingOrder(linkHearingDetails,hearingLinkGroupRequest));
                    hearingRepository.save(hearingToSave);
                }
            });
        return linkedGroupDetailsSaved;
    }

    public Long getHearingOrder(LinkHearingDetails linkHearingDetails,
                                 HearingLinkGroupRequest hearingLinkGroupRequest) {

        val order = Long.valueOf(linkHearingDetails.getHearingOrder());
        if (order == 0
            && hearingLinkGroupRequest.getGroupDetails().getGroupLinkType().equals(LinkType.SAME_SLOT.getLabel())) {
            return null;
        }
        return order;
    }
}
