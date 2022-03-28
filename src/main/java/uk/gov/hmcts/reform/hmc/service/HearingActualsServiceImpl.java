package uk.gov.hmcts.reform.hmc.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.ActualHearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.helper.HearingActualsMapper;
import uk.gov.hmcts.reform.hmc.model.ActualHearingDay;
import uk.gov.hmcts.reform.hmc.model.HearingActual;
import uk.gov.hmcts.reform.hmc.repository.ActualHearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_HEARING_DAYS_INVALID;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_INVALID_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_NON_UNIQUE_HEARING_DAYS;

@Service
public class HearingActualsServiceImpl implements HearingActualsService {
    private final HearingHelper hearingHelper;
    private final HearingRepository hearingRepository;
    private final ActualHearingRepository actualHearingRepository;
    private final HearingActualsMapper hearingActualsMapper;

    private static final List<String> ALLOWED_ACTUALS_STATUSES = List.of("LISTED",
                                                                         "UPDATE_REQUESTED",
                                                                         "UPDATE_SUBMITTED");
    public static final List<String> HEARING_RESULTS_THAT_NEED_REASON_TYPE = List.of("ADJOURNED", "CANCELLED");

    @Autowired
    public HearingActualsServiceImpl(HearingRepository hearingRepository,
                                     ActualHearingRepository actualHearingRepository,
                                     HearingHelper hearingHelper,
                                     HearingActualsMapper hearingActualsMapper) {
        this.hearingRepository = hearingRepository;
        this.actualHearingRepository = actualHearingRepository;
        this.hearingHelper = hearingHelper;
        this.hearingActualsMapper = hearingActualsMapper;
    }

    @Transactional
    public void updateHearingActuals(Long hearingId, HearingActual request) {
        hearingHelper.isValidFormat(hearingId.toString());
        HearingEntity hearing = getHearing(hearingId);
        String hearingStatus = hearing.getStatus();
        validateHearingStatusForActuals(hearingStatus);
        validateRequestPayload(request, hearing);

        HearingResponseEntity latestVersionHearingResponse = hearingHelper.getLatestVersionHearingResponse(hearing);
        deleteHearingActualsForLatestResponse(latestVersionHearingResponse);
        insertNewHearingActuals(latestVersionHearingResponse, request);
    }

    private void deleteHearingActualsForLatestResponse(HearingResponseEntity latestVersionHearingResponse) {
        Optional<ActualHearingEntity> actualHearingEntityOpt = actualHearingRepository
            .findByHearingResponse(latestVersionHearingResponse);
        if (actualHearingEntityOpt.isPresent()) {
            ActualHearingEntity actualHearing = actualHearingEntityOpt.get();
            actualHearingRepository.deleteById(actualHearing.getActualHearingId());
        }
    }

    private void insertNewHearingActuals(HearingResponseEntity latestVersionHearingResponse, HearingActual request) {
        ActualHearingEntity actualHearing = hearingActualsMapper.toActualHearingEntity(request);
        actualHearing.setHearingResponse(latestVersionHearingResponse);
        actualHearingRepository.save(actualHearing);
    }

    private void validateRequestPayload(HearingActual request, HearingEntity hearing) {
        validateHearingActualDaysNotInTheFuture(request);
        validateDuplicateHearingActualDays(request);
        validateHearingActualDaysNotBeforeFirstHearingDate(request, hearing);
        validateHearingResult(request);
    }

    private void validateHearingResult(HearingActual request) {
        if (HEARING_RESULTS_THAT_NEED_REASON_TYPE.contains(request.getHearingOutcome().getHearingResult().toUpperCase())
            && StringUtils.isBlank(request.getHearingOutcome().getHearingResultReasonType())) {
            throw new BadRequestException(null);
        }
    }

    private void validateHearingActualDaysNotBeforeFirstHearingDate(HearingActual request, HearingEntity hearing) {
        LocalDateTime startDate = hearingHelper.getLowestStartDateOfMostRecentHearingResponse(hearing);
        request.getActualHearingDays().forEach(actualHearingDay -> {
            if (actualHearingDay.getHearingDate().isBefore(startDate.toLocalDate())) {
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
