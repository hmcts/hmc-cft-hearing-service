package uk.gov.hmcts.reform.hmc.validator;

import com.microsoft.applicationinsights.core.dependencies.apachecommons.lang3.StringUtils;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;
import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_ID_VALID_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_PUT_HEARING_STATUS;

public class HearingIdValidator {

    protected final HearingRepository hearingRepository;

    public HearingIdValidator(HearingRepository hearingRepository) {
        this.hearingRepository = hearingRepository;
    }

    /**
     * validate Hearing id.
     *
     * @param hearingId hearing id
     */
    protected void validateHearingId(Long hearingId, String errorMessage) {
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
    protected void isValidFormat(String hearingIdStr) {
        if (hearingIdStr.length() != HEARING_ID_VALID_LENGTH || !StringUtils.isNumeric(hearingIdStr)
                || hearingIdStr.charAt(0) != '2') {
            throw new BadRequestException(INVALID_HEARING_ID_DETAILS);
        }
    }

    public LocalDateTime getLowestStartDateOfMostRecentHearingResponse(HearingEntity hearing) {

        List<HearingResponseEntity> latestVersionHearingResponses = getLatestVersionHearingResponses(hearing);

        Optional<HearingResponseEntity> mostRecentLatestVersionHearingResponseOpt = latestVersionHearingResponses
            .stream().max(Comparator.comparing(HearingResponseEntity::getRequestTimeStamp));
        if (mostRecentLatestVersionHearingResponseOpt.isEmpty()) {
            throw new BadRequestException(INVALID_PUT_HEARING_STATUS);
        }

        Optional<HearingDayDetailsEntity> hearingDayDetailsOpt = mostRecentLatestVersionHearingResponseOpt.get()
            .getHearingDayDetails().stream()
            .min(Comparator.comparing(HearingDayDetailsEntity::getStartDateTime));
        if (hearingDayDetailsOpt.isEmpty()) {
            throw new BadRequestException(INVALID_PUT_HEARING_STATUS);
        }
        return hearingDayDetailsOpt.get().getStartDateTime();
    }

    private List<HearingResponseEntity> getLatestVersionHearingResponses(HearingEntity hearing) {
        Optional<Map.Entry<String, List<HearingResponseEntity>>> max = hearing.getHearingResponses().stream()
            .collect(groupingBy(HearingResponseEntity::getRequestVersion))
            .entrySet()
            .stream()
            .max(Map.Entry.comparingByKey());

        return max.isPresent() ? max.get().getValue() : List.of();
    }
}
