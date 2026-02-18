package uk.gov.hmcts.reform.hmc.service;

import com.microsoft.applicationinsights.core.dependencies.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetailsAudit;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingDetailsAudit;
import uk.gov.hmcts.reform.hmc.data.PreviousLinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.helper.LinkedGroupDetailsAuditMapper;
import uk.gov.hmcts.reform.hmc.helper.LinkedHearingDetailsAuditMapper;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.listassist.CaseListing;
import uk.gov.hmcts.reform.hmc.model.listassist.HearingGroup;
import uk.gov.hmcts.reform.hmc.model.listassist.LinkedHearingGroup;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsAuditRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingDetailsAuditRepository;
import uk.gov.hmcts.reform.hmc.validator.LinkedHearingValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.hmc.constants.Constants.PENDING;
import static uk.gov.hmcts.reform.hmc.constants.Constants.VERSION_NUMBER_TO_INCREMENT;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.HEARING_MANAGER;

@Service
@Slf4j
public class FutureHearingsLinkedHearingGroupService {

    private final HearingRepository hearingRepository;
    private final LinkedGroupDetailsRepository linkedGroupDetailsRepository;
    private final LinkedHearingValidator linkedHearingValidator;
    private final LinkedHearingDetailsAuditRepository linkedHearingDetailsAuditRepository;
    private final LinkedGroupDetailsAuditRepository linkedGroupDetailsAuditRepository;
    private final LinkedGroupDetailsAuditMapper linkedGroupDetailsAuditMapper;
    private final LinkedHearingDetailsAuditMapper linkedHearingDetailsAuditMapper;
    private final AccessControlService accessControlService;

