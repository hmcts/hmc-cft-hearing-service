package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.exceptions.PartiesNotifiedBadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.PartiesNotifiedNotFoundException;
import uk.gov.hmcts.reform.hmc.model.partiesnotified.PartiesNotified;
import uk.gov.hmcts.reform.hmc.model.partiesnotified.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.hmc.model.partiesnotified.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.hmc.repository.HearingResponseRepository;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PARTIES_NOTIFIED_ALREADY_SET;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PARTIES_NOTIFIED_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.PARTIES_NOTIFIED_RESPONSE_VERSION_MISMATCH;

@Service
@Component
@Slf4j
public class PartiesNotifiedServiceImpl implements PartiesNotifiedService {

    private final HearingResponseRepository hearingResponseRepository;
    private HearingIdValidator hearingIdValidator;

    @Autowired
    public PartiesNotifiedServiceImpl(HearingResponseRepository hearingResponseRepository,
                                      HearingIdValidator hearingIdValidator) {
        this.hearingResponseRepository = hearingResponseRepository;
        this.hearingIdValidator = hearingIdValidator;
    }

    @Override
    public void getPartiesNotified(Long hearingId, int responseVersion, PartiesNotified partiesNotified) {
        hearingIdValidator.validateHearingId(hearingId, PARTIES_NOTIFIED_ID_NOT_FOUND);
        HearingResponseEntity hearingResponseEntity = hearingResponseRepository.getHearingResponse(hearingId);
        if (hearingResponseEntity.getResponseVersion() != responseVersion) {
            throw new PartiesNotifiedNotFoundException(PARTIES_NOTIFIED_RESPONSE_VERSION_MISMATCH);
        } else if (hearingResponseEntity.getResponseVersion().equals(responseVersion)
            && hearingResponseEntity.getPartiesNotifiedDateTime() != null) {
            throw new PartiesNotifiedBadRequestException(PARTIES_NOTIFIED_ALREADY_SET);
        } else {
            hearingResponseEntity.setPartiesNotifiedDateTime(LocalDateTime.now());
            hearingResponseEntity.setServiceData(partiesNotified.getServiceData());
            hearingResponseRepository.save(hearingResponseEntity);
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
            response.setResponseVersion(e.getResponseVersion());
            response.setResponseReceivedDateTime(e.getRequestTimeStamp());
            response.setRequestVersion(e.getRequestVersion());
            response.setPartiesNotified(e.getPartiesNotifiedDateTime());
            response.setServiceData(e.getServiceData());
            partiesNotified.add(response);
        });
        PartiesNotifiedResponses responses = new PartiesNotifiedResponses();
        responses.setResponses(partiesNotified);
        responses.setHearingID(hearingId.toString());
        return responses;
    }


}
