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
import uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType;
import uk.gov.hmcts.reform.hmc.exceptions.BadFutureHearingRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.FutureHearingServerException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.helper.LinkedGroupDetailsAuditMapper;
import uk.gov.hmcts.reform.hmc.helper.LinkedHearingDetailsAuditMapper;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.listassist.CaseListing;
import uk.gov.hmcts.reform.hmc.model.listassist.HearingGroup;
import uk.gov.hmcts.reform.hmc.model.listassist.LinkedHearingGroup;
import uk.gov.hmcts.reform.hmc.repository.DefaultFutureHearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsAuditRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingDetailsAuditRepository;
import uk.gov.hmcts.reform.hmc.service.common.ObjectMapperService;
import uk.gov.hmcts.reform.hmc.validator.LinkedHearingValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.hmc.constants.Constants.ERROR;
import static uk.gov.hmcts.reform.hmc.constants.Constants.PENDING;
import static uk.gov.hmcts.reform.hmc.constants.Constants.VERSION_NUMBER_TO_INCREMENT;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.LIST_ASSIST_FAILED_TO_RESPOND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.REJECTED_BY_LIST_ASSIST;

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
    private final DefaultFutureHearingRepository futureHearingRepository;
    private final ObjectMapperService objectMapperService;

    @Autowired
    public LinkedHearingGroupServiceImpl(HearingRepository hearingRepository,
                                         LinkedGroupDetailsRepository linkedGroupDetailsRepository,
                                         LinkedHearingValidator linkedHearingValidator,
                                         LinkedHearingDetailsAuditRepository linkedHearingDetailsAuditRepository,
                                         LinkedGroupDetailsAuditRepository linkedGroupDetailsAuditRepository,
                                         LinkedGroupDetailsAuditMapper linkedGroupDetailsAuditMapper,
                                         LinkedHearingDetailsAuditMapper linkedHearingDetailsAuditMapper,
                                         DefaultFutureHearingRepository futureHearingRepository,
                                         ObjectMapperService objectMapperService) {
        this.linkedGroupDetailsRepository = linkedGroupDetailsRepository;
        this.linkedHearingValidator = linkedHearingValidator;
        this.hearingRepository = hearingRepository;
        this.linkedHearingDetailsAuditRepository = linkedHearingDetailsAuditRepository;
        this.linkedGroupDetailsAuditRepository = linkedGroupDetailsAuditRepository;
        this.linkedGroupDetailsAuditMapper = linkedGroupDetailsAuditMapper;
        this.linkedHearingDetailsAuditMapper = linkedHearingDetailsAuditMapper;
        this.futureHearingRepository = futureHearingRepository;
        this.objectMapperService = objectMapperService;
    }


    @Override
    public void linkHearing(HearingLinkGroupRequest hearingLinkGroupRequest) {
        linkedHearingValidator.validateHearingLinkGroupRequest(hearingLinkGroupRequest, null);
    }

    @Override
    public void updateLinkHearing(String requestId, HearingLinkGroupRequest hearingLinkGroupRequest) {
        linkedHearingValidator.validateHearingLinkGroupRequestForUpdate(requestId, hearingLinkGroupRequest);
        //HMAN-94
        LinkedGroupDetails currentLinkGroup = linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(requestId);
        List<HearingEntity> currentHearings =
            hearingRepository.findByLinkedGroupId(currentLinkGroup.getLinkedGroupId());
        unlinkHearingsFromGroup(hearingLinkGroupRequest, currentHearings);
        LinkedGroupDetails linkedGroupDetails = updateLinkGroup(hearingLinkGroupRequest, requestId);
        updateHearingWithLinkGroup(hearingLinkGroupRequest, linkedGroupDetails);
        saveAndAuditLinkHearing(hearingLinkGroupRequest, linkedGroupDetails);

        //HMAN-95
        LinkedHearingGroup linkedHearingGroup = processRequestForListAssist(linkedGroupDetails);
        try {
            futureHearingRepository.updateLinkedHearingGroup(requestId, objectMapperService
                .convertObjectToJsonNode(linkedHearingGroup));
            log.info("Response received from ListAssist successfully");
            linkedGroupDetailsRepository.updateLinkedGroupDetailsStatus(requestId, "ACTIVE");
        } catch (BadFutureHearingRequestException requestException) {
            process400ResponseFromListAssistForLinkedHearing(
                linkedHearingGroup.getLinkedHearingGroup().getGroupClientReference(), hearingLinkGroupRequest);
        } catch (FutureHearingServerException serverException) {
            process500ResponseFromListAssistForLinkedHearing(
                linkedHearingGroup.getLinkedHearingGroup().getGroupClientReference());
        }
    }

    @Override
    @Transactional(noRollbackFor = {BadRequestException.class})
    public void deleteLinkedHearingGroup(Long hearingGroupId) {
        linkedHearingValidator.validateHearingGroup(hearingGroupId);
        List<HearingEntity> linkedGroupHearings = hearingRepository.findByLinkedGroupId(hearingGroupId);
        linkedHearingValidator.validateUnlinkingHearingsStatus(linkedGroupHearings);
        linkedHearingValidator.validateUnlinkingHearingsWillNotHaveStartDateInThePast(linkedGroupHearings);

        deleteFromLinkedGroupDetails(linkedGroupHearings);
    }

    private void deleteFromLinkedGroupDetails(List<HearingEntity> linkedGroupHearings) {
        LinkedGroupDetails linkedGroupDetails = linkedGroupHearings.get(0).getLinkedGroupDetails();
        final String requestId = linkedGroupDetails.getRequestId();
        saveLinkedGroupDetailsAudit(linkedGroupDetails);
        linkedGroupHearings.forEach(hearingEntity -> saveLinkedHearingDetailsAudit(hearingEntity, false));
        saveLinkedGroupDetails(linkedGroupDetails);
        try {
            futureHearingRepository.deleteLinkedHearingGroup(requestId);
            log.debug("Response received from ListAssist successfully");
            linkedGroupDetailsRepository.delete(linkedGroupDetails);
        } catch (BadFutureHearingRequestException requestException) {
            process400ResponseFromListAssistForDeleteLinkedHearing(linkedGroupDetails);
        } catch (FutureHearingServerException serverException) {
            process500ResponseFromListAssistForDeleteLinkedHearing(linkedGroupDetails);
        }

    }


    private void saveAndAuditLinkHearing(HearingLinkGroupRequest hearingLinkGroupRequest,
                                         LinkedGroupDetails linkedGroupDetails) {
        saveLinkedGroupDetailsAudit(linkedGroupDetails);
        hearingLinkGroupRequest.getHearingsInGroup().forEach(hearingInGroup -> {
            Optional<HearingEntity> optionalHearingEntity =
                hearingRepository.findById(Long.valueOf(hearingInGroup.getHearingId()));
            if (optionalHearingEntity.isPresent()) {
                HearingEntity hearingEntity = optionalHearingEntity.get();
                saveLinkedHearingDetailsAudit(hearingEntity, true);
            }
        });
    }

    private void unlinkHearingsFromGroup(HearingLinkGroupRequest hearingLinkGroupRequest,
                                         List<HearingEntity> currentHearings) {
        for (HearingEntity hearingEntity : currentHearings) {
            if (!hearingLinkGroupRequest.getHearingsInGroup()
                .stream().anyMatch(linkHearingDetails ->
                                       Long.valueOf(linkHearingDetails.getHearingId())
                                           .equals(hearingEntity.getId()))) {
                hearingEntity.setLinkedOrder(null);
                hearingEntity.setLinkedGroupDetails(null);
                hearingRepository.save(hearingEntity);
            }
        }
    }

    @Transactional
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

        LinkedGroupDetails linkedGroupDetailsSaved = linkedGroupDetailsRepository.save(linkedGroupDetails);
        return linkedGroupDetailsSaved;
    }

    @Transactional
    private void updateHearingWithLinkGroup(HearingLinkGroupRequest hearingLinkGroupRequest,
                                            LinkedGroupDetails linkedGroupDetailsSaved) {
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


    private void process500ResponseFromListAssistForLinkedHearing(String requestId) {
        linkedGroupDetailsRepository.updateLinkedGroupDetailsStatus(requestId, "ERROR");
        throw new BadRequestException(LIST_ASSIST_FAILED_TO_RESPOND);
    }

    private void process400ResponseFromListAssistForLinkedHearing(String requestId,
                                                                  HearingLinkGroupRequest hearingLinkGroupRequest) {
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
    }

    private void process500ResponseFromListAssistForDeleteLinkedHearing(LinkedGroupDetails linkedGroupDetails) {
        linkedGroupDetails.setStatus(ERROR);
        linkedGroupDetailsRepository.save(linkedGroupDetails);
        throw new BadRequestException(LIST_ASSIST_FAILED_TO_RESPOND);

    }

    private void process400ResponseFromListAssistForDeleteLinkedHearing(LinkedGroupDetails linkedGroupDetails) {
        linkedGroupDetailsRepository.delete(linkedGroupDetails);
        throw new BadRequestException(REJECTED_BY_LIST_ASSIST);
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

    private void saveLinkedHearingDetailsAudit(HearingEntity hearingEntity, boolean update) {
        LinkedHearingDetailsAudit linkedHearingDetailsAuditEntity;
        if (update) {
            linkedHearingDetailsAuditEntity = linkedHearingDetailsAuditMapper
                .modelToEntityUpdate(hearingEntity);
        } else {
            linkedHearingDetailsAuditEntity = linkedHearingDetailsAuditMapper
                .modelToEntity(hearingEntity);
        }
        linkedHearingDetailsAuditRepository.save(linkedHearingDetailsAuditEntity);
    }

}
