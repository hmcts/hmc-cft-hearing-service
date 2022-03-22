package uk.gov.hmcts.reform.hmc.validator;

import com.microsoft.applicationinsights.core.dependencies.apachecommons.lang3.StringUtils;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;

import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_ID_VALID_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;

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
}
