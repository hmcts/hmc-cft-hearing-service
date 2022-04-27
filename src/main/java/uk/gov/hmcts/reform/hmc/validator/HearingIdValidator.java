package uk.gov.hmcts.reform.hmc.validator;

import com.microsoft.applicationinsights.core.dependencies.apachecommons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.ActualHearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.model.HearingResultType;
import uk.gov.hmcts.reform.hmc.repository.ActualHearingDayRepository;
import uk.gov.hmcts.reform.hmc.repository.ActualHearingRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_ID_VALID_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;
import static uk.gov.hmcts.reform.hmc.model.HearingResultType.ADJOURNED;
import static uk.gov.hmcts.reform.hmc.model.HearingResultType.CANCELLED;
import static uk.gov.hmcts.reform.hmc.model.HearingResultType.COMPLETED;

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

    public Optional<HearingResponseEntity> getHearingResponse(HearingEntity hearingEntity) {
        Integer version = hearingEntity.getLatestRequestVersion();
        Optional<HearingResponseEntity> hearingResponse = hearingEntity
                .getHearingResponses().stream().filter(hearingResponseEntity ->
                        hearingResponseEntity.getResponseVersion().equals(version))
                .collect(Collectors.toList()).stream()
                .max(Comparator.comparing(hearingResponseEntity -> hearingResponseEntity.getRequestTimeStamp()));

        return hearingResponse;
    }


    public String getStatus(Long hearingId) {
        return hearingRepository.getStatus(hearingId);
    }


    public void validateHearingOutcomeInformation(Long hearingId, String errorMessage) {
        Optional<ActualHearingEntity> entity = getActualHearing(hearingId);
        if (entity.isEmpty()) {
            throw new BadRequestException(errorMessage);
        }
    }

    public void validateHearingResultType(Long hearingId, String errorMessage) {
        Optional<ActualHearingEntity> entity = getActualHearing(hearingId);
        if (entity.isPresent()) {
            HearingResultType hearingResultType = entity.get().getHearingResultType();

            if ((hearingResultType.getLabel().equals(COMPLETED.getLabel())
                    || hearingResultType.getLabel().equals(ADJOURNED.getLabel()))
                    && actualHearingDayRepository.findByActualHearing(entity.get()).isEmpty()) {
                throw new BadRequestException(errorMessage);
            }
        }
    }

    public Optional<ActualHearingEntity> getActualHearing(Long hearingId) {
        Optional<HearingResponseEntity> hearingResponseEntity =
                getHearingResponse(hearingRepository.findById(hearingId)
                        .orElseThrow(() -> new HearingNotFoundException(hearingId, HEARING_ID_NOT_FOUND)));
        if (hearingResponseEntity.isPresent()) {
            return actualHearingRepository.findByHearingResponse(hearingResponseEntity.get());
        }
        return Optional.empty();
    }

    public void validateCancelHearingResultType(Long hearingId, String errorMessage) {
        Optional<ActualHearingEntity> entity = getActualHearing(hearingId);
        if (entity.isPresent()) {
            HearingResultType hearingResultType = entity.get().getHearingResultType();

            if ((hearingResultType.getLabel().equals(CANCELLED.getLabel()))
                    && actualHearingDayRepository.findByActualHearing(entity.get()).isPresent()) {
                throw new BadRequestException(errorMessage);
            }
        }
    }



}
