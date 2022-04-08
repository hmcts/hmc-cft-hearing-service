package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.domain.model.enums.DeleteHearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.AuthenticationException;
import uk.gov.hmcts.reform.hmc.exceptions.BadFutureHearingRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedHearingGroupNotFoundException;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupResponse;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.LinkHearingDetails;
import uk.gov.hmcts.reform.hmc.model.listassist.CaseListing;
import uk.gov.hmcts.reform.hmc.model.listassist.HearingGroup;
import uk.gov.hmcts.reform.hmc.model.listassist.LinkedHearingGroup;
import uk.gov.hmcts.reform.hmc.repository.DefaultFutureHearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;
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
    private final DefaultFutureHearingRepository futureHearingRepository;
    private final ObjectMapperService objectMapperService;

    @Autowired
    public LinkedHearingGroupServiceImpl(HearingRepository hearingRepository,
                                         LinkedGroupDetailsRepository linkedGroupDetailsRepository,
                                         LinkedHearingDetailsRepository linkedHearingDetailsRepository,
                                         DefaultFutureHearingRepository futureHearingRepository,
                                         ObjectMapperService objectMapperService) {
        super(hearingRepository, linkedGroupDetailsRepository, linkedHearingDetailsRepository);
        this.futureHearingRepository = futureHearingRepository;
        this.objectMapperService = objectMapperService;
        this.hearingRepository = hearingRepository;
    }

    @Override
    public HearingLinkGroupResponse linkHearing(HearingLinkGroupRequest hearingLinkGroupRequest) {
        validateHearingLinkGroupRequest(hearingLinkGroupRequest, null);
        LinkedGroupDetails linkedGroupDetails = updateHearingWithLinkGroup(hearingLinkGroupRequest);

        LinkedHearingGroup linkedHearingGroup = processRequestForListAssist(linkedGroupDetails);

        try {
            futureHearingRepository.createLinkedHearingGroup(objectMapperService
                                                                 .convertObjectToJsonNode(linkedHearingGroup));
            log.info("Response received from ListAssist successfully");
            linkedGroupDetailsRepository
                .updateLinkedGroupDetailsStatus(linkedHearingGroup.getLinkedHearingGroup().getGroupClientReference(),
                                                "ACTIVE");
        } catch (Exception exception) {
            processResponseFromListAssistForCreateLinkedHearing(
                linkedHearingGroup.getLinkedHearingGroup().getGroupClientReference(),
                hearingLinkGroupRequest,
                exception
            );
        }

        HearingLinkGroupResponse hearingLinkGroupResponse = new HearingLinkGroupResponse();
        hearingLinkGroupResponse.setHearingGroupRequestId(linkedGroupDetails.getRequestId());
        return hearingLinkGroupResponse;
    }

    @Override
    public void deleteLinkedHearingGroup(Long hearingGroupId) {

        validateHearingGroup(hearingGroupId);
        List<HearingEntity> linkedGroupHearings = hearingRepository.findByLinkedGroupId(hearingGroupId);
        validateUnlinkingHearingsStatus(linkedGroupHearings);
        validateUnlinkingHearingsWillNotHaveStartDateInThePast(linkedGroupHearings);

        deleteFromLinkedGroupDetails(linkedGroupHearings, hearingGroupId);
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

    private void deleteFromLinkedGroupDetails(List<HearingEntity> linkedGroupHearings, Long hearingGroupId) {
        linkedGroupHearings.forEach(hearingEntity -> {
            // TODO: unlink hearingEntity from the group and persist - https://tools.hmcts.net/jira/browse/HMAN-96
        });
        linkedGroupDetailsRepository.deleteHearingGroup(hearingGroupId);
        // TODO: call ListAssist - https://tools.hmcts.net/jira/browse/HMAN-97
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
                ((BadFutureHearingRequestException) exception).getErrorDetails().getErrorCode());
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
                        throw new HearingNotFoundException(Long.valueOf(linkHearingDetails.getHearingId()),
                                                           HEARING_ID_NOT_FOUND);
                    }
                });
            linkedGroupDetailsRepository.deleteLinkedGroupDetailsStatus(requestId);
            throw new BadRequestException(REJECTED_BY_LIST_ASSIST);
        }  else {
            log.error(
                "Time out exception occurred with status code:  {}",
                ((AuthenticationException) exception).getErrorDetails().getErrorCode());
            linkedGroupDetailsRepository.updateLinkedGroupDetailsStatus(requestId, "ERROR");
            throw new BadRequestException(LIST_ASSIST_FAILED_TO_RESPOND);
        }

    }
}
