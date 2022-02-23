package uk.gov.hmcts.reform.hmc.service;

import com.microsoft.applicationinsights.core.dependencies.apachecommons.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingResponseRepository;

import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * get parties notified.
     * @param hearingId hearing id
     * @return  list dateTimes
     */
    @Override
    public List<LocalDateTime> getPartiesNotified(Long hearingId) {
        validateHearingId(hearingId);
        List<LocalDateTime> partiesNotifiedDateTimeList = hearingResponseRepository.getHearingResponses(hearingId);
        if (partiesNotifiedDateTimeList.isEmpty()) {
            log.info("No partiesNotifiedDateTimes found for hearingId {}", hearingId);
        } else {
            log.info("hearingId {}, partiesNotifiedDateTime {}",  hearingId,
                    partiesNotifiedDateTimeList.get(0));
        }
        return partiesNotifiedDateTimeList;
    }

    /**
     * validate Hearing id.
     * @param hearingId hearing id
     */
    private void validateHearingId(Long hearingId) {
        if (hearingId == null) {
            throw new BadRequestException(INVALID_HEARING_ID_DETAILS);
        } else {
            String hearingIdStr = String.valueOf(hearingId);
            isValidFormat(hearingIdStr);
            if (!hearingRepository.existsById(hearingId)) {
                throw new HearingNotFoundException(hearingId);
            }
        }
    }

    /**
     * validate Hearing id format.
     * @param hearingIdStr hearing id string
     */
    private void isValidFormat(String hearingIdStr) {
        if (hearingIdStr.length() != HEARING_ID_MAX_LENGTH || !StringUtils.isNumeric(hearingIdStr)
                || hearingIdStr.charAt(0) != '2') {
            throw new BadRequestException(INVALID_HEARING_ID_DETAILS);
        }
    }
}
