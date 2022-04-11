package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetailsAudit;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingDetailsAudit;
import uk.gov.hmcts.reform.hmc.domain.model.enums.DeleteHearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.AuthenticationException;
import uk.gov.hmcts.reform.hmc.exceptions.BadFutureHearingRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedHearingGroupNotFoundException;
import uk.gov.hmcts.reform.hmc.helper.LinkedGroupDetailsAuditMapper;
import uk.gov.hmcts.reform.hmc.helper.LinkedHearingDetailsAuditMapper;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.LinkHearingDetails;
import uk.gov.hmcts.reform.hmc.repository.DefaultFutureHearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsAuditRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingDetailsAuditRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingDetailsRepository;
import uk.gov.hmcts.reform.hmc.validator.LinkedHearingValidator;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static uk.gov.hmcts.reform.hmc.constants.Constants.ERROR;
import static uk.gov.hmcts.reform.hmc.constants.Constants.LIST_ASSIST;
import static uk.gov.hmcts.reform.hmc.constants.Constants.PENDING;
import static uk.gov.hmcts.reform.hmc.constants.Constants.VERSION_NUMBER_TO_INCREMENT;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_GROUP_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_DELETE_HEARING_GROUP_HEARING_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_DELETE_HEARING_GROUP_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_LINKED_GROUP_REQUEST_ID_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.LIST_ASSIST_FAILED_TO_RESPOND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.REJECTED_BY_LIST_ASSIST;

@Service
@Component
@Slf4j
public class LinkedHearingGroupServiceImpl extends LinkedHearingValidator implements LinkedHearingGroupService {

    private static final List<String> invalidDeleteGroupStatuses = Arrays.asList("PENDING", "ERROR");
    private HearingRepository hearingRepository;

    private final LinkedHearingDetailsAuditRepository linkedHearingDetailsAuditRepository;

    private final LinkedGroupDetailsAuditRepository linkedGroupDetailsAuditRepository;

    private final LinkedGroupDetailsAuditMapper linkedGroupDetailsAuditMapper;

    private final LinkedHearingDetailsAuditMapper linkedHearingDetailsAuditMapper;

    private final DefaultFutureHearingRepository futureHearingRepository;

    private final TransactionHandler transactionHandler;

    @Autowired
    public LinkedHearingGroupServiceImpl(HearingRepository hearingRepository,
                                         LinkedGroupDetailsRepository linkedGroupDetailsRepository,
                                         LinkedHearingDetailsRepository linkedHearingDetailsRepository,
                                         LinkedHearingDetailsAuditRepository linkedHearingDetailsAuditRepository,
                                         LinkedGroupDetailsAuditRepository linkedGroupDetailsAuditRepository,
                                         LinkedGroupDetailsAuditMapper linkedGroupDetailsAuditMapper,
                                         LinkedHearingDetailsAuditMapper linkedHearingDetailsAuditMapper,
                                         DefaultFutureHearingRepository futureHearingRepository,
                                         TransactionHandler transactionHandler) {
        super(hearingRepository, linkedGroupDetailsRepository, linkedHearingDetailsRepository);
        this.hearingRepository = hearingRepository;
        this.linkedHearingDetailsAuditRepository = linkedHearingDetailsAuditRepository;
        this.linkedGroupDetailsAuditRepository = linkedGroupDetailsAuditRepository;
        this.linkedGroupDetailsAuditMapper = linkedGroupDetailsAuditMapper;
        this.linkedHearingDetailsAuditMapper = linkedHearingDetailsAuditMapper;
        this.futureHearingRepository = futureHearingRepository;
        this.transactionHandler = transactionHandler;
    }


    @Override
    public void linkHearing(HearingLinkGroupRequest hearingLinkGroupRequest) {
        validateHearingLinkGroupRequest(hearingLinkGroupRequest, null);
    }

