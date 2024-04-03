package uk.gov.hmcts.reform.hmc.service;

import com.microsoft.applicationinsights.core.dependencies.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.data.PreviousLinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.exceptions.BadFutureHearingRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.FhBadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.FutureHearingServerException;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.GetLinkedHearingGroupResponse;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.GroupDetails;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupResponse;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.LinkedHearingDetails;
import uk.gov.hmcts.reform.hmc.model.listassist.LinkedHearingGroup;
import uk.gov.hmcts.reform.hmc.repository.DefaultFutureHearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;
import uk.gov.hmcts.reform.hmc.service.common.ObjectMapperService;
import uk.gov.hmcts.reform.hmc.validator.LinkedHearingValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.hmc.constants.Constants.LIST_ASSIST_SUCCESSFUL_RESPONSE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_LINKED_GROUP_REQUEST_ID_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.LIST_ASSIST_FAILED_TO_RESPOND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.REJECTED_BY_LIST_ASSIST;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.HEARING_VIEWER;

@Service
@Slf4j
public class LinkedHearingGroupServiceImpl implements LinkedHearingGroupService {

    private final HearingRepository hearingRepository;
    private final LinkedGroupDetailsRepository linkedGroupDetailsRepository;
    private final LinkedHearingValidator linkedHearingValidator;
    private final DefaultFutureHearingRepository futureHearingRepository;
    private final ObjectMapperService objectMapperService;
    private final AccessControlService accessControlService;
    private final FutureHearingsLinkedHearingGroupService futureHearingsLinkedHearingGroupService;

    @Autowired
    public LinkedHearingGroupServiceImpl(HearingRepository hearingRepository,
                                         LinkedGroupDetailsRepository linkedGroupDetailsRepository,
                                         LinkedHearingValidator linkedHearingValidator,
                                         DefaultFutureHearingRepository futureHearingRepository,
                                         ObjectMapperService objectMapperService,
                                         AccessControlService accessControlService,
                                         FutureHearingsLinkedHearingGroupService
                                                 futureHearingsLinkedHearingGroupService) {
        this.linkedGroupDetailsRepository = linkedGroupDetailsRepository;
        this.linkedHearingValidator = linkedHearingValidator;
        this.hearingRepository = hearingRepository;
        this.futureHearingRepository = futureHearingRepository;
        this.objectMapperService = objectMapperService;
        this.accessControlService = accessControlService;
        this.futureHearingsLinkedHearingGroupService = futureHearingsLinkedHearingGroupService;
    }


    @Override
    public HearingLinkGroupResponse linkHearing(HearingLinkGroupRequest hearingLinkGroupRequest,
                                                String clientS2SToken) {
        //POST
        linkedHearingValidator.validateHearingLinkGroupRequest(hearingLinkGroupRequest, null);
        LinkedGroupDetails linkedGroupDetails =
            linkedHearingValidator.updateHearingWithLinkGroup(hearingLinkGroupRequest);
        LinkedHearingGroup linkedHearingGroup =
            futureHearingsLinkedHearingGroupService.processRequestForListAssist(linkedGroupDetails);

        try {
            futureHearingRepository.createLinkedHearingGroup(objectMapperService
                                                                 .convertObjectToJsonNode(linkedHearingGroup));
            log.debug(LIST_ASSIST_SUCCESSFUL_RESPONSE);
            linkedGroupDetails.setStatus("ACTIVE");
            linkedGroupDetailsRepository.save(linkedGroupDetails);
        } catch (BadFutureHearingRequestException requestException) {
            futureHearingsLinkedHearingGroupService.deleteLinkedHearingGroups(
                linkedHearingGroup.getLinkedHearingGroup().getGroupClientReference(),
                hearingLinkGroupRequest
            );
            throw new BadRequestException(REJECTED_BY_LIST_ASSIST);
        } catch (FutureHearingServerException serverException) {
            futureHearingsLinkedHearingGroupService.deleteLinkedHearingGroups(
                linkedHearingGroup.getLinkedHearingGroup().getGroupClientReference(),
                hearingLinkGroupRequest
            );
            throw new FhBadRequestException(LIST_ASSIST_FAILED_TO_RESPOND);
        }
        HearingLinkGroupResponse hearingLinkGroupResponse = new HearingLinkGroupResponse();
        hearingLinkGroupResponse.setHearingGroupRequestId(linkedGroupDetails.getRequestId());
        return hearingLinkGroupResponse;
    }

