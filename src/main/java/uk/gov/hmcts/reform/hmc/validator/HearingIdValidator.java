package uk.gov.hmcts.reform.hmc.validator;

import com.microsoft.applicationinsights.core.dependencies.apachecommons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_ID_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;

@Component
public class HearingIdValidator {

    private final HearingRepository hearingRepository;

    @Autowired
    public HearingIdValidator(HearingRepository hearingRepository) {
        this.hearingRepository = hearingRepository;
    }

    /**
     * validate Hearing id.
     *
     * @param hearingId hearing id
     */
    public void validateHearingId(Long hearingId, String errorMessage) {
        if (hearingId == null) {
            throw new BadRequestException(INVALID_HEARING_ID_DETAILS);
        } else {
            String hearingIdStr = String.valueOf(hearingId);
            isValidFormat(hearingIdStr);
            if (!hearingRepository.existsById(hearingId)) {
                throw new HearingNotFoundException(hearingId, errorMessage);
            }
        }
    }

    /**
     * validate Hearing id format.
     * @param hearingIdStr hearing id string
     */
    public void isValidFormat(String hearingIdStr) {
        if (hearingIdStr.length() != HEARING_ID_MAX_LENGTH || !StringUtils.isNumeric(hearingIdStr)
                || hearingIdStr.charAt(0) != '2') {
            throw new BadRequestException(INVALID_HEARING_ID_DETAILS);
        }
    }

    public LocalDate filterHearingResponses(HearingEntity hearingEntity) {
        Optional<HearingResponseEntity> hearingResponse = getHearingResponse(hearingEntity);

        return getLowestDate(hearingResponse.orElseThrow(() -> new BadRequestException("bad request")));
    }

    public LocalDate getLowestDate(HearingResponseEntity hearingResponse) {
        Optional<HearingDayDetailsEntity> hearingDayDetails = hearingResponse.getHearingDayDetails()
            .stream().min(Comparator.comparing(hearingDayDetailsEntity -> hearingDayDetailsEntity.getStartDateTime()));

        return hearingDayDetails
            .orElseThrow(() -> new BadRequestException("bad request")).getStartDateTime().toLocalDate();
    }

    public Optional<HearingResponseEntity> getHearingResponse(HearingEntity hearingEntity) {
        Integer version = hearingEntity.getLatestRequestVersion();
        Optional<HearingResponseEntity> hearingResponse = hearingEntity
            .getHearingResponses().stream().filter(hearingResponseEntity ->
                                                       hearingResponseEntity.getResponseVersion().equals(version))
            .collect(Collectors.toList()).stream()
            .max(Comparator.comparing(hearingResponseEntity -> hearingResponseEntity.getRequestTimeStamp()));

        return hearingResponse;
    }

    /**
     * get Hearing.
     *
     * @param hearingId hearing Id
     * @return hearing hearing
     */
    public Optional<HearingEntity> getHearing(Long hearingId) {
        return hearingRepository.findById(hearingId);
    }
}