    @Override
    @Transactional(noRollbackFor = {BadRequestException.class})
    public void deleteLinkedHearingGroup(Long hearingGroupId) {

        validateHearingGroup(hearingGroupId);
        List<HearingEntity> linkedGroupHearings = hearingRepository.findByLinkedGroupId(hearingGroupId);
        validateUnlinkingHearingsStatus(linkedGroupHearings);
        validateUnlinkingHearingsWillNotHaveStartDateInThePast(linkedGroupHearings);

        deleteFromLinkedGroupDetails(linkedGroupHearings);
    }

    @Override
    public void updateLinkHearing(String requestId, HearingLinkGroupRequest hearingLinkGroupRequest) {
        validateHearingLinkGroupRequestForUpdate(requestId, hearingLinkGroupRequest);
    }

    private void validateHearingLinkGroupRequestForUpdate(String requestId,
                                                          HearingLinkGroupRequest hearingLinkGroupRequest) {
        validateRequestId(requestId, INVALID_LINKED_GROUP_REQUEST_ID_DETAILS);
        validateHearingLinkGroupRequest(hearingLinkGroupRequest, requestId);
        List<LinkHearingDetails> linkedHearingDetailsListPayload = hearingLinkGroupRequest.getHearingsInGroup();
        validateLinkedHearingsForUpdate(requestId, linkedHearingDetailsListPayload);
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

    private List<HearingResponseEntity> getLatestVersionHearingResponses(HearingEntity hearing) {
        Optional<Map.Entry<Integer, List<HearingResponseEntity>>> max = hearing.getHearingResponses().stream()
            .collect(groupingBy(HearingResponseEntity::getRequestVersion))
            .entrySet()
            .stream()
            .max(Map.Entry.comparingByKey());

        return max.isPresent() ? max.get().getValue() : List.of();
    }

    private void deleteFromLinkedGroupDetails(List<HearingEntity> linkedGroupHearings) {
        LinkedGroupDetails linkedGroupDetails = linkedGroupHearings.get(0).getLinkedGroupDetails();
        final String requestId = linkedGroupDetails.getRequestId();
        saveLinkedGroupDetailsAudit(linkedGroupDetails);
        linkedGroupHearings.forEach(hearingEntity -> saveLinkedHearingDetailsAudit(hearingEntity));
        saveLinkedGroupDetails(linkedGroupDetails, requestId);
        try {
            futureHearingRepository.deleteLinkedHearingGroup(requestId);
            log.info("Response received from ListAssist successfully");
            linkedGroupDetailsRepository.delete(linkedGroupDetails);
        } catch (Exception exception) {
            processResponseFromListAssistForDeleteLinkedHearing(linkedGroupDetails, exception);
        }

    }

    private void processResponseFromListAssistForDeleteLinkedHearing(LinkedGroupDetails linkedGroupDetails,
                                                                     Exception exception) {
        //Errors with 4xxx
        if (exception instanceof BadFutureHearingRequestException) {
            log.error("Exception occurred List Assist failed to respond with status code: {}",
                ((BadFutureHearingRequestException) exception).getErrorDetails().getErrorCode());
            linkedGroupDetailsRepository.delete(linkedGroupDetails);
            throw new BadRequestException(REJECTED_BY_LIST_ASSIST);
        } else {
            //Errors with 5xxx
            log.error("Time out exception occurred with status code:  {}",
                ((AuthenticationException) exception).getErrorDetails().getErrorCode());
            saveLinkedGroupDetails(linkedGroupDetails, LIST_ASSIST);
            throw new BadRequestException(LIST_ASSIST_FAILED_TO_RESPOND);
        }
    }

    private void saveLinkedGroupDetails(LinkedGroupDetails linkedGroupDetails, String request) {
        if (LIST_ASSIST.equals(request)) {
            linkedGroupDetails.setStatus(ERROR);
        } else {
            Long versionNumber = linkedGroupDetails.getLinkedGroupLatestVersion();
            linkedGroupDetails.setLinkedGroupLatestVersion(versionNumber + VERSION_NUMBER_TO_INCREMENT);
            linkedGroupDetails.setStatus(PENDING);
        }
        linkedGroupDetailsRepository.save(linkedGroupDetails);
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

}
