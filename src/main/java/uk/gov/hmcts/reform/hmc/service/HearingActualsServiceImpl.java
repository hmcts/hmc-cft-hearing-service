package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.hmc.helper.GetHearingActualsResponseMapper;
import uk.gov.hmcts.reform.hmc.model.hearingactuals.HearingActualResponse;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;
import uk.gov.hmcts.reform.hmc.validator.HearingIdValidator;

import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_ID_NOT_FOUND;

@Service
@Component
@Slf4j
public class HearingActualsServiceImpl extends HearingIdValidator implements HearingActualsService {


    private final GetHearingActualsResponseMapper getHearingActualsResponseMapper;

    @Autowired
    public HearingActualsServiceImpl(HearingRepository hearingRepository,
                                     GetHearingActualsResponseMapper getHearingActualsResponseMapper) {
        super(hearingRepository);
        this.getHearingActualsResponseMapper = getHearingActualsResponseMapper;
    }

    @Override
    public ResponseEntity<HearingActualResponse> getHearingActuals(Long hearingId) {
        validateHearingId(hearingId,HEARING_ID_NOT_FOUND);
        val hearingEntity = hearingRepository.findById(hearingId);
        if (hearingEntity.isPresent()) {
            return ResponseEntity.ok(getHearingActualsResponseMapper.toHearingActualResponse(hearingEntity.get()));
        } else {
            throw new HearingNotFoundException(hearingId,HEARING_ID_NOT_FOUND);
        }
    }

}
