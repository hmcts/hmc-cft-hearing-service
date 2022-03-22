package uk.gov.hmcts.reform.hmc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.model.HearingActual;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_ID_NOT_FOUND;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ACTUALS_INVALID_STATUS;

@Service
public class HearingActualsServiceImpl extends HearingIdValidator implements HearingActualsService {

    private static final List<String> allowedActualsStatuses = Arrays.asList("LISTED",
                                                                             "UPDATE_REQUESTED",
                                                                             "UPDATE_SUBMITTED");

    @Autowired
    public HearingActualsServiceImpl(HearingRepository hearingRepository) {
        super(hearingRepository);
    }

    public void updateHearingActuals(Long hearingId, HearingActual request) {
        isValidFormat(hearingId.toString());

        Optional<HearingEntity> hearingEntityOptional = hearingRepository.findById(hearingId);
        validateHearingId(hearingId, hearingEntityOptional);

        HearingEntity hearing = hearingEntityOptional.get();
        String hearingStatus = hearing.getStatus();
        validateHearingStatusForActuals(hearingStatus);
    }

    private void validateHearingStatusForActuals(String hearingStatus) {
        if (allowedActualsStatuses.stream().noneMatch(e -> e.equals(hearingStatus))) {
            throw new BadRequestException(String.format(HEARING_ACTUALS_INVALID_STATUS, hearingStatus));
        }
    }

    private void validateHearingId(Long hearingId, Optional<HearingEntity> hearingEntityOptional) {
        if (hearingEntityOptional.isEmpty()) {
            throw new HearingNotFoundException(hearingId, HEARING_ACTUALS_ID_NOT_FOUND);
        }
    }
}
