package uk.gov.hmcts.reform.hmc.validator;

import com.microsoft.applicationinsights.core.dependencies.apachecommons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.ActualHearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.repository.ActualHearingDayRepository;
import uk.gov.hmcts.reform.hmc.repository.ActualHearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;

import java.time.LocalDate;
import java.util.Optional;

import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_ID_VALID_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_NO_HEARING_RESPONSE_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;

@Component
public class HearingIdValidator {

    public final HearingRepository hearingRepository;
    public final ActualHearingRepository actualHearingRepository;
    public final ActualHearingDayRepository actualHearingDayRepository;

    @Autowired
    public HearingIdValidator(HearingRepository hearingRepository,
                              ActualHearingRepository actualHearingRepository,
                              ActualHearingDayRepository actualHearingDayRepository) {
        this.hearingRepository = hearingRepository;
        this.actualHearingRepository = actualHearingRepository;
        this.actualHearingDayRepository = actualHearingDayRepository;
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
        if (hearingIdStr.length() != HEARING_ID_VALID_LENGTH || !StringUtils.isNumeric(hearingIdStr)
                || hearingIdStr.charAt(0) != '2') {
            throw new BadRequestException(INVALID_HEARING_ID_DETAILS);
        }
    }

    public LocalDate getLowestStartDateOfMostRecentHearingResponse(HearingEntity hearingEntity) {
        Optional<HearingResponseEntity> hearingResponse = hearingEntity.getHearingResponseForLatestRequest();

        return getLowestDate(hearingResponse.orElseThrow(() -> new BadRequestException(
            String.format(HEARING_ACTUALS_NO_HEARING_RESPONSE_FOUND, hearingEntity.getId()))));
    }

    public LocalDate getLowestDate(HearingResponseEntity hearingResponse) {
        Optional<HearingDayDetailsEntity> hearingDayDetails = hearingResponse.getEarliestHearingDayDetails();

        return hearingDayDetails
            .orElseThrow(() -> new BadRequestException("bad request")).getStartDateTime().toLocalDate();
    }

    public String getStatus(Long hearingId) {
        return hearingRepository.getStatus(hearingId);
    }

    public Optional<ActualHearingEntity> getActualHearing(Long hearingId) {
        Optional<HearingResponseEntity> hearingResponseEntity = hearingRepository.findById(hearingId)
                        .orElseThrow(() -> new HearingNotFoundException(hearingId, HEARING_ID_NOT_FOUND))
            .getHearingResponseForLatestRequest();
        if (hearingResponseEntity.isPresent()) {
            return actualHearingRepository.findByHearingResponse(hearingResponseEntity.get());
        }
        return Optional.empty();
    }

}
