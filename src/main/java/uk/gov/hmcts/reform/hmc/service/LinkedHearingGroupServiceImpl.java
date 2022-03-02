package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingDetails;
import uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType;
import uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.model.linkedHearingGroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedHearingGroup.LinkHearingDetails;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingDetailsRepository;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Component
@Slf4j
public class LinkedHearingGroupServiceImpl extends HearingIdValidator implements LinkedHearingGroupService {

    private final LinkedHearingDetailsRepository linkedHearingDetailsRepository;

    @Autowired
    public LinkedHearingGroupServiceImpl(HearingRepository hearingRepository,
                                         LinkedHearingDetailsRepository linkedHearingDetailsRepository) {
        super(hearingRepository);
        this.linkedHearingDetailsRepository = linkedHearingDetailsRepository;

    }


    @Override
    public void linkHearing(HearingLinkGroupRequest hearingLinkGroupRequest) {
        //hman-55 step 3 / hman-56 step 5
        List<LinkHearingDetails> listDistinct = hearingLinkGroupRequest.getHearingsInGroup().stream()
            .distinct().collect(Collectors.toList());
        if (listDistinct.size() != hearingLinkGroupRequest.getHearingsInGroup().size()) {
            throw new BadRequestException("001 Insufficient requestIds");
        }

        //hman -55 step 4 / hman-56 step 6
        hearingLinkGroupRequest.getHearingsInGroup().forEach(linkHearingDetails -> {
            validateHearingId(Long.valueOf(linkHearingDetails.getHearingId()), "No hearing found for reference: %s");
            Optional<HearingEntity> hearingEntity = hearingRepository
                .findById(Long.valueOf(linkHearingDetails.getHearingId()));
            if (hearingEntity.isPresent()) {
                //hman-55 step 4.1 / hman-56 step 6.1
                if (!hearingEntity.get().getCaseHearingRequest().getIsLinkedFlag().booleanValue()) {
                    throw new BadRequestException("002 hearing request isLinked is False");
                }
            }

            //hearing id  in linkedHearingDetails check if its in a group
            //hman-55 step 4.2 / hman-56 step 6.2
            List<LinkedHearingDetails> lhd = linkedHearingDetailsRepository.getLinkedHearingDetailsById(Long.valueOf(
                linkHearingDetails.getHearingId()));

            //hman-55 step 4.3 / hamn-56 step 6.3
            if (!PutHearingStatus.isValid(lhd.get(0).getLinkedGroup().getStatus())
                && LocalDate.now().isBefore(lhd.get(0).getLinkedGroup().getRequestDateTime().toLocalDate())) {
                throw new BadRequestException("004 Invalid state for hearing request <hearingId>");
            }

            //hman-55 step 4.4 / hman-56 step 6.4
            if (LinkType.isValid(lhd.get(0).getLinkedGroup().getLinkType())) {
                //check link order is unique
                Long linkedOrder = lhd.get(0).getLinkedOrder();
            }
        });
    }
}
