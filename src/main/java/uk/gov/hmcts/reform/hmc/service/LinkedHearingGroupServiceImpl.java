package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.validator.LinkedHearingValidator;

import java.util.List;
import javax.transaction.Transactional;

@Service
@Component
@Slf4j
@Transactional
public class LinkedHearingGroupServiceImpl implements LinkedHearingGroupService {

    private final HearingRepository hearingRepository;
    private final LinkedHearingValidator linkedHearingValidator;

    @Autowired
    public LinkedHearingGroupServiceImpl(HearingRepository hearingRepository,
                                         LinkedHearingValidator linkedHearingValidator) {
        this.hearingRepository = hearingRepository;
        this.linkedHearingValidator = linkedHearingValidator;
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
    public void deleteLinkedHearingGroup(Long hearingGroupId) {
        linkedHearingValidator.validateHearingGroup(hearingGroupId);
        List<HearingEntity> linkedGroupHearings = hearingRepository.findByLinkedGroupId(hearingGroupId);
        linkedHearingValidator.validateUnlinkingHearingsStatus(linkedGroupHearings);
        linkedHearingValidator.validateUnlinkingHearingsWillNotHaveStartDateInThePast(linkedGroupHearings);

        linkedHearingValidator.deleteFromLinkedGroupDetails(linkedGroupHearings, hearingGroupId);
    }

}
