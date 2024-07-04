package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.ActualHearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.helper.GetHearingActualsResponseMapper;
import uk.gov.hmcts.reform.hmc.helper.HearingActualsMapper;
import uk.gov.hmcts.reform.hmc.model.HearingActual;
import uk.gov.hmcts.reform.hmc.model.hearingactuals.HearingActualResponse;
import uk.gov.hmcts.reform.hmc.repository.ActualHearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingResponseRepository;
import uk.gov.hmcts.reform.hmc.service.common.HearingStatusAuditService;
import uk.gov.hmcts.reform.hmc.validator.HearingActualsValidator;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;

import java.util.Optional;

import static uk.gov.hmcts.reform.hmc.constants.Constants.HMC;
import static uk.gov.hmcts.reform.hmc.constants.Constants.PUT_HEARING_ACTUALS_COMPLETION;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_ID_NOT_FOUND;
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
    private final HearingActualsValidator hearingActualsValidator;
    private final HearingStatusAuditService hearingStatusAuditService;
    private final SecurityUtils securityUtils;

    @Autowired
    public HearingActualsServiceImpl(HearingRepository hearingRepository,
                                     HearingResponseRepository hearingResponseRepository,
                                     ActualHearingRepository actualHearingRepository,
                                     GetHearingActualsResponseMapper getHearingActualsResponseMapper,
                                     HearingActualsMapper hearingActualsMapper,
                                     HearingIdValidator hearingIdValidator,
                                     HearingActualsValidator hearingActualsValidator,
                                     HearingStatusAuditService hearingStatusAuditService,
                                     SecurityUtils securityUtils) {
        this.hearingRepository = hearingRepository;
        this.hearingResponseRepository = hearingResponseRepository;
        this.actualHearingRepository = actualHearingRepository;
        this.getHearingActualsResponseMapper = getHearingActualsResponseMapper;
        this.hearingIdValidator = hearingIdValidator;
        this.hearingActualsMapper = hearingActualsMapper;
        this.hearingActualsValidator = hearingActualsValidator;
        this.hearingStatusAuditService = hearingStatusAuditService;
        this.securityUtils = securityUtils;
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
    public void updateHearingActuals(Long hearingId, String clientS2SToken, HearingActual request) {
        hearingIdValidator.isValidFormat(hearingId.toString());
        HearingEntity hearing = getHearing(hearingId);
        String hearingStatus = hearing.getStatus();
        hearingActualsValidator.validateHearingStatusForActuals(hearingStatus);
        validateRequestPayload(request, hearing);

        Optional<HearingResponseEntity> latestVersionHearingResponse = hearing.getHearingResponseForLatestRequest();
        if (latestVersionHearingResponse.isEmpty()) {
            throw new BadRequestException(String.format(HEARING_ACTUALS_NO_HEARING_RESPONSE_FOUND, hearingId));
        }
        upsertNewHearingActuals(latestVersionHearingResponse.get(), request, getServiceName(clientS2SToken), hearing);
    }

    private void upsertNewHearingActuals(HearingResponseEntity latestVersionHearingResponse, HearingActual request,
                                         String clientS2SToken, HearingEntity hearingEntity) {
        ActualHearingEntity actualHearing = hearingActualsMapper
            .toActualHearingEntity(request);
        latestVersionHearingResponse.setActualHearingEntity(actualHearing);
        actualHearing.setHearingResponse(latestVersionHearingResponse);
        actualHearingRepository.save(actualHearing);
        hearingStatusAuditService.saveAuditTriageDetails(hearingEntity, hearingEntity.getUpdatedDateTime(),
                                                         PUT_HEARING_ACTUALS_COMPLETION, null, getServiceName(clientS2SToken),
                                                         HMC, null);
    }

    private void validateRequestPayload(HearingActual request, HearingEntity hearing) {
        hearingActualsValidator.validateHearingActualDaysNotInTheFuture(request.getActualHearingDays());
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

    private String getServiceName(String clientS2SToken) {
        return securityUtils.getServiceNameFromS2SToken(clientS2SToken);
    }
}
