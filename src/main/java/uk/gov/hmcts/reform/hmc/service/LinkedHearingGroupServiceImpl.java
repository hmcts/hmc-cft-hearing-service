package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.linkedHearingGroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;

import java.util.Optional;

@Service
@Component
@Slf4j
public class LinkedHearingGroupServiceImpl extends HearingIdValidator implements LinkedHearingGroupService {


    @Autowired
    public LinkedHearingGroupServiceImpl(HearingRepository hearingRepository) {
        super(hearingRepository);

    }


    @Override
    public void linkHearing(HearingLinkGroupRequest hearingLinkGroupRequest) {
        hearingLinkGroupRequest.getHearingsInGroup().forEach(linkHearingDetails -> {
            validateHearingId(Long.valueOf(linkHearingDetails.getHearingId()), "No hearing found for reference: %s");
            Optional<HearingEntity> hearingEntity = hearingRepository
                .findById(Long.valueOf(linkHearingDetails.getHearingId()));
            if (hearingEntity.isPresent()) {
                if (!hearingEntity.get().getCaseHearingRequest().getIsLinkedFlag().booleanValue()) {
                    throw new BadRequestException("002 hearing request isLinked is False");
                }
            }
            //hearing id  in linkedHearingDetails check if its in a group
            // if so throw error
            //check hearing status
            //check date of hearing
            //check date of hearing order
        });
    }
}
