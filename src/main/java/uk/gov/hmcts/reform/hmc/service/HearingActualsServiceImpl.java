package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.ActualHearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.helper.GetHearingActualsResponseMapper;
import uk.gov.hmcts.reform.hmc.helper.HearingActualsMapper;
import uk.gov.hmcts.reform.hmc.model.ActualHearingDay;
import uk.gov.hmcts.reform.hmc.model.HearingActual;
import uk.gov.hmcts.reform.hmc.model.hearingactuals.HearingActualResponse;
import uk.gov.hmcts.reform.hmc.repository.ActualHearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingResponseRepository;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HA_OUTCOME_RESULT_NOT_EMPTY;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_HEARING_DAYS_INVALID;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_INVALID_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_MISSING_RESULT_TYPE;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_NON_UNIQUE_HEARING_DAYS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_NO_HEARING_RESPONSE_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_NOT_FOUND;

@Service
@Slf4j
public class HearingActualsServiceImpl implements HearingActualsService {
    private final HearingRepository hearingRepository;
    private final HearingResponseRepository hearingResponseRepository;
    private final ActualHearingRepository actualHearingRepository;
    private final HearingActualsMapper hearingActualsMapper;
    private final GetHearingActualsResponseMapper getHearingActualsResponseMapper;
    private final HearingIdValidator hearingIdValidator;

    private static final List<String> ALLOWED_ACTUALS_STATUSES = List.of("LISTED",
                                                                         "UPDATE_REQUESTED",
                                                                         "UPDATE_SUBMITTED");
    public static final List<String> HEARING_RESULTS_REASONS = List.of("ADJOURNED", "CANCELLED", "COMPLETED");
    public static final List<String> HEARING_RESULTS_THAT_NEED_REASON_TYPE = List.of("ADJOURNED", "CANCELLED");

    @Autowired
    public HearingActualsServiceImpl(HearingRepository hearingRepository,
                                     HearingResponseRepository hearingResponseRepository,
                                     ActualHearingRepository actualHearingRepository,
                                     GetHearingActualsResponseMapper getHearingActualsResponseMapper,
                                     HearingActualsMapper hearingActualsMapper,
                                     HearingIdValidator hearingIdValidator) {
        this.hearingRepository = hearingRepository;
        this.hearingResponseRepository = hearingResponseRepository;
        this.actualHearingRepository = actualHearingRepository;
        this.getHearingActualsResponseMapper = getHearingActualsResponseMapper;
        this.hearingIdValidator = hearingIdValidator;
        this.hearingActualsMapper = hearingActualsMapper;
    }

    @Override
    public ResponseEntity<HearingActualResponse> getHearingActuals(Long hearingId) {
        hearingIdValidator.validateHearingId(hearingId,HEARING_ID_NOT_FOUND);
        val hearingEntity = hearingRepository.findById(hearingId);
        if (hearingEntity.isPresent()) {
            return ResponseEntity.ok(getHearingActualsResponseMapper.toHearingActualResponse(hearingEntity.get()));
        } else {
            throw new HearingNotFoundException(hearingId,HEARING_ID_NOT_FOUND);
        }
    }

    @Transactional
    public void updateHearingActuals(Long hearingId, HearingActual request) {
        hearingIdValidator.isValidFormat(hearingId.toString());
        HearingEntity hearing = getHearing(hearingId);
        String hearingStatus = hearing.getStatus();
        validateHearingStatusForActuals(hearingStatus);
        validateRequestPayload(request, hearing);

        Optional<HearingResponseEntity> latestVersionHearingResponse = hearing.getHearingResponseForLatestRequest();
        if (latestVersionHearingResponse.isEmpty()) {
            throw new BadRequestException(String.format(HEARING_ACTUALS_NO_HEARING_RESPONSE_FOUND, hearingId));
        }
        upsertNewHearingActuals(latestVersionHearingResponse.get(), request);
    }

    private void upsertNewHearingActuals(HearingResponseEntity latestVersionHearingResponse, HearingActual request) {
        ActualHearingEntity actualHearing = hearingActualsMapper
            .toActualHearingEntity(request);
        latestVersionHearingResponse.setActualHearingEntity(actualHearing);
        actualHearing.setHearingResponse(latestVersionHearingResponse);
        actualHearingRepository.save(actualHearing);
        hearingResponseRepository.save(latestVersionHearingResponse);
    }

    private void validateRequestPayload(HearingActual request, HearingEntity hearing) {
        validateHearingActualDaysNotInTheFuture(request);
        validateDuplicateHearingActualDays(request);
        validateHearingActualDaysNotBeforeFirstHearingDate(request, hearing);
        validateHearingResult(request);
    }

    private void validateHearingResult(HearingActual request) {
        if (!HEARING_RESULTS_REASONS.contains(request.getHearingOutcome().getHearingResult().toUpperCase())) {
            throw new BadRequestException(HA_OUTCOME_RESULT_NOT_EMPTY);
        }
        if (HEARING_RESULTS_THAT_NEED_REASON_TYPE.contains(request.getHearingOutcome().getHearingResult().toUpperCase())
            && StringUtils.isBlank(request.getHearingOutcome().getHearingResultReasonType())) {
            throw new BadRequestException(String.format(HEARING_ACTUALS_MISSING_RESULT_TYPE,
                                                        request.getHearingOutcome().getHearingResult()));
        }
    }

    private void validateHearingActualDaysNotBeforeFirstHearingDate(HearingActual request, HearingEntity hearing) {
        LocalDate minStartDate = hearingIdValidator.getLowestStartDateOfMostRecentHearingResponse(hearing);
        request.getActualHearingDays().forEach(actualHearingDay -> {
            if (actualHearingDay.getHearingDate().isBefore(minStartDate)) {
                throw new BadRequestException(HEARING_ACTUALS_HEARING_DAYS_INVALID);
            }
        });
    }

    private void validateHearingActualDaysNotInTheFuture(HearingActual request) {
        request.getActualHearingDays().forEach(hearingDay -> {
            if (hearingDay.getHearingDate().isAfter(LocalDate.now())) {
                throw new BadRequestException(HEARING_ACTUALS_HEARING_DAYS_INVALID);
            }
        });
    }

    private void validateDuplicateHearingActualDays(HearingActual request) {
        Set<LocalDate> hearingDays = request.getActualHearingDays()
            .stream()
            .map(ActualHearingDay::getHearingDate)
            .collect(Collectors.toSet());
        if (hearingDays.size() != request.getActualHearingDays().size()) {
            throw new BadRequestException(HEARING_ACTUALS_NON_UNIQUE_HEARING_DAYS);
        }
    }

    private void validateHearingStatusForActuals(String hearingStatus) {
        if (ALLOWED_ACTUALS_STATUSES.stream().noneMatch(e -> e.equals(hearingStatus))) {
            throw new BadRequestException(String.format(HEARING_ACTUALS_INVALID_STATUS, hearingStatus));
        }
    }

    private HearingEntity getHearing(Long hearingId) {
        Optional<HearingEntity> hearingEntityOptional = hearingRepository.findById(hearingId);

        if (hearingEntityOptional.isEmpty()) {
            throw new HearingNotFoundException(hearingId, HEARING_ACTUALS_ID_NOT_FOUND);
        }
        return hearingEntityOptional.get();
    }
}
