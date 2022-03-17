package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingDetailsAudit;
import uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType;
import uk.gov.hmcts.reform.hmc.domain.model.enums.PutHearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.HearingLinkGroupRequest;
import uk.gov.hmcts.reform.hmc.model.linkedhearinggroup.LinkHearingDetails;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedGroupDetailsRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingDetailsRepository;
import uk.gov.hmcts.reform.hmc.validator.LinkedHearingValidator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_LINKED_GROUP_REQUEST_ID_DETAILS;

@Service
@Component
@Slf4j
public class LinkedHearingGroupServiceImpl extends LinkedHearingValidator implements LinkedHearingGroupService {

    @Autowired
    public LinkedHearingGroupServiceImpl(HearingRepository hearingRepository,
                                         LinkedGroupDetailsRepository linkedGroupDetailsRepository,
                                         LinkedHearingDetailsRepository linkedHearingDetailsRepository) {
        super(hearingRepository, linkedGroupDetailsRepository, linkedHearingDetailsRepository);
    }

    @Override
    public void linkHearing(HearingLinkGroupRequest hearingLinkGroupRequest) {
        validateHearingLinkGroupRequest(hearingLinkGroupRequest, null);
    }

    @Override
    public void updateLinkHearing(String requestId, HearingLinkGroupRequest hearingLinkGroupRequest) {
        validateHearingLinkGroupRequestForUpdate(requestId, hearingLinkGroupRequest);
    }

    private void validateHearingLinkGroupRequestForUpdate(String requestId,
                                                          HearingLinkGroupRequest hearingLinkGroupRequest) {
        validateRequestId(requestId, INVALID_LINKED_GROUP_REQUEST_ID_DETAILS);
        validateHearingLinkGroupRequest(hearingLinkGroupRequest, requestId);
        List<LinkHearingDetails> linkedHearingDetailsListPayload = hearingLinkGroupRequest.getHearingsInGroup();
        validateLinkedHearingsForUpdate(requestId, linkedHearingDetailsListPayload);
    }

    private void validateHearingLinkGroupRequest(HearingLinkGroupRequest hearingLinkGroupRequest, String requestId) {
        hearingLinkGroupRequest.getHearingsInGroup().forEach(details -> {
            checkSufficientRequestIds(hearingLinkGroupRequest, details);
            log.debug("hearingId: {}", details.getHearingId());
            validateHearingId(Long.valueOf(details.getHearingId()), HEARING_ID_NOT_FOUND);
            Optional<HearingEntity> hearingEntity = getHearing(Long.valueOf(details.getHearingId()));

            if (hearingEntity.isPresent()) {
                checkHearingRequestAllowsLinking(hearingEntity);
                checkHearingRequestIsNotInAnotherGroup(details, requestId);
                checkValidStateForHearingRequest(hearingEntity, details);
                checkHearingOrderIsUnique(hearingLinkGroupRequest, details);
            }
        });
    }

    private void checkSufficientRequestIds(HearingLinkGroupRequest hearingLinkGroupRequest,
                                           LinkHearingDetails details) {
        int occurrences = getIdOccurrences(hearingLinkGroupRequest.getHearingsInGroup(), details.getHearingId());
        if (occurrences > 1) {
            throw new BadRequestException("001 Insufficient requestIds");
        }
    }

    private void checkHearingRequestAllowsLinking(Optional<HearingEntity> hearingEntity) {
        if (hearingEntity.isEmpty() || Boolean.FALSE.equals(hearingEntity.get().getIsLinkedFlag())) {
            throw new BadRequestException("002 hearing request isLinked is False");
        }
    }

    private void checkHearingRequestIsNotInAnotherGroup(LinkHearingDetails details,
                                                       String requestId) {
        LinkedHearingDetailsAudit linkedHearingDetails =
            linkedHearingDetailsRepository.getLinkedHearingDetailsByHearingId(
                    Long.parseLong(details.getHearingId()));
        if (null != linkedHearingDetails) {
            log.info("requestId:{}", requestId);
            log.info("linkedHearingDetails:{}", linkedHearingDetails);
            log.info("linkedHearingDetails.getLinkedGroup():{}", linkedHearingDetails.getLinkedGroup());
            if (null != requestId) {
                log.info("linkedGroupDetailsById:{}",
                        linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(requestId));
            }
            if ((null == requestId && linkedHearingDetails.getLinkedGroup() != null)
                    || (null != requestId && !linkedHearingDetails.getLinkedGroup()
                    .equals(linkedGroupDetailsRepository.getLinkedGroupDetailsByRequestId(requestId)))) {
                throw new BadRequestException("003 hearing request already in a group");
            }
        }
    }

    private void checkValidStateForHearingRequest(Optional<HearingEntity> hearingEntity,
                                           LinkHearingDetails details) {

        if (hearingEntity.isEmpty()) {
            log.info("hearingEntity is Empty");
        } else if (!PutHearingStatus.isValid(hearingEntity.get().getStatus())) {
            log.info("hearingEntity status is invalid {}", hearingEntity.get().getStatus());
        } else if (null == hearingEntity.get().getCaseHearingRequest()) {
            log.info("hearingEntity caseHearingRequest is null");
        } else if (null == hearingEntity.get().getCaseHearingRequest().getHearingWindowStartDateRange()) {
            log.info("hearingEntity caseHearingRequest hearing window start date range is null");
        } else if (hearingEntity.get().getCaseHearingRequest().getHearingWindowStartDateRange()
                .isBefore(LocalDate.now())) {
            log.info("hearingEntity caseHearingRequest hearing window start date range already started");
        }

        if (hearingEntity.isEmpty()
            || !PutHearingStatus.isValid(hearingEntity.get().getStatus())
            || (null == hearingEntity.get().getCaseHearingRequest()
                || null == hearingEntity.get().getCaseHearingRequest().getHearingWindowStartDateRange()
                ||  hearingEntity.get().getCaseHearingRequest().getHearingWindowStartDateRange()
                   .isBefore(LocalDate.now()))) {
            throw new BadRequestException("004 Invalid state for hearing request "
                + details.getHearingId());
        }
    }

    private void checkHearingOrderIsUnique(HearingLinkGroupRequest hearingLinkGroupRequest,
                                           LinkHearingDetails details) {
        if (LinkType.ORDERED.label.equals(hearingLinkGroupRequest.getGroupDetails().getGroupLinkType())) {
            int counter = getOrderOccurrences(
                    hearingLinkGroupRequest.getHearingsInGroup(),
                    details.getHearingOrder()
            );
            if (counter > 1) {
                throw new BadRequestException("005 Hearing Order is not unique");
            }
        }
    }

    private int getOrderOccurrences(List<LinkHearingDetails> hearingDetails, int value) {
        List<Integer> list = new ArrayList<>();
        hearingDetails.forEach(lo -> list.add(lo.getHearingOrder()));
        return Collections.frequency(list, value);
    }

    private int getIdOccurrences(List<LinkHearingDetails> hearingDetails, String value) {
        List<String> list = new ArrayList<>();
        hearingDetails.forEach(lo -> list.add(lo.getHearingId()));
        return Collections.frequency(list, value);
    }
}
