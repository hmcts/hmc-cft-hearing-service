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
import uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType;
import uk.gov.hmcts.reform.hmc.exceptions.AuthenticationException;
import uk.gov.hmcts.reform.hmc.exceptions.BadFutureHearingRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedHearingGroupNotFoundException;
import uk.gov.hmcts.reform.hmc.helper.LinkedGroupDetailsAuditMapper;
import uk.gov.hmcts.reform.hmc.helper.LinkedHearingDetailsAuditMapper;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.LinkHearingDetails;
import uk.gov.hmcts.reform.hmc.model.listassist.CaseListing;
import uk.gov.hmcts.reform.hmc.model.listassist.HearingGroup;
import uk.gov.hmcts.reform.hmc.model.listassist.LinkedHearingGroup;
import uk.gov.hmcts.reform.hmc.repository.DefaultFutureHearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsAuditRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingDetailsAuditRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingDetailsRepository;
import uk.gov.hmcts.reform.hmc.service.common.ObjectMapperService;
import uk.gov.hmcts.reform.hmc.validator.LinkedHearingValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_NOT_FOUND;
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

    private final ObjectMapperService objectMapperService;

    @Autowired
    public LinkedHearingGroupServiceImpl(HearingRepository hearingRepository,
                                         LinkedGroupDetailsRepository linkedGroupDetailsRepository,
                                         LinkedHearingDetailsRepository linkedHearingDetailsRepository,
                                         LinkedHearingDetailsAuditRepository linkedHearingDetailsAuditRepository,
                                         LinkedGroupDetailsAuditRepository linkedGroupDetailsAuditRepository,
                                         LinkedGroupDetailsAuditMapper linkedGroupDetailsAuditMapper,
                                         LinkedHearingDetailsAuditMapper linkedHearingDetailsAuditMapper,
                                         DefaultFutureHearingRepository futureHearingRepository,
                                         TransactionHandler transactionHandler,
                                         ObjectMapperService objectMapperService) {
        super(hearingRepository, linkedGroupDetailsRepository, linkedHearingDetailsRepository);
        this.hearingRepository = hearingRepository;
        this.linkedHearingDetailsAuditRepository = linkedHearingDetailsAuditRepository;
        this.linkedGroupDetailsAuditRepository = linkedGroupDetailsAuditRepository;
        this.linkedGroupDetailsAuditMapper = linkedGroupDetailsAuditMapper;
        this.linkedHearingDetailsAuditMapper = linkedHearingDetailsAuditMapper;
        this.futureHearingRepository = futureHearingRepository;
        this.transactionHandler = transactionHandler;
        this.objectMapperService = objectMapperService;
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
        //HMAN-94
        LinkedGroupDetails currentLinkGroup = linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(requestId);
        List<HearingEntity> currentHearings =
            hearingRepository.findByLinkedGroupId(currentLinkGroup.getLinkedGroupId());
        unlinkHearingsFromGroup(hearingLinkGroupRequest, currentHearings);
        LinkedGroupDetails linkedGroupDetails = updateHearingWithLinkGroup(hearingLinkGroupRequest, requestId);
        saveAndAuditLinkHearing(hearingLinkGroupRequest, linkedGroupDetails);

        //HMAN-95
        LinkedHearingGroup linkedHearingGroup = processRequestForListAssist(linkedGroupDetails);
        try {
            futureHearingRepository.updateLinkedHearingGroup(requestId, objectMapperService
                .convertObjectToJsonNode(linkedHearingGroup));
            log.info("Response received from ListAssist successfully");
            linkedGroupDetailsRepository.updateLinkedGroupDetailsStatus(requestId, "ACTIVE");
        } catch (Exception exception) {
            processResponseFromListAssistForCreateLinkedHearing(requestId, hearingLinkGroupRequest, exception);
        }

    }

    @Transactional
    private void saveAndAuditLinkHearing(HearingLinkGroupRequest hearingLinkGroupRequest,
                                         LinkedGroupDetails linkedGroupDetails) {
        saveLinkedGroupDetailsAudit(linkedGroupDetails);
        //        hearingLinkGroupRequest.getHearingsInGroup().forEach(hearingInGroup -> {
        //            Optional<HearingEntity> optionalHearingEntity =
        //                hearingRepository.findById(Long.valueOf(hearingInGroup.getHearingId()));
        //            if (optionalHearingEntity.isPresent()){
        //                HearingEntity hearingEntity = optionalHearingEntity.get();
        //                saveLinkedHearingDetailsAudit(hearingEntity);
        //            }
        //        });
    }

    @Transactional
    private void unlinkHearingsFromGroup(HearingLinkGroupRequest hearingLinkGroupRequest,
                                         List<HearingEntity> currentHearings) {
        for (HearingEntity hearingEntity : currentHearings) {
            if (hearingLinkGroupRequest.getHearingsInGroup()
                .stream().noneMatch(linkHearingDetails ->
                                        Long.valueOf(linkHearingDetails.getHearingId()).equals(hearingEntity.getId()))) {
                hearingEntity.setLinkedOrder(null);
                hearingEntity.setLinkedGroupDetails(null);
                hearingRepository.save(hearingEntity);
            }
        }
    }

    protected LinkedGroupDetails updateHearingWithLinkGroup(HearingLinkGroupRequest hearingLinkGroupRequest,
                                                            String requestId) {
        LinkedGroupDetails linkedGroupDetails = new LinkedGroupDetails();
        linkedGroupDetails.setLinkedGroupLatestVersion(1L);
        if (requestId != null) {
            linkedGroupDetails = linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(requestId);
            linkedGroupDetails.setLinkedGroupLatestVersion((linkedGroupDetails.getLinkedGroupLatestVersion() + 1));
        }
        linkedGroupDetails.setRequestName(hearingLinkGroupRequest.getGroupDetails().getGroupName());
        linkedGroupDetails.setReasonForLink(hearingLinkGroupRequest.getGroupDetails().getGroupReason());
        linkedGroupDetails.setLinkType(LinkType.getByLabel(hearingLinkGroupRequest
                                                               .getGroupDetails().getGroupLinkType()));
        linkedGroupDetails.setLinkedComments(hearingLinkGroupRequest.getGroupDetails().getGroupComments());
        linkedGroupDetails.setStatus("PENDING");
        linkedGroupDetails.setRequestDateTime(LocalDateTime.now());

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

    private LinkedHearingGroup processRequestForListAssist(LinkedGroupDetails linkedGroupDetails) {
        HearingGroup hearingGroup = new HearingGroup();
        hearingGroup.setGroupClientReference(linkedGroupDetails.getRequestId());
        hearingGroup.setGroupName(linkedGroupDetails.getRequestName());
        hearingGroup.setGroupReason(linkedGroupDetails.getReasonForLink());
        hearingGroup.setGroupLinkType(linkedGroupDetails.getLinkType());
        hearingGroup.setGroupComment(linkedGroupDetails.getLinkedComments());
        hearingGroup.setGroupStatus("LHSAWL");
        ArrayList<CaseListing> caseListingArrayList = new ArrayList<>();
        List<HearingEntity> hearingEntities = hearingRepository
            .findByLinkedGroupId(linkedGroupDetails.getLinkedGroupId());
        for (HearingEntity hearingEntity : hearingEntities) {
            CaseListing caseListing = new CaseListing();
            caseListing.setCaseListingRequestId(hearingEntity.getId().toString());
            caseListing.setCaseLinkOrder(Integer.valueOf(hearingEntity.getLinkedOrder().toString()));
            caseListingArrayList.add(caseListing);
        }
        hearingGroup.setGroupHearings(caseListingArrayList);
        LinkedHearingGroup linkedHearingGroup = new LinkedHearingGroup();
        linkedHearingGroup.setLinkedHearingGroup(hearingGroup);
        return linkedHearingGroup;
    }

    private void processResponseFromListAssistForCreateLinkedHearing(String requestId,
                                                                     HearingLinkGroupRequest hearingLinkGroupRequest,
                                                                     Exception exception) {
        if (exception instanceof BadFutureHearingRequestException) {
            log.error(
                "Exception occurred List Assist failed to respond with status code: {}",
                ((BadFutureHearingRequestException) exception).getErrorDetails().getErrorCode()
            );
            hearingLinkGroupRequest.getHearingsInGroup()
                .forEach(linkHearingDetails -> {
                    Optional<HearingEntity> hearing = hearingRepository
                        .findById(Long.valueOf(linkHearingDetails.getHearingId()));
                    if (hearing.isPresent()) {
                        HearingEntity hearingToSave = hearing.get();
                        hearingToSave.setLinkedOrder(null);
                        hearingToSave.setLinkedGroupDetails(null);
                        hearingRepository.save(hearingToSave);
                    } else {
                        throw new HearingNotFoundException(
                            Long.valueOf(linkHearingDetails.getHearingId()),
                            HEARING_ID_NOT_FOUND
                        );
                    }
                });
            linkedGroupDetailsRepository.deleteLinkedGroupDetailsStatus(requestId);
            throw new BadRequestException(REJECTED_BY_LIST_ASSIST);
        } else {
            log.error(
                "Time out exception occurred with status code:  {}",
                ((AuthenticationException) exception).getErrorDetails().getErrorCode()
            );
            linkedGroupDetailsRepository.updateLinkedGroupDetailsStatus(requestId, "ERROR");
            throw new BadRequestException(LIST_ASSIST_FAILED_TO_RESPOND);
        }

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
            validateListAssistException(linkedGroupDetails, exception);
        }

    }

    private void validateListAssistException(LinkedGroupDetails linkedGroupDetails, Exception exception) {
        //Errors with 4xxx
        if (exception instanceof BadFutureHearingRequestException) {
            log.error(
                "Exception occurred List Assist failed to respond with status code: {}",
                ((BadFutureHearingRequestException) exception).getErrorDetails().getErrorCode()
            );
            linkedGroupDetailsRepository.delete(linkedGroupDetails);
            throw new BadRequestException(REJECTED_BY_LIST_ASSIST);
        } else {
            //Errors with 5xxx
            log.error(
                "Time out exception occurred with status code:  {}",
                ((AuthenticationException) exception).getErrorDetails().getErrorCode()
            );
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
