package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.ActualHearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.domain.model.HearingStatusAuditContext;
import uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.helper.GetHearingActualsResponseMapper;
import uk.gov.hmcts.reform.hmc.helper.HearingActualsMapper;
import uk.gov.hmcts.reform.hmc.model.HearingActual;
import uk.gov.hmcts.reform.hmc.model.HearingActualsOutcome;
import uk.gov.hmcts.reform.hmc.model.hearingactuals.HearingActualResponse;
import uk.gov.hmcts.reform.hmc.repository.ActualHearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.service.common.ActualHearingAuditService;
import uk.gov.hmcts.reform.hmc.service.common.HearingStatusAuditService;
import uk.gov.hmcts.reform.hmc.validator.HearingActualsValidator;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;

import java.util.Optional;

import static uk.gov.hmcts.reform.hmc.constants.Constants.HMC;
import static uk.gov.hmcts.reform.hmc.constants.Constants.PUT_HEARING_ACTUALS_COMPLETION;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_NO_HEARING_RESPONSE_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_ACTUALS_POST_STATUS;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.NO_PREVIOUS_HEARING_ACTUALS_RECORDED;

@Service
@Slf4j
public class HearingActualsServiceImpl implements HearingActualsService {
    private final HearingRepository hearingRepository;
    private final ActualHearingRepository actualHearingRepository;
    private final HearingActualsMapper hearingActualsMapper;
    private final GetHearingActualsResponseMapper getHearingActualsResponseMapper;
    private final HearingIdValidator hearingIdValidator;
    private final HearingActualsValidator hearingActualsValidator;
    private final HearingStatusAuditService hearingStatusAuditService;
    private final ActualHearingAuditService actualHearingAuditService;
    private final HearingCompletionService hearingCompletionService;

    @Autowired
    public HearingActualsServiceImpl(HearingRepository hearingRepository,
                                     ActualHearingRepository actualHearingRepository,
                                     GetHearingActualsResponseMapper getHearingActualsResponseMapper,
                                     HearingActualsMapper hearingActualsMapper,
                                     HearingIdValidator hearingIdValidator,
                                     HearingActualsValidator hearingActualsValidator,
                                     HearingStatusAuditService hearingStatusAuditService,
                                     ActualHearingAuditService actualHearingAuditService,
                                     HearingCompletionService hearingCompletionService) {
        this.hearingRepository = hearingRepository;
        this.actualHearingRepository = actualHearingRepository;
        this.getHearingActualsResponseMapper = getHearingActualsResponseMapper;
        this.hearingIdValidator = hearingIdValidator;
        this.hearingActualsMapper = hearingActualsMapper;
        this.hearingActualsValidator = hearingActualsValidator;
        this.hearingStatusAuditService = hearingStatusAuditService;
        this.actualHearingAuditService = actualHearingAuditService;
        this.hearingCompletionService = hearingCompletionService;
    }

    @Override
    public ResponseEntity<HearingActualResponse> getHearingActuals(Long hearingId) {
        hearingIdValidator.validateHearingId(hearingId,HEARING_ID_NOT_FOUND);
        var hearingEntity = hearingRepository.findById(hearingId);
        if (hearingEntity.isPresent()) {
            return ResponseEntity.ok(getHearingActualsResponseMapper.toHearingActualResponse(hearingEntity.get()));
        } else {
            throw new HearingNotFoundException(hearingId,HEARING_ID_NOT_FOUND);
        }
    }

    @Transactional
    public void updateHearingActuals(Long hearingId, String clientS2SToken, HearingActual request) {
        hearingIdValidator.isValidFormat(hearingId.toString());
        HearingEntity hearing = getHearing(hearingId);
        HearingStatus hearingStatus = HearingStatus.valueOf(hearing.getStatus());

        hearingActualsValidator.validateHearingStatusForActuals(hearingStatus.name());
        validateRequestPayload(request, hearing);

        HearingResponseEntity latestHearingResponse = getHearingResponseEntity(hearingId, hearing);

        if (!HearingStatus.isFinalStatus(hearingStatus)) {
            upsertNewHearingActuals(latestHearingResponse, request, clientS2SToken, hearing);
            return;
        }

        completeHearingForFinalStatus(hearingId, clientS2SToken, request, latestHearingResponse,
                                      hearing, hearingStatus);
    }

