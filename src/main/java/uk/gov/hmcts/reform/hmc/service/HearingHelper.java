package uk.gov.hmcts.reform.hmc.service;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_STATE;

@Component
public class HearingHelper extends HearingIdValidator {

    public HearingHelper(HearingRepository hearingRepository) {
        super(hearingRepository);
    }

    public LocalDateTime getLowestStartDateOfMostRecentHearingResponse(HearingEntity hearing) {

        List<HearingResponseEntity> latestVersionHearingResponses = getLatestVersionHearingResponses(hearing);

        Optional<HearingResponseEntity> mostRecentLatestVersionHearingResponseOpt = latestVersionHearingResponses
            .stream().max(Comparator.comparing(HearingResponseEntity::getRequestTimeStamp));
        if (mostRecentLatestVersionHearingResponseOpt.isEmpty()) {
            throw new BadRequestException(INVALID_HEARING_STATE);
        }

        Optional<HearingDayDetailsEntity> hearingDayDetailsOpt = mostRecentLatestVersionHearingResponseOpt.get()
            .getHearingDayDetails().stream()
            .min(Comparator.comparing(HearingDayDetailsEntity::getStartDateTime));
        if (hearingDayDetailsOpt.isEmpty()) {
            throw new BadRequestException(INVALID_HEARING_STATE);
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
