package uk.gov.hmcts.reform.hmc.service;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.model.UnNotifiedHearingsResponse;
import uk.gov.hmcts.reform.hmc.repository.CaseHearingRequestRepository;

import java.util.List;

import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HMCTS_SERVICE_CODE;

@Service
@Component
@Slf4j
public class UnNotifiedHearingServiceImpl implements UnNotifiedHearingService  {

    private final CaseHearingRequestRepository caseHearingRequestRepository;


    public UnNotifiedHearingServiceImpl(CaseHearingRequestRepository caseHearingRequestRepository) {
        this.caseHearingRequestRepository = caseHearingRequestRepository;
    }

    @Override
    public UnNotifiedHearingsResponse getUnNotifiedHearings(String hmctsServiceCode, String hearingStartDateFrom,
                                                            String hearingStartDateTo) {
        isValidHmctsServiceCode(hmctsServiceCode);

        return null;
    }

    private void isValidHmctsServiceCode(String hmctsServiceCode) {
        List<String> results = caseHearingRequestRepository.getHmctsServiceCode(hmctsServiceCode);
        if(results.isEmpty()) {
            throw new BadRequestException(INVALID_HMCTS_SERVICE_CODE);
        }
    }

}
