package uk.gov.hmcts.reform.hmc.service;

import com.microsoft.applicationinsights.core.dependencies.apachecommons.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.PartiesNotifiedBadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.PartiesNotifiedNotFoundException;
import uk.gov.hmcts.reform.hmc.model.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.hmc.model.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.hmc.model.partiesnotified.PartiesNotified;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingResponseRepository;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_ID_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;

@Service
@Component
@Slf4j
public class PartiesNotifiedServiceImpl  extends HearingIdValidator  implements PartiesNotifiedService {

    private final HearingResponseRepository hearingResponseRepository;

    @Autowired
    public PartiesNotifiedServiceImpl(HearingRepository hearingRepository,
                                      HearingResponseRepository hearingResponseRepository) {
        super(hearingRepository);
        this.hearingResponseRepository = hearingResponseRepository;
    }

    @Override
    public void getPartiesNotified(Long hearingId, int responseVersion, PartiesNotified partiesNotified) {
        validateHearingId(hearingId);
        if (!hearingRepository.existsById(hearingId)) {
            throw new PartiesNotifiedNotFoundException("001 No such id: %s", hearingId);
        } else {
            HearingResponseEntity hearingResponseEntity = hearingResponseRepository.getHearingResponse(hearingId);
            if (hearingResponseEntity.getResponseVersion() != responseVersion) {
                throw new PartiesNotifiedNotFoundException("002 No such response version", null);
            } else if (Integer.valueOf(hearingResponseEntity.getResponseVersion()).equals(responseVersion)
                && hearingResponseEntity.getPartiesNotifiedDateTime() != null) {
                throw new PartiesNotifiedBadRequestException("003 Already set", null);
            } else {
                hearingResponseEntity.setPartiesNotifiedDateTime(LocalDateTime.now());
                hearingResponseEntity.setServiceData(partiesNotified.getServiceData());
                hearingResponseRepository.save(hearingResponseEntity);
            }
        }


    }

    /**
     * get parties notified.
     * @param hearingId hearing id
     * @return  list partiesNotified
     */
    @Override
    public PartiesNotifiedResponses getPartiesNotified(Long hearingId) {
        validateHearingId(hearingId);
        List<HearingResponseEntity> entities = hearingResponseRepository.getPartiesNotified(hearingId);
        if (entities.isEmpty()) {
            log.info("No partiesNotified found for hearingId {}", hearingId);
        } else {
            HearingResponseEntity entity = entities.get(0);
            log.info("hearingId {}, partiesNotified {}",  hearingId,
                     entity.getHearingResponseId());
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
        responses.setHearingID(hearingId);
        return responses;
    }


}
