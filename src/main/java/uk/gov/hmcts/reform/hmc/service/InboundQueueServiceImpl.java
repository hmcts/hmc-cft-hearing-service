package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingCode;
import uk.gov.hmcts.reform.hmc.client.hmi.HearingResponse;
import uk.gov.hmcts.reform.hmc.config.MessageType;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus;
import uk.gov.hmcts.reform.hmc.exceptions.MalformedMessageException;
import uk.gov.hmcts.reform.hmc.helper.hmi.HmiHearingResponseMapper;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static uk.gov.hmcts.reform.hmc.domain.model.enums.HearingStatus.EXCEPTION;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_NOT_FOUND;

@Service
@Component
@Slf4j
public class InboundQueueServiceImpl extends HearingIdValidator implements InboundQueueService {

    private final ObjectMapper objectMapper;
    private HearingRepository hearingRepository;
    private final HmiHearingResponseMapper hmiHearingResponseMapper;
    private static final String HEARING_ID = "hearing_id";
    public static final String UNSUPPORTED_HEARING_STATUS = "Hearing has unsupported value for hearing status";

    public InboundQueueServiceImpl(ObjectMapper objectMapper, HearingRepository hearingRepository,
                                   HmiHearingResponseMapper hmiHearingResponseMapper) {
        super(hearingRepository);
        this.objectMapper = objectMapper;
        this.hearingRepository = hearingRepository;
        this.hmiHearingResponseMapper = hmiHearingResponseMapper;
    }

    @Override
    public void processMessage(JsonNode message, Map<String, Object> applicationProperties)
        throws JsonProcessingException {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        HearingResponse hearingResponse = objectMapper.treeToValue(message, HearingResponse.class);
        Set<ConstraintViolation<HearingResponse>> violations = validator.validate(hearingResponse);
        if (violations.size() == 0) {
            log.info("Successfully converted message to HearingResponseType " + hearingResponse);
            updateHearing(hearingResponse, applicationProperties);

        } else {
            log.info("Total violations found: " + violations.size());
            for (ConstraintViolation<HearingResponse> violation : violations) {
                log.error("Violations are " + violation.getMessage());
            }
        }
    }

    @Override
    public HearingStatus getHearingStatus(HearingResponse hearing, HearingEntity hearingEntity) {
        HearingStatus currentStatus = HearingStatus.valueOf(hearingEntity.getStatus());
        HearingCode laStatus = hearing.getHearing().getHearingCaseStatus().getCode();
        HearingStatus postStatus = null;

        switch (laStatus) {
            case EXCEPTION:
                postStatus = EXCEPTION;
                break;
            case LISTED:
                int currentVersion = hearingEntity.getCaseHearingRequest().getVersionNumber();
                int hearingVersion = hearing.getHearing().getHearingCaseVersionId();
                postStatus = getHearingStatusWhenLaStatusIsListed(currentStatus, hearingVersion, currentVersion);
                break;
            case PENDING_RELISTING:
                postStatus = currentStatus;
                break;
            case CLOSED:
                postStatus = getHearingStatusWhenLaStatusIsClosed(currentStatus);
                break;
            default:
                throw new MalformedMessageException(UNSUPPORTED_HEARING_STATUS);
        }
        return postStatus;
    }

    private void updateHearing(HearingResponse hearingResponse, Map<String, Object> applicationProperties) {
        if (applicationProperties.containsKey(HEARING_ID)) {
            Long hearingId = Long.valueOf(applicationProperties.get(HEARING_ID).toString());
            validateHearingId(hearingId, HEARING_ID_NOT_FOUND);
            updateHearingAndStatus(hearingId, hearingResponse);
        }

    }

    private void updateHearingAndStatus(Long hearingId, HearingResponse hearingResponse) {

        Optional<HearingEntity> hearingResult = hearingRepository.findById(hearingId);
        if (hearingResult.isPresent()) {
            HearingEntity hearingToSave = hmiHearingResponseMapper.mapHmiHearingToEntity(
                hearingResponse,
                hearingResult.get()
            );
            hearingRepository.save(hearingToSave);
            // transform and add to queue 79
        }
    }

    private HearingStatus getHearingStatusWhenLaStatusIsListed(HearingStatus currentStatus,
                                                               int hearingVersion,
                                                               int currentVersion) {
        HearingStatus postStatus = null;
        switch (currentStatus) {
            case AWAITING_LISTING:
            case UPDATE_SUBMITTED:
            case LISTED:
                postStatus = HearingStatus.LISTED;
                break;
            case UPDATE_REQUESTED:
                if (hearingVersion == currentVersion) {
                    postStatus = HearingStatus.LISTED;
                } else {
                    postStatus = HearingStatus.UPDATE_REQUESTED;
                }
                break;
            case CANCELLATION_REQUESTED:
                if (hearingVersion == currentVersion) {
                    postStatus = EXCEPTION;
                } else {
                    postStatus = HearingStatus.CANCELLATION_REQUESTED;
                }
                break;
            case EXCEPTION:
                postStatus = EXCEPTION;
                break;
            default:
                throw new MalformedMessageException(UNSUPPORTED_HEARING_STATUS);
        }
        return postStatus;
    }

    private HearingStatus getHearingStatusWhenLaStatusIsClosed(HearingStatus currentStatus) {
        HearingStatus postStatus = null;
        switch (currentStatus) {
            case AWAITING_LISTING:
            case UPDATE_SUBMITTED:
            case LISTED:
            case UPDATE_REQUESTED:
            case CANCELLATION_REQUESTED:
                postStatus = HearingStatus.CANCELLED;
                break;
            case EXCEPTION:
                postStatus = EXCEPTION;
                break;
            default:
                throw new MalformedMessageException(UNSUPPORTED_HEARING_STATUS);
        }
        return postStatus;
    }
}
