package uk.gov.hmcts.reform.hmc.validator;

import com.microsoft.applicationinsights.core.dependencies.apachecommons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_ID_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;

@Component
public class HearingIdValidator {

    public final HearingRepository hearingRepository;

    @Autowired
    public HearingIdValidator(HearingRepository hearingRepository) {
        this.hearingRepository = hearingRepository;
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
        if (hearingIdStr.length() != HEARING_ID_MAX_LENGTH || !StringUtils.isNumeric(hearingIdStr)
                || hearingIdStr.charAt(0) != '2') {
            throw new BadRequestException(INVALID_HEARING_ID_DETAILS);
        }
    }

    /**
     * get Hearing.
     *
     * @param hearingId hearing Id
     * @return hearing hearing
     */
    public Optional<HearingEntity> getHearing(Long hearingId) {
        return hearingRepository.findById(hearingId);
    }

    public List<HearingEntity> getHearingsByRequestId(String requestId) {
        return hearingRepository.findByRequestId(requestId);
    }


    public Optional<HearingResponseEntity> getHearingResponse(HearingEntity hearingEntity) {
        Integer version = hearingEntity.getLatestRequestVersion();
        List<HearingResponseEntity> entities = hearingEntity.getHearingResponses();
        List<HearingResponseEntity> filteredEntities = entities.stream().filter(hearingResponseEntity ->
                        null != hearingResponseEntity
                                && null != hearingResponseEntity.getResponseVersion()
                                && hearingResponseEntity.getResponseVersion().equals(version))
                .collect(Collectors.toList());
        if (filteredEntities.size() == 1) {
            return Optional.of(entities.get(0));
        } else if (filteredEntities.size() > 1) {
            return filteredEntities.stream()
                    .max(Comparator.comparing(HearingResponseEntity::getRequestTimeStamp));
        }
        return Optional.empty();
    }
}