    @Override
    public void updateLinkHearing(String requestId, HearingLinkGroupRequest hearingLinkGroupRequest,
                                  String clientS2SToken) {
        //PUT
        linkedHearingValidator.validateHearingLinkGroupRequestForUpdate(requestId, hearingLinkGroupRequest);
        //HMAN-94
        LinkedGroupDetails currentLinkGroup = linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(requestId);
        PreviousLinkedGroupDetails previousLinkedGroupDetails = mapPreviousLinkGroupDetails(currentLinkGroup);
        HashMap<Long, Long> oldHearings = new HashMap<>();
        hearingRepository.findByLinkedGroupId(currentLinkGroup.getLinkedGroupId()).forEach(hearingEntity ->
            oldHearings.put(hearingEntity.getId(), hearingEntity.getLinkedOrder()));

        List<HearingEntity> currentHearings =
            hearingRepository.findByLinkedGroupId(currentLinkGroup.getLinkedGroupId());
        LinkedGroupDetails linkedGroupDetails =
            futureHearingsLinkedHearingGroupService.processAmendLinkedHearingRequest(
            hearingLinkGroupRequest,
            currentHearings,
            requestId
        );

        //HMAN-95
        LinkedHearingGroup linkedHearingGroup =
            futureHearingsLinkedHearingGroupService.processRequestForListAssist(linkedGroupDetails);

        try {
            futureHearingRepository.updateLinkedHearingGroup(requestId, objectMapperService
                .convertObjectToJsonNode(linkedHearingGroup));
            log.info(LIST_ASSIST_SUCCESSFUL_RESPONSE);
            linkedGroupDetails.setStatus("ACTIVE");
            linkedGroupDetailsRepository.save(linkedGroupDetails);
        } catch (BadFutureHearingRequestException requestException) {
            futureHearingsLinkedHearingGroupService.processAmendLinkedHearingResponse(
                hearingLinkGroupRequest,
                oldHearings,
                linkedGroupDetails,
                previousLinkedGroupDetails
            );
            throw new BadRequestException(REJECTED_BY_LIST_ASSIST);
        } catch (FutureHearingServerException serverException) {
            futureHearingsLinkedHearingGroupService.processAmendLinkedHearingResponse(
                hearingLinkGroupRequest,
                oldHearings,
                linkedGroupDetails,
                previousLinkedGroupDetails
            );
            throw new BadRequestException(LIST_ASSIST_FAILED_TO_RESPOND);
        }
    }


    @Override
    public void deleteLinkedHearingGroup(String requestId, String clientS2SToken) {
        Long linkedGroupId = linkedHearingValidator.validateHearingGroup(requestId);
        List<HearingEntity> linkedGroupHearings = hearingRepository.findByLinkedGroupId(linkedGroupId);
        Optional<LinkedGroupDetails> linkedGroupDetailsOptional = linkedGroupDetailsRepository.findById(linkedGroupId);
        LinkedGroupDetails linkedGroupDetails;
        linkedGroupDetails = (linkedGroupDetailsOptional.isPresent()) ? linkedGroupDetailsOptional.get() : null;
        if (linkedGroupDetails != null) {
            futureHearingsLinkedHearingGroupService
                .processDeleteHearingRequest(linkedGroupHearings, linkedGroupDetails);
            try {
                futureHearingRepository.deleteLinkedHearingGroup(linkedGroupDetails.getRequestId());
                log.debug(LIST_ASSIST_SUCCESSFUL_RESPONSE);
                unlinkHearingsFromGroup(linkedGroupHearings);
                linkedGroupDetailsRepository.delete(linkedGroupDetails);

            } catch (BadFutureHearingRequestException requestException) {
                futureHearingsLinkedHearingGroupService.processDeleteHearingResponse(linkedGroupDetails);
                throw new BadRequestException(REJECTED_BY_LIST_ASSIST);
            } catch (FutureHearingServerException serverException) {
                futureHearingsLinkedHearingGroupService.processDeleteHearingResponse(linkedGroupDetails);
                throw new BadRequestException(LIST_ASSIST_FAILED_TO_RESPOND);
            }
        }
    }