    private void completeHearingForFinalStatus(Long hearingId, String clientS2SToken, HearingActual request,
                                      HearingResponseEntity latestHearingResponse, HearingEntity hearing,
                                      HearingStatus hearingStatus) {
        ActualHearingEntity actualHearingEntity = getActualHearingEntity(latestHearingResponse);

        actualHearingAuditService.saveActualHearingAuditDetails(request, actualHearingEntity);

        upsertNewHearingActuals(latestHearingResponse, request, clientS2SToken, hearing);

        String hearingResult = validateAndGetHearingResult(request.getHearingOutcome(), hearingStatus);

        hearingActualsValidator.validateActualHearingEntity(actualHearingEntity);
        HearingEntity hearingEntity = updateStatus(hearingId, hearingResult);
        hearingCompletionService.completeHearing(hearingEntity, clientS2SToken, hearing.getLatestRequestVersion());
    }

    private static String validateAndGetHearingResult(HearingActualsOutcome hearingOutcome,
                                                      HearingStatus hearingStatus) {
        if (hearingOutcome == null || hearingOutcome.getHearingResult() == null) {
            throw new BadRequestException(INVALID_ACTUALS_POST_STATUS);
        }

        String hearingResult = hearingOutcome.getHearingResult();
        HearingStatus hearingResultStatus = HearingStatus.valueOf(hearingResult);

        if (!HearingStatus.isFinalStatus(hearingStatus)
            && !HearingStatus.isFinalStatus(hearingResultStatus)) {
            throw new BadRequestException(INVALID_ACTUALS_POST_STATUS);
        }
        return hearingResult;
    }

    private HearingEntity updateStatus(Long hearingId, String hearingResult) {
        HearingEntity hearingEntity = hearingRepository.findById(hearingId)
            .orElseThrow(() -> new HearingNotFoundException(hearingId, HEARING_ID_NOT_FOUND));
        hearingEntity.setStatus(hearingResult);
        hearingRepository.save(hearingEntity);
        return hearingEntity;
    }

    private static ActualHearingEntity getActualHearingEntity(HearingResponseEntity latestVersionHearingResponse) {
        ActualHearingEntity actualHearingEntity = latestVersionHearingResponse.getActualHearingEntity();
        if (actualHearingEntity == null) {
            throw new BadRequestException(NO_PREVIOUS_HEARING_ACTUALS_RECORDED);
        }
        return actualHearingEntity;
    }

    private static HearingResponseEntity getHearingResponseEntity(Long hearingId, HearingEntity hearing) {
        Optional<HearingResponseEntity> latestVersionHearingResponse = hearing.getHearingResponseForLatestRequest();
        if (latestVersionHearingResponse.isEmpty()) {
            throw new BadRequestException(String.format(HEARING_ACTUALS_NO_HEARING_RESPONSE_FOUND, hearingId));
        }
        return latestVersionHearingResponse.get();
    }

    private void upsertNewHearingActuals(HearingResponseEntity latestVersionHearingResponse, HearingActual request,
                                         String clientS2SToken, HearingEntity hearingEntity) {
        ActualHearingEntity actualHearing = hearingActualsMapper
            .toActualHearingEntity(request);
        latestVersionHearingResponse.setActualHearingEntity(actualHearing);
        actualHearing.setHearingResponse(latestVersionHearingResponse);
        actualHearingRepository.save(actualHearing);
        HearingStatusAuditContext hearingStatusAuditContext =
            HearingStatusAuditContext.builder()
                .hearingEntity(hearingEntity)
                .hearingEvent(PUT_HEARING_ACTUALS_COMPLETION)
                .source(clientS2SToken)
                .target(HMC)
                .useCurrentTimestamp(false)
                .build();
        hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDateOrCurrentDate(hearingStatusAuditContext);
    }

    private void validateRequestPayload(HearingActual request, HearingEntity hearing) {
        hearingActualsValidator.validateHearingActualDaysNotInTheFuture(request);
        hearingActualsValidator.validateDuplicateHearingActualDays(request.getActualHearingDays());
        hearingActualsValidator.validateHearingActualDaysNotBeforeFirstHearingDate(request.getActualHearingDays(),
                hearing);
        hearingActualsValidator.validateHearingResult(request.getHearingOutcome());
    }

    private HearingEntity getHearing(Long hearingId) {
        Optional<HearingEntity> hearingEntityOptional = hearingRepository.findById(hearingId);

        if (hearingEntityOptional.isEmpty()) {
            throw new HearingNotFoundException(hearingId, HEARING_ACTUALS_ID_NOT_FOUND);
        }
        return hearingEntityOptional.get();
    }

}
