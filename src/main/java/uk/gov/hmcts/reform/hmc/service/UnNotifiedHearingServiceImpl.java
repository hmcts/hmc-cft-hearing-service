package uk.gov.hmcts.reform.hmc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.model.UnNotifiedHearingsResponse;
import uk.gov.hmcts.reform.hmc.repository.CaseHearingRequestRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;

import java.util.List;

import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HMCTS_SERVICE_CODE;

@Service
@Component
@Slf4j
public class UnNotifiedHearingServiceImpl implements UnNotifiedHearingService {

    private final CaseHearingRequestRepository caseHearingRequestRepository;
    private final HearingRepository hearingRepository;


    @Autowired
    public UnNotifiedHearingServiceImpl(CaseHearingRequestRepository caseHearingRequestRepository,
                                        HearingRepository hearingRepository) {
        this.caseHearingRequestRepository = caseHearingRequestRepository;
        this.hearingRepository = hearingRepository;
    }

    @Override
    public UnNotifiedHearingsResponse getUnNotifiedHearings(String hmctsServiceCode, String hearingStartDateFrom,
                                                            String hearingStartDateTo) {
        isValidHmctsServiceCode(hmctsServiceCode);
        List<String> hearingIds = getUnNotifiedHearingResults(
            hmctsServiceCode, hearingStartDateFrom, hearingStartDateTo);
        return getUnNotifiedHearingsResponse(hearingIds);
    }

    private UnNotifiedHearingsResponse getUnNotifiedHearingsResponse(List<String> hearingIds) {
        UnNotifiedHearingsResponse response = new UnNotifiedHearingsResponse();
        response.setHearingIds(hearingIds);
        return response;
    }

    private List<String> getUnNotifiedHearingResults(String hmctsServiceCode, String hearingStartDateFrom,
                                                     String hearingStartDateTo) {
        return hearingRepository.getUnNotifiedHearings(hmctsServiceCode, hearingStartDateFrom,
                                                                 hearingStartDateTo);
    }

    private void isValidHmctsServiceCode(String hmctsServiceCode) {
        Long results = caseHearingRequestRepository.getHmctsServiceCode(hmctsServiceCode);
        if (results == 0) {
            throw new BadRequestException(INVALID_HMCTS_SERVICE_CODE);
        }
    }

}
