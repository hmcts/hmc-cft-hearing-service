package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.core.dependencies.google.common.collect.Lists;
import jakarta.transaction.Transactional;
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
import uk.gov.hmcts.reform.hmc.service.common.LinkedHearingStatusAuditService;
import uk.gov.hmcts.reform.hmc.service.common.ObjectMapperService;
import uk.gov.hmcts.reform.hmc.validator.LinkedHearingValidator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.hmc.constants.Constants.CREATE_LINKED_HEARING_REQUEST;
import static uk.gov.hmcts.reform.hmc.constants.Constants.DELETE_LINKED_HEARING_REQUEST;
import static uk.gov.hmcts.reform.hmc.constants.Constants.FH;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HMC;
import static uk.gov.hmcts.reform.hmc.constants.Constants.LA_FAILURE_SERVER_STATUS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.LA_FAILURE_STATUS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.LIST_ASSIST_SUCCESSFUL_RESPONSE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.SUCCESS_STATUS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.UPDATE_LINKED_HEARING_REQUEST;
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
    private final LinkedHearingStatusAuditService linkedHearingStatusAuditService;
    private final ObjectMapper objectMapper;

    @Autowired
    public LinkedHearingGroupServiceImpl(HearingRepository hearingRepository,
                                         LinkedGroupDetailsRepository linkedGroupDetailsRepository,
                                         LinkedHearingValidator linkedHearingValidator,
                                         DefaultFutureHearingRepository futureHearingRepository,
                                         ObjectMapperService objectMapperService,
                                         AccessControlService accessControlService,
                                         FutureHearingsLinkedHearingGroupService
                                                 futureHearingsLinkedHearingGroupService,
                                         LinkedHearingStatusAuditService linkedHearingStatusAuditService,
                                         ObjectMapper objectMapper) {
        this.linkedGroupDetailsRepository = linkedGroupDetailsRepository;
        this.linkedHearingValidator = linkedHearingValidator;
        this.hearingRepository = hearingRepository;
        this.futureHearingRepository = futureHearingRepository;
        this.objectMapperService = objectMapperService;
        this.accessControlService = accessControlService;
        this.futureHearingsLinkedHearingGroupService = futureHearingsLinkedHearingGroupService;
        this.linkedHearingStatusAuditService = linkedHearingStatusAuditService;
        this.objectMapper = objectMapper;
    }


    @Override
    public HearingLinkGroupResponse linkHearing(HearingLinkGroupRequest hearingLinkGroupRequest,
                                                String clientS2SToken) {
        //POST
        List<HearingEntity> hearingEntities = linkedHearingValidator.validateHearingLinkGroupRequest(
            hearingLinkGroupRequest, null);
        LinkedGroupDetails linkedGroupDetails =
            linkedHearingValidator.updateHearingWithLinkGroup(hearingLinkGroupRequest);
        LinkedHearingGroup linkedHearingGroup =
            futureHearingsLinkedHearingGroupService.processRequestForListAssist(linkedGroupDetails);
        invokeLinkedHearingAuditService(clientS2SToken,linkedGroupDetails, CREATE_LINKED_HEARING_REQUEST,null,
                                         FH, null, hearingEntities);
        try {
            futureHearingRepository.createLinkedHearingGroup(objectMapperService
                                                                 .convertObjectToJsonNode(linkedHearingGroup));
            log.debug(LIST_ASSIST_SUCCESSFUL_RESPONSE);
            linkedGroupDetails.setStatus("ACTIVE");
            linkedGroupDetailsRepository.save(linkedGroupDetails);
            invokeLinkedHearingAuditService(FH,linkedGroupDetails, CREATE_LINKED_HEARING_REQUEST,SUCCESS_STATUS,
                                            HMC, null, hearingEntities);
        } catch (BadFutureHearingRequestException requestException) {
            futureHearingsLinkedHearingGroupService.deleteLinkedHearingGroups(
                linkedHearingGroup.getLinkedHearingGroup().getGroupClientReference(),
                hearingLinkGroupRequest
            );
            JsonNode errorDescription = objectMapper.convertValue(REJECTED_BY_LIST_ASSIST, JsonNode.class);
            invokeLinkedHearingAuditService(FH,linkedGroupDetails, CREATE_LINKED_HEARING_REQUEST,LA_FAILURE_STATUS,
                                            HMC, errorDescription, hearingEntities);
            throw new BadRequestException(REJECTED_BY_LIST_ASSIST);
        } catch (FutureHearingServerException serverException) {
            futureHearingsLinkedHearingGroupService.deleteLinkedHearingGroups(
                linkedHearingGroup.getLinkedHearingGroup().getGroupClientReference(),
                hearingLinkGroupRequest
            );
            JsonNode errorDescription = objectMapper.convertValue(LIST_ASSIST_FAILED_TO_RESPOND, JsonNode.class);
            invokeLinkedHearingAuditService(FH,linkedGroupDetails, CREATE_LINKED_HEARING_REQUEST,
                                            LA_FAILURE_SERVER_STATUS, HMC, errorDescription, hearingEntities);
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
        invokeLinkedHearingAuditService(clientS2SToken,linkedGroupDetails, UPDATE_LINKED_HEARING_REQUEST, null,
                                        FH, null, currentHearings);
        //HMAN-95
        LinkedHearingGroup linkedHearingGroup =
            futureHearingsLinkedHearingGroupService.processRequestForListAssist(linkedGroupDetails);

        try {
            futureHearingRepository.updateLinkedHearingGroup(requestId, objectMapperService
                .convertObjectToJsonNode(linkedHearingGroup));
            log.info(LIST_ASSIST_SUCCESSFUL_RESPONSE);
            linkedGroupDetails.setStatus("ACTIVE");
            linkedGroupDetailsRepository.save(linkedGroupDetails);
            invokeLinkedHearingAuditService(FH,linkedGroupDetails, UPDATE_LINKED_HEARING_REQUEST, SUCCESS_STATUS,
                                            HMC, null, currentHearings);
        } catch (BadFutureHearingRequestException requestException) {
            futureHearingsLinkedHearingGroupService.processAmendLinkedHearingResponse(
                hearingLinkGroupRequest,
                oldHearings,
                linkedGroupDetails,
                previousLinkedGroupDetails
            );
            JsonNode errorDescription = objectMapper.convertValue(REJECTED_BY_LIST_ASSIST, JsonNode.class);
            invokeLinkedHearingAuditService(FH,linkedGroupDetails, UPDATE_LINKED_HEARING_REQUEST, LA_FAILURE_STATUS,
                                            HMC, errorDescription, currentHearings);
            throw new BadRequestException(REJECTED_BY_LIST_ASSIST);
        } catch (FutureHearingServerException serverException) {
            futureHearingsLinkedHearingGroupService.processAmendLinkedHearingResponse(
                hearingLinkGroupRequest,
                oldHearings,
                linkedGroupDetails,
                previousLinkedGroupDetails
            );
            JsonNode errorDescription = objectMapper.convertValue(LIST_ASSIST_FAILED_TO_RESPOND, JsonNode.class);
            invokeLinkedHearingAuditService(FH,linkedGroupDetails, UPDATE_LINKED_HEARING_REQUEST,
                                            LA_FAILURE_SERVER_STATUS, HMC, errorDescription, currentHearings);
            throw new BadRequestException(LIST_ASSIST_FAILED_TO_RESPOND);
        }
    }


    @Override
    @Transactional
    public void deleteLinkedHearingGroup(String requestId, String clientS2SToken) {
        Long linkedGroupId = linkedHearingValidator.validateHearingGroup(requestId);
        List<HearingEntity> linkedGroupHearings = hearingRepository.findByLinkedGroupId(linkedGroupId);
        Optional<LinkedGroupDetails> linkedGroupDetailsOptional = linkedGroupDetailsRepository.findById(linkedGroupId);
        LinkedGroupDetails linkedGroupDetails;
        linkedGroupDetails = (linkedGroupDetailsOptional.isPresent()) ? linkedGroupDetailsOptional.get() : null;

        invokeLinkedHearingAuditService(clientS2SToken,linkedGroupDetails, DELETE_LINKED_HEARING_REQUEST, null,
                                        HMC, null, linkedGroupHearings);
        if (linkedGroupDetails != null) {
            futureHearingsLinkedHearingGroupService
                .processDeleteHearingRequest(linkedGroupHearings, linkedGroupDetails);
            try {
                futureHearingRepository.deleteLinkedHearingGroup(linkedGroupDetails.getRequestId());
                log.debug(LIST_ASSIST_SUCCESSFUL_RESPONSE);
                unlinkHearingsFromGroup(linkedGroupHearings);
                linkedGroupDetailsRepository.delete(linkedGroupDetails);
                invokeLinkedHearingAuditService(FH,linkedGroupDetails, DELETE_LINKED_HEARING_REQUEST, SUCCESS_STATUS,
                                                HMC, null, linkedGroupHearings);
            } catch (BadFutureHearingRequestException requestException) {
                futureHearingsLinkedHearingGroupService.processDeleteHearingResponse(linkedGroupDetails);
                JsonNode errorDescription = objectMapper.convertValue(REJECTED_BY_LIST_ASSIST, JsonNode.class);
                invokeLinkedHearingAuditService(FH,linkedGroupDetails, DELETE_LINKED_HEARING_REQUEST, LA_FAILURE_STATUS,
                                                HMC, errorDescription, linkedGroupHearings);
                throw new BadRequestException(REJECTED_BY_LIST_ASSIST);
            } catch (FutureHearingServerException serverException) {
                futureHearingsLinkedHearingGroupService.processDeleteHearingResponse(linkedGroupDetails);
                JsonNode errorDescription = objectMapper.convertValue(LIST_ASSIST_FAILED_TO_RESPOND, JsonNode.class);
                invokeLinkedHearingAuditService(FH,linkedGroupDetails, DELETE_LINKED_HEARING_REQUEST,
                                                LA_FAILURE_SERVER_STATUS, HMC, errorDescription, linkedGroupHearings);
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
            Long hearingId = hearingEntity.getId();
            if (hearingRepository.existsById(hearingId)) {
                hearingRepository.removeLinkedGroupDetailsAndOrder(hearingId);
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
        hearingsInGroup.sort(Comparator.comparing(LinkedHearingDetails::getHearingOrder,
                                                  Comparator.nullsLast(Long::compareTo))
                                     .thenComparing(Comparator.comparing(LinkedHearingDetails::getHearingId,
                                                  Comparator.nullsLast(Long::compareTo)).reversed()));
        return hearingsInGroup;
    }

    private void verifyAccess(List<HearingEntity> linkedGroupHearings, List<String> requiredRoles) {
        linkedGroupHearings
            .forEach(hearingEntity -> accessControlService
                .verifyAccess(hearingEntity.getId(), requiredRoles));
    }

    private void invokeLinkedHearingAuditService(String source, LinkedGroupDetails linkedGroupDetails,
                                                 String hearingEvent,String httpStatus, String target,
                                                 JsonNode errorDesc, List<HearingEntity> hearingEntities) {
        linkedHearingStatusAuditService.saveLinkedHearingAuditTriageDetails(source, linkedGroupDetails, hearingEvent,
                                                                            httpStatus, target, errorDesc,
                                                                            hearingEntities);
    }

}
