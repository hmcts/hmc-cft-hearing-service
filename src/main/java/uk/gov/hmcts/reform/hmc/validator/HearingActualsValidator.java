package uk.gov.hmcts.reform.hmc.validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.ActualHearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.model.ActualHearingDay;
import uk.gov.hmcts.reform.hmc.model.HearingActualsOutcome;
import uk.gov.hmcts.reform.hmc.model.HearingResultType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HA_OUTCOME_RESULT_NOT_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_HEARING_DAYS_INVALID;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_INVALID_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_MISSING_RESULT_TYPE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_NON_UNIQUE_HEARING_DAYS;

@Component
public class HearingActualsValidator {
    private final HearingIdValidator hearingIdValidator;
    private static final List<String> ALLOWED_ACTUALS_STATUSES = List.of("LISTED",
            "UPDATE_REQUESTED",
            "UPDATE_SUBMITTED");
    public static final List<String> HEARING_RESULTS_REASONS = List.of("ADJOURNED", "CANCELLED", "COMPLETED");
    public static final List<String> HEARING_RESULTS_THAT_NEED_REASON_TYPE = List.of("ADJOURNED", "CANCELLED");

    @Autowired
    public HearingActualsValidator(HearingIdValidator hearingIdValidator) {
        this.hearingIdValidator = hearingIdValidator;
    }

    public void validateHearingResult(HearingActualsOutcome hearingOutcome) {
        if (null != hearingOutcome && null != hearingOutcome.getHearingResult()) {
            validateHearingResult(hearingOutcome.getHearingResult());
            validateHearingResult(hearingOutcome.getHearingResult(), hearingOutcome.getHearingResultReasonType());
        }
    }

    public void validateHearingResult(String hearingResult) {
        if (null == hearingResult || !HEARING_RESULTS_REASONS.contains(hearingResult.toUpperCase())) {
            throw new BadRequestException(HA_OUTCOME_RESULT_NOT_EMPTY);
        }
    }

    public void validateHearingResult(HearingResultType hearingResultType) {
        if (null == hearingResultType) {
            throw new BadRequestException(ValidationError.HA_OUTCOME_RESULT_NOT_EMPTY);
        }
    }

    public void validateHearingResult(String hearingResult, String hearingResultReasonType) {
        if (null != hearingResult && HEARING_RESULTS_THAT_NEED_REASON_TYPE.contains(
                hearingResult.toUpperCase())
                && StringUtils.isBlank(hearingResultReasonType)) {
            throw new BadRequestException(String.format(HEARING_ACTUALS_MISSING_RESULT_TYPE, hearingResult));
        }
    }

    public void validateHearingResult(HearingResultType hearingResultType, String hearingResultReasonType) {
        if (null != hearingResultType && HEARING_RESULTS_THAT_NEED_REASON_TYPE.contains(
                hearingResultType.getLabel().toUpperCase())
                && StringUtils.isBlank(hearingResultReasonType)) {
            throw new BadRequestException(String.format(HEARING_ACTUALS_MISSING_RESULT_TYPE, hearingResultType));
        }
    }

    public void validateHearingActualDaysNotBeforeFirstHearingDate(List<ActualHearingDay> actualHearingDays,
                                                                   HearingEntity hearing) {
        LocalDate minStartDate = hearingIdValidator.getLowestStartDateOfMostRecentHearingResponse(hearing);
        actualHearingDays.forEach(actualHearingDay -> {
            if (actualHearingDay.getHearingDate().isBefore(minStartDate)) {
                throw new BadRequestException(HEARING_ACTUALS_HEARING_DAYS_INVALID);
            }
        });
    }

    public void validateHearingActualDaysNotInTheFuture(List<ActualHearingDay> actualHearingDays) {
        actualHearingDays.forEach(hearingDay -> {
            if (hearingDay.getHearingDate().isAfter(LocalDate.now())) {
                throw new BadRequestException(HEARING_ACTUALS_HEARING_DAYS_INVALID);
            }
        });
    }

    public void validateDuplicateHearingActualDays(List<ActualHearingDay> actualHearingDays) {
        Set<LocalDate> hearingDays = actualHearingDays
                .stream()
                .map(ActualHearingDay::getHearingDate)
                .collect(Collectors.toSet());
        if (hearingDays.size() != actualHearingDays.size()) {
            throw new BadRequestException(HEARING_ACTUALS_NON_UNIQUE_HEARING_DAYS);
        }
    }

    public void validateHearingStatusForActuals(String hearingStatus) {
        if (ALLOWED_ACTUALS_STATUSES.stream().noneMatch(e -> e.equals(hearingStatus))) {
            throw new BadRequestException(String.format(HEARING_ACTUALS_INVALID_STATUS, hearingStatus));
        }
    }

    public void validateHearingOutcomeInformation(Long hearingId) {
        Optional<ActualHearingEntity> entity = hearingIdValidator.getActualHearing(hearingId);
        if (entity.isEmpty()) {
            throw new BadRequestException(ValidationError.HEARING_ACTUALS_MISSING_HEARING_OUTCOME);
        }
        ActualHearingEntity actualHearingEntity = entity.get();
        validateActualHearingType(actualHearingEntity.getActualHearingType());
        validateActualHearingIsFinalFlag(actualHearingEntity.getActualHearingIsFinalFlag());
        validateHearingResult(actualHearingEntity.getHearingResultType());
        validateHearingResult(actualHearingEntity.getHearingResultType(),
                actualHearingEntity.getHearingResultReasonType());
        validateHearingResultReasonType(actualHearingEntity.getHearingResultReasonType());
        validateHearingResultDate(actualHearingEntity.getHearingResultDate());
    }

    public void validateActualHearingType(String actualHearingType) {
        if (StringUtils.isBlank(actualHearingType)) {
            throw new BadRequestException(ValidationError.HA_OUTCOME_TYPE_NOT_EMPTY);
        }
    }

    public void validateActualHearingIsFinalFlag(Boolean actualHearingIsFinalFlag) {
        if (null == actualHearingIsFinalFlag) {
            throw new BadRequestException(ValidationError.HA_OUTCOME_FINAL_FLAG_NOT_EMPTY);
        }
    }

    public void validateHearingResultReasonType(String hearingResultReasonType) {
        if (null != hearingResultReasonType && hearingResultReasonType.length() > 70) {
            throw new BadRequestException(ValidationError.HA_OUTCOME_REASON_TYPE_MAX_LENGTH);
        }
    }

    public void validateHearingResultDate(LocalDate hearingResultDate) {
        if (null == hearingResultDate) {
            throw new BadRequestException(ValidationError.HA_OUTCOME_REQUEST_DATE_NOT_EMPTY);
        }
    }

}
