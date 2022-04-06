package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupResponse;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;
import uk.gov.hmcts.reform.hmc.validator.LinkedHearingValidator;

import java.util.List;
import javax.transaction.Transactional;

@Service
@Component
@Slf4j
@Transactional
public class LinkedHearingGroupServiceImpl extends LinkedHearingValidator implements LinkedHearingGroupService {

    @Autowired
    public LinkedHearingGroupServiceImpl(HearingRepository hearingRepository,
                                         LinkedGroupDetailsRepository linkedGroupDetailsRepository) {
        super(hearingRepository, linkedGroupDetailsRepository);
    }

    @Override
    public HearingLinkGroupResponse linkHearing(HearingLinkGroupRequest hearingLinkGroupRequest) {
        validateHearingLinkGroupRequest(hearingLinkGroupRequest, null);
        LinkedGroupDetails linkedGroupDetails = updateHearingWithLinkGroup(hearingLinkGroupRequest);
        HearingLinkGroupResponse hearingLinkGroupResponse = new HearingLinkGroupResponse();
        hearingLinkGroupResponse.setHearingGroupRequestId(linkedGroupDetails.getRequestId());
        return  hearingLinkGroupResponse;
    }

    @Override
    public void updateLinkHearing(String requestId, HearingLinkGroupRequest hearingLinkGroupRequest) {
        validateHearingLinkGroupRequestForUpdate(requestId, hearingLinkGroupRequest);
    }

    @Override
    public void deleteLinkedHearingGroup(Long hearingGroupId) {
        validateHearingGroup(hearingGroupId);
        List<HearingEntity> linkedGroupHearings = hearingRepository.findByLinkedGroupId(hearingGroupId);
        validateUnlinkingHearingsStatus(linkedGroupHearings);
        validateUnlinkingHearingsWillNotHaveStartDateInThePast(linkedGroupHearings);

        deleteFromLinkedGroupDetails(linkedGroupHearings, hearingGroupId);
    }
}
