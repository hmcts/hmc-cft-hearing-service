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
import uk.gov.hmcts.reform.hmc.exceptions.BadFutureHearingRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.FutureHearingServerException;
import uk.gov.hmcts.reform.hmc.helper.LinkedGroupDetailsAuditMapper;
import uk.gov.hmcts.reform.hmc.helper.LinkedHearingDetailsAuditMapper;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.repository.DefaultFutureHearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsAuditRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingDetailsAuditRepository;
import uk.gov.hmcts.reform.hmc.validator.LinkedHearingValidator;

import java.util.List;

import static uk.gov.hmcts.reform.hmc.constants.Constants.ERROR;
import static uk.gov.hmcts.reform.hmc.constants.Constants.PENDING;
import static uk.gov.hmcts.reform.hmc.constants.Constants.VERSION_NUMBER_TO_INCREMENT;
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

    @Autowired
    public LinkedHearingGroupServiceImpl(HearingRepository hearingRepository,
                                         LinkedGroupDetailsRepository linkedGroupDetailsRepository,
                                         LinkedHearingValidator linkedHearingValidator,
                                         LinkedHearingDetailsAuditRepository linkedHearingDetailsAuditRepository,
                                         LinkedGroupDetailsAuditRepository linkedGroupDetailsAuditRepository,
                                         LinkedGroupDetailsAuditMapper linkedGroupDetailsAuditMapper,
                                         LinkedHearingDetailsAuditMapper linkedHearingDetailsAuditMapper,
                                         DefaultFutureHearingRepository futureHearingRepository) {
        this.linkedGroupDetailsRepository = linkedGroupDetailsRepository;
        this.linkedHearingValidator = linkedHearingValidator;
        this.hearingRepository = hearingRepository;
        this.linkedHearingDetailsAuditRepository = linkedHearingDetailsAuditRepository;
        this.linkedGroupDetailsAuditRepository = linkedGroupDetailsAuditRepository;
        this.linkedGroupDetailsAuditMapper = linkedGroupDetailsAuditMapper;
        this.linkedHearingDetailsAuditMapper = linkedHearingDetailsAuditMapper;
        this.futureHearingRepository = futureHearingRepository;
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
        linkedGroupHearings.forEach(hearingEntity -> saveLinkedHearingDetailsAudit(hearingEntity));
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

    private void saveLinkedHearingDetailsAudit(HearingEntity hearingEntity) {
        LinkedHearingDetailsAudit linkedHearingDetailsAuditEntity = linkedHearingDetailsAuditMapper
            .modelToEntity(hearingEntity);
        linkedHearingDetailsAuditRepository.save(linkedHearingDetailsAuditEntity);
    }


}
