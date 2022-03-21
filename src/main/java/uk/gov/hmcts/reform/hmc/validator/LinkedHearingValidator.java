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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.LINKED_GROUP_ID_EMPTY;

@Slf4j
public class LinkedHearingValidator extends HearingIdValidator {

    protected final LinkedGroupDetailsRepository linkedGroupDetailsRepository;
    protected final LinkedHearingDetailsRepository linkedHearingDetailsRepository;

    public LinkedHearingValidator(HearingRepository hearingRepository,
                                  LinkedGroupDetailsRepository linkedGroupDetailsRepository,
                                  LinkedHearingDetailsRepository linkedHearingDetailsRepository) {
        super(hearingRepository);
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
                errorMessages.add("008 Invalid state for unlinking hearing request <hearingId>"
                        .replace("<hearingId>", e.getHearing().getId().toString()));
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
            throw new BadRequestException("001 Insufficient requestIds");
        }
    }

    protected void checkHearingRequestAllowsLinking(Optional<HearingEntity> hearingEntity) {
        if (hearingEntity.isEmpty() || Boolean.FALSE.equals(hearingEntity.get().getIsLinkedFlag())) {
            throw new BadRequestException("002 hearing request isLinked is False");
        }
    }

    protected void checkLinkedGroupInActiveStatus(String requestId) {
        LinkedGroupDetails linkedGroupDetails =
                linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(requestId);
        if (null != linkedGroupDetails
                && !linkedGroupDetails.getStatus().equals("ACTIVE")) {
            throw new BadRequestException("007 group is in a " + linkedGroupDetails.getStatus() + " state");
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
                throw new BadRequestException("003 hearing request already in a group");
            }
        }
    }

    protected void checkValidStateForHearingRequest(Optional<HearingEntity> hearingEntity,
                                                  LinkHearingDetails details) {
        if (hearingEntity.isEmpty()
                || !PutHearingStatus.isValid(hearingEntity.get().getStatus())
                || filterHearingResponses(hearingEntity.get()).isBefore(LocalDate.now())) {
            throw new BadRequestException("004 Invalid state for hearing request "
                    + details.getHearingId());
        }
    }

    protected void checkHearingOrderIsUnique(HearingLinkGroupRequest hearingLinkGroupRequest,
                                           LinkHearingDetails details) {
        if (LinkType.ORDERED.label.equals(hearingLinkGroupRequest.getGroupDetails().getGroupLinkType())) {
            int counter = getOrderOccurrences(
                    hearingLinkGroupRequest.getHearingsInGroup(),
                    details.getHearingOrder()
            );
            if (counter > 1) {
                throw new BadRequestException("005 Hearing Order is not unique");
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

    protected LocalDate filterHearingResponses(HearingEntity hearingEntity) {
        log.debug("hearing id: {}", hearingEntity.getId());
        String version = hearingEntity.getCaseHearingRequest().getVersionNumber().toString();
        Optional<HearingResponseEntity> hearingResponse = hearingEntity
                .getHearingResponses().stream().filter(hearingResponseEntity ->
                        hearingResponseEntity.getResponseVersion().equals(version))
                .collect(Collectors.toList()).stream()
                .max(Comparator.comparing(HearingResponseEntity::getRequestTimeStamp));
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
                new BadRequestException("004 Invalid state for hearing request " + hearingEntity.getId()
                        + " no lowest date for given version")));
    }

    protected LocalDate getLowestDate(HearingResponseEntity hearingResponse) {
        Optional<HearingDayDetailsEntity> hearingDayDetails = hearingResponse.getHearingDayDetails()
                .stream().min(Comparator.comparing(HearingDayDetailsEntity::getStartDateTime));
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
                .orElseThrow(() -> new BadRequestException("004 Invalid state for hearing request "
                        + hearingResponse.getHearing().getId() + " "
                        + "valid hearingDayDetails not found"))
                .getStartDateTime().toLocalDate();
    }
}