    @Autowired
    public FutureHearingsLinkedHearingGroupService(
        HearingRepository hearingRepository,
        LinkedGroupDetailsRepository linkedGroupDetailsRepository,
        LinkedHearingValidator linkedHearingValidator,
        LinkedHearingDetailsAuditRepository linkedHearingDetailsAuditRepository,
        LinkedGroupDetailsAuditRepository linkedGroupDetailsAuditRepository,
        LinkedGroupDetailsAuditMapper linkedGroupDetailsAuditMapper,
        LinkedHearingDetailsAuditMapper linkedHearingDetailsAuditMapper,
        AccessControlService accessControlService) {
        this.linkedGroupDetailsRepository = linkedGroupDetailsRepository;
        this.linkedHearingValidator = linkedHearingValidator;
        this.hearingRepository = hearingRepository;
        this.linkedHearingDetailsAuditRepository = linkedHearingDetailsAuditRepository;
        this.linkedGroupDetailsAuditRepository = linkedGroupDetailsAuditRepository;
        this.linkedGroupDetailsAuditMapper = linkedGroupDetailsAuditMapper;
        this.linkedHearingDetailsAuditMapper = linkedHearingDetailsAuditMapper;
        this.accessControlService = accessControlService;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processDeleteHearingRequest(List<HearingEntity> linkedGroupHearings,
                                            LinkedGroupDetails linkedGroupDetails) {
        verifyAccess(linkedGroupHearings, Lists.newArrayList(HEARING_MANAGER));
        linkedHearingValidator.validateUnlinkingHearingsStatus(linkedGroupHearings);
        linkedHearingValidator.validateUnlinkingHearingsWillNotHaveStartDateInThePast(linkedGroupHearings);

        saveLinkedGroupDetailsAudit(linkedGroupDetails);
        linkedGroupHearings.forEach(hearingEntity ->
                                        saveLinkedHearingDetailsAudit(hearingEntity, linkedGroupDetails));
        saveLinkedGroupDetails(linkedGroupDetails);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processDeleteHearingResponse(LinkedGroupDetails linkedGroupDetails) {
        linkedGroupDetailsAuditRepository.deleteLinkedGroupDetailsAudit(
            linkedGroupDetails.getLinkedGroupId(),
            (linkedGroupDetails.getLinkedGroupLatestVersion() - 1)
        );
        linkedHearingDetailsAuditRepository.deleteLinkedHearingsDetailsAudit(
            linkedGroupDetails.getLinkedGroupId(),
            (linkedGroupDetails.getLinkedGroupLatestVersion() - 1)
        );
        linkedGroupDetails.setStatus("ACTIVE");
        linkedGroupDetails.setLinkedGroupLatestVersion((linkedGroupDetails.getLinkedGroupLatestVersion() - 1));
        linkedGroupDetailsRepository.save(linkedGroupDetails);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public LinkedGroupDetails processAmendLinkedHearingRequest(HearingLinkGroupRequest hearingLinkGroupRequest,
                                                               List<HearingEntity> currentHearings,
                                                               String requestId) {
        unlinkHearingsFromGroup(hearingLinkGroupRequest, currentHearings);
        LinkedGroupDetails linkedGroupDetails = updateLinkGroup(hearingLinkGroupRequest, requestId);
        updateHearingWithLinkGroup(hearingLinkGroupRequest, linkedGroupDetails);
        saveAndAuditLinkHearing(hearingLinkGroupRequest, linkedGroupDetails);
        return linkedGroupDetails;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processAmendLinkedHearingResponse(HearingLinkGroupRequest hearingLinkGroupRequest,
                                                  Map<Long, Long> currentHearings,
                                                  LinkedGroupDetails linkedGroupDetails,
                                                  PreviousLinkedGroupDetails previousLinkedGroupDetails) {
        linkedGroupDetailsAuditRepository.deleteLinkedGroupDetailsAudit(
            linkedGroupDetails.getLinkedGroupId(),
            linkedGroupDetails.getLinkedGroupLatestVersion()
        );
        linkedHearingDetailsAuditRepository.deleteLinkedHearingsDetailsAudit(
            linkedGroupDetails.getLinkedGroupId(),
            linkedGroupDetails.getLinkedGroupLatestVersion()
        );
        unlinkNewHearingsFromGroup(hearingLinkGroupRequest);
        relinkOldHearingsFromGroup(currentHearings, linkedGroupDetails);
        rollBackLinkGroupDetails(previousLinkedGroupDetails);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public LinkedHearingGroup processRequestForListAssist(LinkedGroupDetails linkedGroupDetails) {
        HearingGroup hearingGroup = new HearingGroup();
        hearingGroup.setGroupClientReference(linkedGroupDetails.getRequestId());
        hearingGroup.setGroupName(linkedGroupDetails.getRequestName());
        hearingGroup.setGroupReason(linkedGroupDetails.getReasonForLink());
        hearingGroup.setGroupLinkType(linkedGroupDetails.getLinkType().getLabel());
        hearingGroup.setGroupComment(linkedGroupDetails.getLinkedComments());
        hearingGroup.setGroupStatus("LHSAWL");
        ArrayList<CaseListing> caseListingArrayList = new ArrayList<>();
        List<HearingEntity> hearingEntities = hearingRepository
            .findByLinkedGroupId(linkedGroupDetails.getLinkedGroupId());
        for (HearingEntity hearingEntity : hearingEntities) {
            CaseListing caseListing = new CaseListing();
            caseListing.setCaseListingRequestId(hearingEntity.getId().toString());
            caseListing.setCaseLinkOrder(getCaseLinkOrder(hearingEntity));
            caseListingArrayList.add(caseListing);
        }
        hearingGroup.setGroupHearings(caseListingArrayList);
        LinkedHearingGroup linkedHearingGroup = new LinkedHearingGroup();
        linkedHearingGroup.setLinkedHearingGroup(hearingGroup);
        return linkedHearingGroup;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteLinkedHearingGroups(String requestId,
                                          HearingLinkGroupRequest hearingLinkGroupRequest) {
        hearingLinkGroupRequest.getHearingsInGroup()
            .forEach(linkHearingDetails -> {
                Long hearingId = Long.valueOf(linkHearingDetails.getHearingId());
                if (hearingRepository.existsById(hearingId)) {
                    hearingRepository.removeLinkedGroupDetailsAndOrder(hearingId);
                } else {
                    throw new HearingNotFoundException(hearingId, HEARING_ID_NOT_FOUND);
                }
            });
        LinkedGroupDetails linkedGroupDetails =
            linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(requestId);
        linkedGroupDetailsRepository.delete(linkedGroupDetails);
    }

    private void rollBackLinkGroupDetails(PreviousLinkedGroupDetails oldLinkedGroupDetails) {
        LinkedGroupDetails linkedGroupDetails = linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(
            oldLinkedGroupDetails.getRequestId());
        linkedGroupDetails.setLinkedGroupId(oldLinkedGroupDetails.getLinkedGroupId());
        linkedGroupDetails.setRequestId(oldLinkedGroupDetails.getRequestId());
        linkedGroupDetails.setRequestName(oldLinkedGroupDetails.getRequestName());
        linkedGroupDetails.setRequestDateTime(oldLinkedGroupDetails.getRequestDateTime());
        linkedGroupDetails.setLinkType(oldLinkedGroupDetails.getLinkType());
        linkedGroupDetails.setStatus(oldLinkedGroupDetails.getStatus());
        linkedGroupDetails.setLinkedComments(oldLinkedGroupDetails.getLinkedComments());
        linkedGroupDetails.setReasonForLink(oldLinkedGroupDetails.getReasonForLink());
        linkedGroupDetails.setLinkedGroupLatestVersion(oldLinkedGroupDetails.getLinkedGroupLatestVersion());
        linkedGroupDetailsRepository.save(linkedGroupDetails);
    }

    private void unlinkHearingsFromGroup(HearingLinkGroupRequest hearingLinkGroupRequest,
                                         List<HearingEntity> currentHearings) {
        for (HearingEntity hearingEntity : currentHearings) {
            if (hearingLinkGroupRequest.getHearingsInGroup()
                .stream().noneMatch(linkHearingDetails ->
                    Long.valueOf(linkHearingDetails.getHearingId()).equals(hearingEntity.getId()))
            ) {
                hearingEntity.setLinkedOrder(null);
                hearingEntity.setLinkedGroupDetails(null);
                hearingRepository.removeLinkedGroupDetailsAndOrder(hearingEntity.getId());
            }
        }
    }

    private void relinkOldHearingsFromGroup(Map<Long, Long> currentHearings,
                                            LinkedGroupDetails linkedGroupDetails) {
        for (Map.Entry<Long, Long> entry : currentHearings.entrySet()) {
            Long hearingId = entry.getKey();
            Long order = entry.getValue();
            if (hearingRepository.existsById(hearingId)) {
                hearingRepository.updateLinkedGroupDetailsAndOrder(hearingId, linkedGroupDetails, order);
            }
        }
    }

    private void unlinkNewHearingsFromGroup(HearingLinkGroupRequest hearingLinkGroupRequest) {
        hearingLinkGroupRequest.getHearingsInGroup().forEach(hearing -> {
            Long hearingId = Long.valueOf(hearing.getHearingId());
            hearingRepository.removeLinkedGroupDetailsAndOrder(hearingId);
        });
    }

    private void saveAndAuditLinkHearing(HearingLinkGroupRequest hearingLinkGroupRequest,
                                         LinkedGroupDetails linkedGroupDetails) {
        saveLinkedGroupDetailsAudit(linkedGroupDetails);
        hearingLinkGroupRequest.getHearingsInGroup().forEach(hearingInGroup -> {
            Optional<HearingEntity> optionalHearingEntity =
                hearingRepository.findById(Long.valueOf(hearingInGroup.getHearingId()));
            if (optionalHearingEntity.isPresent()) {
                HearingEntity hearingEntity = optionalHearingEntity.get();
                saveLinkedHearingDetailsAudit(hearingEntity, linkedGroupDetails);
            }
        });
    }

    protected LinkedGroupDetails updateLinkGroup(HearingLinkGroupRequest hearingLinkGroupRequest,
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

        return linkedGroupDetailsRepository.save(linkedGroupDetails);
    }

    private void updateHearingWithLinkGroup(HearingLinkGroupRequest hearingLinkGroupRequest,
                                            LinkedGroupDetails linkedGroupDetailsSaved) {
        hearingLinkGroupRequest.getHearingsInGroup()
            .forEach(linkHearingDetails -> {
                Long hearingId = Long.valueOf(linkHearingDetails.getHearingId());
                if (hearingRepository.existsById(hearingId)) {
                    Long hearingOrder =
                        linkedHearingValidator.getHearingOrder(linkHearingDetails, hearingLinkGroupRequest);
                    hearingRepository.updateLinkedGroupDetailsAndOrder(hearingId,
                                                                       linkedGroupDetailsSaved,
                                                                       hearingOrder);
                }
            });
    }

    private Integer getCaseLinkOrder(HearingEntity hearingEntity) {
        if (hearingEntity.getLinkedOrder() != null) {
            return Integer.valueOf(hearingEntity.getLinkedOrder().toString());
        }
        return null;
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

    private void saveLinkedHearingDetailsAudit(HearingEntity hearingEntity, LinkedGroupDetails linkedGroupDetails) {
        LinkedHearingDetailsAudit linkedHearingDetailsAuditEntity;
        linkedHearingDetailsAuditEntity = linkedHearingDetailsAuditMapper
            .modelToEntity(hearingEntity, linkedGroupDetails);
        linkedHearingDetailsAuditRepository.save(linkedHearingDetailsAuditEntity);
    }

    private void verifyAccess(List<HearingEntity> linkedGroupHearings, List<String> requiredRoles) {
        linkedGroupHearings.stream()
            .forEach(hearingEntity -> accessControlService
                .verifyAccess(hearingEntity.getId(), requiredRoles));
    }

}
