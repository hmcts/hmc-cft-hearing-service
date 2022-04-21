package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetailsAudit;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingDetailsAudit;
import uk.gov.hmcts.reform.hmc.exceptions.LinkedGroupNotFoundException;
import uk.gov.hmcts.reform.hmc.helper.LinkedGroupDetailsAuditMapper;
import uk.gov.hmcts.reform.hmc.helper.LinkedHearingDetailsAuditMapper;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.GetLinkedHearingGroupResponse;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.GroupDetails;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.LinkedHearingDetails;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsAuditRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingDetailsAuditRepository;
import uk.gov.hmcts.reform.hmc.validator.LinkedHearingValidator;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.hmc.constants.Constants.PENDING;
import static uk.gov.hmcts.reform.hmc.constants.Constants.VERSION_NUMBER_TO_INCREMENT;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_LINKED_GROUP_REQUEST_ID_DETAILS;

@Service
@Component
@Slf4j
public class LinkedHearingGroupServiceImpl implements LinkedHearingGroupService {

    private final HearingRepository hearingRepository;
    private final LinkedGroupDetailsRepository linkedGroupDetailsRepository;
    private final LinkedHearingValidator linkedHearingValidator;
    private final LinkedHearingDetailsAuditRepository linkedHearingDetailsAuditRepository;
    private final LinkedGroupDetailsAuditRepository linkedGroupDetailsAuditRepository;
    private final LinkedGroupDetailsAuditMapper linkedGroupDetailsAuditMapper;
    private final LinkedHearingDetailsAuditMapper linkedHearingDetailsAuditMapper;

    @Autowired
    public LinkedHearingGroupServiceImpl(HearingRepository hearingRepository,
                                         LinkedGroupDetailsRepository linkedGroupDetailsRepository,
                                         LinkedHearingValidator linkedHearingValidator,
                                         LinkedHearingDetailsAuditRepository linkedHearingDetailsAuditRepository,
                                         LinkedGroupDetailsAuditRepository linkedGroupDetailsAuditRepository,
                                         LinkedGroupDetailsAuditMapper linkedGroupDetailsAuditMapper,
                                         LinkedHearingDetailsAuditMapper linkedHearingDetailsAuditMapper) {
        this.linkedGroupDetailsRepository = linkedGroupDetailsRepository;
        this.linkedHearingValidator = linkedHearingValidator;
        this.hearingRepository = hearingRepository;
        this.linkedHearingDetailsAuditRepository = linkedHearingDetailsAuditRepository;
        this.linkedGroupDetailsAuditRepository = linkedGroupDetailsAuditRepository;
        this.linkedGroupDetailsAuditMapper = linkedGroupDetailsAuditMapper;
        this.linkedHearingDetailsAuditMapper = linkedHearingDetailsAuditMapper;
    }


    @Override
    public void linkHearing(HearingLinkGroupRequest hearingLinkGroupRequest) {
        linkedHearingValidator.validateHearingLinkGroupRequest(hearingLinkGroupRequest, null);
    }

    @Override
    public void updateLinkHearing(String requestId, HearingLinkGroupRequest hearingLinkGroupRequest) {
        linkedHearingValidator.validateHearingLinkGroupRequestForUpdate(requestId, hearingLinkGroupRequest);
    }

    @Override
    @Transactional
    public void deleteLinkedHearingGroup(Long hearingGroupId) {
        linkedHearingValidator.validateHearingGroup(hearingGroupId);
        List<HearingEntity> linkedGroupHearings = hearingRepository.findByLinkedGroupId(hearingGroupId);
        linkedHearingValidator.validateUnlinkingHearingsStatus(linkedGroupHearings);
        linkedHearingValidator.validateUnlinkingHearingsWillNotHaveStartDateInThePast(linkedGroupHearings);

        deleteFromLinkedGroupDetails(linkedGroupHearings);
    }

    @Override
    public GetLinkedHearingGroupResponse getLinkedHearingGroupDetails(String requestId) {
        LinkedGroupDetails linkedGroupDetails =
            linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(requestId);
        if (linkedGroupDetails != null) {
            return getLinkedHearingGroupDetails(linkedGroupDetails);
        } else {
            throw new LinkedGroupNotFoundException(requestId,INVALID_LINKED_GROUP_REQUEST_ID_DETAILS);
        }
    }

    private GetLinkedHearingGroupResponse getLinkedHearingGroupDetails(LinkedGroupDetails linkedGroupDetails) {
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
            hearingsInGroup.add(response);
        });

        return hearingsInGroup;
    }

    private void deleteFromLinkedGroupDetails(List<HearingEntity> linkedGroupHearings) {
        LinkedGroupDetails linkedGroupDetails = linkedGroupHearings.get(0).getLinkedGroupDetails();
        saveLinkedGroupDetailsAudit(linkedGroupDetails);
        linkedGroupHearings.forEach(hearingEntity -> saveLinkedHearingDetailsAudit(hearingEntity));
        saveLinkedGroupDetails(linkedGroupDetails);
        // TODO: call ListAssist - https://tools.hmcts.net/jira/browse/HMAN-97
    }

    private void saveLinkedGroupDetails(LinkedGroupDetails linkedGroupDetails) {
        Long versionNumber = linkedGroupDetails.getLinkedGroupLatestVersion();
        linkedGroupDetails.setLinkedGroupLatestVersion(versionNumber + VERSION_NUMBER_TO_INCREMENT);
        linkedGroupDetails.setStatus(PENDING);
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
