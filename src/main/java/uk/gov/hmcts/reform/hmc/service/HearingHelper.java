package uk.gov.hmcts.reform.hmc.service;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Optional;

import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_STATE;

@Component
public class HearingHelper {

    public LocalDateTime getLowestStartDateOfMostRecentHearingResponse(HearingEntity hearing) {

        Optional<HearingResponseEntity> mostRecentLatestVersionHearingResponseOpt = hearing.getLatestHearingResponse();
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
}