    @Override
    public GetLinkedHearingGroupResponse getLinkedHearingGroupResponse(String requestId) {
        linkedHearingValidator.validateRequestId(requestId, INVALID_LINKED_GROUP_REQUEST_ID_DETAILS);
        List<HearingEntity> linkedGroupHearings = hearingRepository.findByRequestId(requestId);
        verifyAccess(linkedGroupHearings, Lists.newArrayList(HEARING_VIEWER));
        return getLinkedHearingGroupDetails(requestId);
    }

    private PreviousLinkedGroupDetails mapPreviousLinkGroupDetails(LinkedGroupDetails oldLinkedGroupDetails) {
        PreviousLinkedGroupDetails linkedGroupDetails = new PreviousLinkedGroupDetails();
        linkedGroupDetails.setLinkedGroupId(oldLinkedGroupDetails.getLinkedGroupId());
        linkedGroupDetails.setRequestId(oldLinkedGroupDetails.getRequestId());
        linkedGroupDetails.setRequestName(oldLinkedGroupDetails.getRequestName());
        linkedGroupDetails.setRequestDateTime(oldLinkedGroupDetails.getRequestDateTime());
        linkedGroupDetails.setReasonForLink(oldLinkedGroupDetails.getReasonForLink());
        linkedGroupDetails.setLinkType(oldLinkedGroupDetails.getLinkType());
        linkedGroupDetails.setStatus(oldLinkedGroupDetails.getStatus());
        linkedGroupDetails.setLinkedComments(oldLinkedGroupDetails.getLinkedComments());
        linkedGroupDetails.setLinkedGroupLatestVersion(oldLinkedGroupDetails.getLinkedGroupLatestVersion());
        return linkedGroupDetails;
    }


    private void unlinkHearingsFromGroup(List<HearingEntity> linkedGroupHearings) {
        for (HearingEntity hearingEntity : linkedGroupHearings) {
            Optional<HearingEntity> hearingEntityOptional = hearingRepository.findById(hearingEntity.getId());
            HearingEntity hearingEntityToUpdate =
                (hearingEntityOptional.isPresent()) ? hearingEntityOptional.get() : null;
            if (hearingEntityToUpdate != null) {
                hearingEntityToUpdate.setLinkedOrder(null);
                hearingEntityToUpdate.setLinkedGroupDetails(null);
                hearingRepository.save(hearingEntityToUpdate);
            }
        }
    }

    private GetLinkedHearingGroupResponse getLinkedHearingGroupDetails(String requestId) {
        LinkedGroupDetails linkedGroupDetails =
            linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(requestId);
        GetLinkedHearingGroupResponse response = new GetLinkedHearingGroupResponse();
        response.setGroupDetails(getGroupDetails(linkedGroupDetails));
        response.setHearingsInGroup(getHearingsInGroup(linkedGroupDetails.getLinkedGroupId()));
        return response;
    }

    private GroupDetails getGroupDetails(LinkedGroupDetails linkedGroupDetails) {
        GroupDetails groupDetails = new GroupDetails();
        groupDetails.setGroupName(linkedGroupDetails.getRequestName());
        groupDetails.setGroupReason(linkedGroupDetails.getReasonForLink());
        groupDetails.setGroupComments(linkedGroupDetails.getLinkedComments());
        groupDetails.setGroupLinkType(linkedGroupDetails.getLinkType().label);

        return groupDetails;
    }

    private List<LinkedHearingDetails> getHearingsInGroup(Long hearingGroupId) {
        List<HearingEntity> linkedGroupHearings =
            hearingRepository.findByLinkedGroupId(hearingGroupId);
        List<LinkedHearingDetails> hearingsInGroup = new ArrayList<>();
        linkedGroupHearings.forEach(hearing -> {
            LinkedHearingDetails response = new LinkedHearingDetails();
            response.setHearingId(hearing.getId());
            response.setHearingOrder(hearing.getLinkedOrder());
            response.setCaseRef(hearing.getLatestCaseHearingRequest().getCaseReference());
            response.setHmctsInternalCaseName(hearing.getLatestCaseHearingRequest().getHmctsInternalCaseName());
            hearingsInGroup.add(response);
        });

        return hearingsInGroup;
    }

    private void verifyAccess(List<HearingEntity> linkedGroupHearings, List<String> requiredRoles) {
        linkedGroupHearings.stream()
            .forEach(hearingEntity -> accessControlService
                .verifyAccess(hearingEntity.getId(), requiredRoles));
    }

}
