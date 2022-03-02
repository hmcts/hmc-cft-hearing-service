package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingDetails;
import uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.model.LinkType;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingDetailsRepository;
import uk.gov.hmcts.reform.hmc.validator.LinkedHearingGroupValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Component
@Slf4j
public class LinkedHearingGroupServiceImpl extends LinkedHearingGroupValidator implements LinkedHearingGroupService {

    private final LinkedHearingDetailsRepository linkedHearingDetailsRepository;

    @Autowired
    public LinkedHearingGroupServiceImpl(HearingRepository hearingRepository,
                                         LinkedGroupDetailsRepository linkedGroupDetailsRepository,
                                         LinkedHearingDetailsRepository linkedHearingDetailsRepository) {
        super(hearingRepository, linkedGroupDetailsRepository);
        this.linkedHearingDetailsRepository = linkedHearingDetailsRepository;

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
            //hearing id  in linkedHearingDetails check if it's in a group
            List<LinkedHearingDetails> lhd = linkedHearingDetailsRepository.getLinkedHearingDetailsById(Long.valueOf(
                linkHearingDetails.getHearingId()));

            if (!lhd.isEmpty()) {
                // if so throw error
            }
            if (!PutHearingStatus.isValid(lhd.get(0).getLinkedGroup().getStatus())) {
                //check hearing status
                //  throw new BadRequestException();
            }
            if (LocalDate.now().isBefore(lhd.get(0).getLinkedGroup().getRequestDateTime().toLocalDate())) {
                //check date of hearing
                //  throw new BadRequestException();
            }
            if (LinkType.ORDERED.equals(lhd.get(0).getLinkedGroup().getLinkType())) {
                //check link order is unique
                Long linkedOrder = lhd.get(0).getLinkedOrder();
            }
        });
    }
}
