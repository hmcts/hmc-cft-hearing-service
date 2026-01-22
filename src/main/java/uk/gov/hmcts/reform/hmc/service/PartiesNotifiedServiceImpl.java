package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.domain.model.HearingStatusAuditContext;
import uk.gov.hmcts.reform.hmc.exceptions.PartiesNotifiedBadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.PartiesNotifiedNotFoundException;
import uk.gov.hmcts.reform.hmc.model.partiesnotified.PartiesNotified;
import uk.gov.hmcts.reform.hmc.model.partiesnotified.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.hmc.model.partiesnotified.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.hmc.repository.HearingResponseRepository;
import uk.gov.hmcts.reform.hmc.service.common.HearingStatusAuditService;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static uk.gov.hmcts.reform.hmc.constants.Constants.HMC;
import static uk.gov.hmcts.reform.hmc.constants.Constants.PUT_PARTIES_NOTIFIED;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PARTIES_NOTIFIED_ALREADY_SET;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PARTIES_NOTIFIED_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PARTIES_NOTIFIED_NO_SUCH_RESPONSE;

@Service
@Component
@Slf4j
public class PartiesNotifiedServiceImpl implements PartiesNotifiedService {

    private final HearingResponseRepository hearingResponseRepository;
    private final HearingIdValidator hearingIdValidator;
    private final HearingStatusAuditService hearingStatusAuditService;

    @Autowired
    public PartiesNotifiedServiceImpl(HearingResponseRepository hearingResponseRepository,
                                      HearingIdValidator hearingIdValidator,
                                      HearingStatusAuditService hearingStatusAuditService) {
        this.hearingResponseRepository = hearingResponseRepository;
        this.hearingIdValidator = hearingIdValidator;
        this.hearingStatusAuditService = hearingStatusAuditService;
    }

    @Override
    public void getPartiesNotified(Long hearingId, Integer requestVersion,
                                   LocalDateTime receivedDateTime, PartiesNotified partiesNotified,
                                   String clientS2SToken) {
        hearingIdValidator.validateHearingId(hearingId, PARTIES_NOTIFIED_ID_NOT_FOUND);
        HearingResponseEntity hearingResponseEntity =
                hearingResponseRepository.getHearingResponse(hearingId, requestVersion, receivedDateTime);
        if (null == hearingResponseEntity) {
            throw new PartiesNotifiedNotFoundException(PARTIES_NOTIFIED_NO_SUCH_RESPONSE);
        } else if (hearingResponseEntity.getPartiesNotifiedDateTime() != null) {
            throw new PartiesNotifiedBadRequestException(PARTIES_NOTIFIED_ALREADY_SET);
        } else {
            hearingResponseEntity.setPartiesNotifiedDateTime(LocalDateTime.now());
            hearingResponseEntity.setServiceData(partiesNotified.getServiceData());
            hearingResponseRepository.save(hearingResponseEntity);
            HearingEntity hearingEntity = hearingResponseEntity.getHearing();

            HearingStatusAuditContext hearingStatusAuditContext =
                HearingStatusAuditContext.builder()
                    .hearingEntity(hearingEntity)
                    .hearingEvent(PUT_PARTIES_NOTIFIED)
                    .source(clientS2SToken)
                    .target(HMC).build();
            hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDate(hearingStatusAuditContext);
        }
    }

    /**
     * get parties notified.
     *
     * @param hearingId hearing id
     * @return list partiesNotified
     */
    @Override
    public PartiesNotifiedResponses getPartiesNotified(Long hearingId) {
        hearingIdValidator.validateHearingId(hearingId, PARTIES_NOTIFIED_ID_NOT_FOUND);
        List<HearingResponseEntity> entities = hearingResponseRepository.getPartiesNotified(hearingId);
        if (entities.isEmpty()) {
            log.debug("No partiesNotified found for hearingId {}", hearingId);
        } else {
            HearingResponseEntity entity = entities.get(0);
            log.debug("hearingId {}, partiesNotified {}", hearingId,
                     entity.getHearingResponseId()
            );
        }
        List<PartiesNotifiedResponse> partiesNotified = new ArrayList<>();
        entities.forEach(e -> {
            PartiesNotifiedResponse response = new PartiesNotifiedResponse();
            response.setResponseReceivedDateTime(e.getRequestTimeStamp());
            response.setRequestVersion(e.getRequestVersion());
            response.setPartiesNotified(e.getPartiesNotifiedDateTime());
            response.setServiceData(e.getServiceData());
            partiesNotified.add(response);
        });
        PartiesNotifiedResponses responses = new PartiesNotifiedResponses();
        sortPartiesNotifiedResponse(partiesNotified);
        responses.setResponses(partiesNotified);
        responses.setHearingID(hearingId.toString());
        return responses;
    }

    private static void sortPartiesNotifiedResponse(List<PartiesNotifiedResponse> partiesNotified) {
        partiesNotified.sort(Comparator
                                 .comparing(PartiesNotifiedResponse::getRequestVersion).reversed()
                                 .thenComparing(Comparator.comparing(
                                     PartiesNotifiedResponse::getResponseReceivedDateTime).reversed()));
    }

}
