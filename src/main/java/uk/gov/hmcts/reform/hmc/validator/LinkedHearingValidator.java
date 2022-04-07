package uk.gov.hmcts.reform.hmc.validator;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingDetailsAudit;
import uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType;
import uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;

import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ORDER_NOT_UNIQUE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_REQUEST_ALREADY_LINKED;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_REQUEST_CANNOT_BE_LINKED;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INSUFFICIENT_REQUEST_IDS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_GROUP_LINK_TYPE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ORDER;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_STATE_FOR_HEARING_REQUEST;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_STATE_FOR_LINKED_GROUP;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_STATE_FOR_UNLINKING_HEARING_REQUEST;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.LINKED_GROUP_ID_EMPTY;

@Slf4j
public class LinkedHearingValidator extends HearingIdValidator {

    protected final LinkedGroupDetailsRepository linkedGroupDetailsRepository;
    protected final LinkedHearingDetailsRepository linkedHearingDetailsRepository;
    private static final String HEARING_ID_PLACEHOLDER = "<hearingId>";
    private final HearingRepository hearingRepository;

    public LinkedHearingValidator(HearingRepository hearingRepository,
                                  LinkedGroupDetailsRepository linkedGroupDetailsRepository,
                                  LinkedHearingDetailsRepository linkedHearingDetailsRepository) {
        super(hearingRepository);
        this.hearingRepository = hearingRepository;
        this.linkedGroupDetailsRepository = linkedGroupDetailsRepository;
        this.linkedHearingDetailsRepository = linkedHearingDetailsRepository;
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

    /**
     * validate linked hearings to be updated.
     * @param requestId requestId
     * @param linkHearingDetailsListPayload linkHearingDetails from payload
     */
    protected final void validateLinkedHearingsForUpdate(String requestId,
                                                         List<LinkHearingDetails> linkHearingDetailsListPayload) {
        // get existing data linkedHearingDetails
        List<LinkedHearingDetailsAudit> linkedHearingDetailsListExisting
                = linkedHearingDetailsRepository.getLinkedHearingDetailsByRequestId(requestId);

        // get obsolete linkedHearingDetails
        List<LinkedHearingDetailsAudit> linkedHearingDetailsListObsolete =
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
    protected final List<LinkedHearingDetailsAudit> extractObsoleteLinkedHearings(
            List<LinkHearingDetails> hearingDetailsListPayload,
            List<LinkedHearingDetailsAudit> hearingDetailsListExisting) {
        // build list of hearing ids
        List<String> payloadHearingIds = new ArrayList<>();
        hearingDetailsListPayload.forEach(e -> payloadHearingIds.add(e.getHearingId()));

        // deduce obsolete Linked Hearing Details
        List<LinkedHearingDetailsAudit> obsoleteLinkedHearingDetails = new ArrayList<>();
        hearingDetailsListExisting.forEach(e -> {
            if (!payloadHearingIds.contains(e.getHearing().getId().toString())) {
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
            List<LinkedHearingDetailsAudit> obsoleteLinkedHearings) {
        List<String> errorMessages = new ArrayList<>();
        obsoleteLinkedHearings.forEach(e -> {
            if (!PutHearingStatus.isValid((e.getHearing().getStatus()))) {
                errorMessages.add(INVALID_STATE_FOR_UNLINKING_HEARING_REQUEST
                        .replace(HEARING_ID_PLACEHOLDER, e.getHearing().getId().toString()));
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
        log.info("requestId:{}, hearingId: {}", requestId, details.getHearingId());
        LinkedHearingDetailsAudit linkedHearingDetails =
                linkedHearingDetailsRepository.getLinkedHearingDetailsByHearingId(
                        Long.parseLong(details.getHearingId()));
        if (null != linkedHearingDetails) {
            if (log.isDebugEnabled()) {
                log.debug("requestId:{}", requestId);
                log.debug("linkedHearingDetails:{}", linkedHearingDetails);
                log.debug("linkedHearingDetails.getLinkedGroup():{}", linkedHearingDetails.getLinkedGroup());
                if (null != requestId) {
                    log.debug("linkedGroupDetailsById:{}",
                            linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(requestId));
                }
            }
            if ((null == requestId && linkedHearingDetails.getLinkedGroup() != null)
                    || (null != requestId && !linkedHearingDetails.getLinkedGroup()
                    .equals(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(requestId)))) {
                throw new BadRequestException(HEARING_REQUEST_ALREADY_LINKED);
            }
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

    @Transactional
    protected LinkedGroupDetails updateHearingWithLinkGroup(HearingLinkGroupRequest hearingLinkGroupRequest) {
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
                    hearingToSave.setLinkedOrder(Long.valueOf(linkHearingDetails.getHearingOrder()));
                    hearingRepository.save(hearingToSave);
                }
            });
        return linkedGroupDetailsSaved;
    }
}
