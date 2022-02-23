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
import uk.gov.hmcts.reform.hmc.model.partiesnotified.PartiesNotified;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingResponseRepository;

import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_ID_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;

@Service
@Component
@Slf4j
public class PartiesNotifiedServiceImpl implements PartiesNotifiedService {

    private final HearingRepository hearingRepository;
    private final HearingResponseRepository hearingResponseRepository;

    @Autowired
    public PartiesNotifiedServiceImpl(HearingRepository hearingRepository,
                                      HearingResponseRepository hearingResponseRepository) {
        this.hearingRepository = hearingRepository;
        this.hearingResponseRepository = hearingResponseRepository;
    }

    @Override
    public void getPartiesNotified(Long hearingId, int responseVersion, PartiesNotified partiesNotified) {
        isValidFormat(hearingId.toString());
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

    private void isValidFormat(String hearingIdStr) {
        if (hearingIdStr.length() != HEARING_ID_MAX_LENGTH || !StringUtils.isNumeric(hearingIdStr)
            || hearingIdStr.charAt(0) != '2') {
            throw new BadRequestException(INVALID_HEARING_ID_DETAILS);
        }
    }


}
