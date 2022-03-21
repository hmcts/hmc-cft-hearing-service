package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType;
import uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.LinkHearingDetails;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_NOT_FOUND;

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
        validateHearingLinkGroupRequest(hearingLinkGroupRequest);
    }

    private void validateHearingLinkGroupRequest(HearingLinkGroupRequest hearingLinkGroupRequest) {
        //hman -55 step 4 / hman-56 step 6
        hearingLinkGroupRequest.getHearingsInGroup().forEach(details -> {
            //hman-55 step 3 / hman-56 step 5
            int occurrences = getIdOccurrences(hearingLinkGroupRequest.getHearingsInGroup(), details.getHearingId());
            if (occurrences > 1) {
                throw new BadRequestException("001 Insufficient requestIds");
            }

            validateHearingId(Long.valueOf(details.getHearingId()), HEARING_ID_NOT_FOUND);
            Optional<HearingEntity> hearingEntity = hearingRepository
                .findById(Long.valueOf(details.getHearingId()));

            if (hearingEntity.isPresent()) {
                //hman-55 step 4.1 / hman-56 step 6.1
                if (!hearingEntity.get().getIsLinkedFlag().booleanValue()) {
                    throw new BadRequestException("002 hearing request isLinked is False");
                }

                //hearing id  in linkedHearingDetails check if it's in a group
                //hman-55 step 4.2 / hman-56 step 6.2
                if (hearingEntity.get().getLinkedGroupDetails() != null) {
                    throw new BadRequestException("003 hearing request already in a group");
                }

                //hman-55 step 4.3 / hamn-56 step 6.3
                if (!PutHearingStatus.isValid(hearingEntity.get().getStatus())
                    || filterHearingResponses(hearingEntity.get()).isBefore(LocalDate.now())) {
                    throw new BadRequestException("004 Invalid state for hearing request "
                                                      + details.getHearingId());
                }

                //hman-55 step 4.4 / hman-56 step 6.4
                LinkType value = LinkType.getByLabel(hearingLinkGroupRequest.getGroupDetails().getGroupLinkType());
                if (value == null) {
                    throw new BadRequestException("Invalid value for GroupLinkType");
                }
                if (LinkType.ORDERED.equals(value)) {
                    if (details.getHearingOrder() == 0) {
                        throw new BadRequestException("Hearing order must exist and be greater than 0");
                    }
                    int counter = getOrderOccurrences(
                        hearingLinkGroupRequest.getHearingsInGroup(),
                        details.getHearingOrder()
                    );
                    if (counter > 1) {
                        throw new BadRequestException("005 Hearing Order is not unique");
                    }
                }
            }
        });
    }

    private int getOrderOccurrences(List<LinkHearingDetails> hearingDetails, int value) {
        List<Integer> list = new ArrayList<>();
        hearingDetails.forEach(lo -> list.add(lo.getHearingOrder()));
        int occurrences = Collections.frequency(list, value);
        return occurrences;
    }

    private int getIdOccurrences(List<LinkHearingDetails> hearingDetails, String value) {
        List<String> list = new ArrayList<>();
        hearingDetails.forEach(lo -> list.add(lo.getHearingId()));
        int occurrences = Collections.frequency(list, value);
        return occurrences;
    }

    private LocalDate filterHearingResponses(HearingEntity hearingEntity) {
        String version = hearingEntity.getLatestRequestVersion().toString();
        Optional<HearingResponseEntity> hearingResponse = hearingEntity
            .getHearingResponses().stream().filter(hearingResponseEntity ->
                                                       hearingResponseEntity.getResponseVersion().equals(version))
            .collect(Collectors.toList()).stream()
            .max(Comparator.comparing(hearingResponseEntity -> hearingResponseEntity.getRequestTimeStamp()));

        return getLowestDate(hearingResponse.orElseThrow(() -> new BadRequestException("bad request")));
    }

    private LocalDate getLowestDate(HearingResponseEntity hearingResponse) {
        Optional<HearingDayDetailsEntity> hearingDayDetails = hearingResponse.getHearingDayDetails()
            .stream().min(Comparator.comparing(hearingDayDetailsEntity -> hearingDayDetailsEntity.getStartDateTime()));

        return hearingDayDetails
            .orElseThrow(() -> new BadRequestException("bad request")).getStartDateTime().toLocalDate();
    }
}
