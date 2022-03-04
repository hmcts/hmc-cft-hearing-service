package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.model.UnNotifiedHearingsResponse;
import uk.gov.hmcts.reform.hmc.repository.CaseHearingRequestRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingDayDetailsRepository;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.hmc.constants.Constants.FIRST_PAGE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.UN_NOTIFIED_HEARINGS_LIMIT;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HMCTS_SERVICE_CODE;

@Service
@Component
@Slf4j
public class UnNotifiedHearingServiceImpl implements UnNotifiedHearingService {

    private final CaseHearingRequestRepository caseHearingRequestRepository;
    private final HearingDayDetailsRepository hearingDayDetailsRepository;


    @Autowired
    public UnNotifiedHearingServiceImpl(CaseHearingRequestRepository caseHearingRequestRepository,
                                        HearingDayDetailsRepository hearingDayDetailsRepository) {
        this.caseHearingRequestRepository = caseHearingRequestRepository;
        this.hearingDayDetailsRepository = hearingDayDetailsRepository;
    }

    @Override
    public UnNotifiedHearingsResponse getUnNotifiedHearings(String hmctsServiceCode, LocalDateTime hearingStartDateFrom,
                                                            LocalDateTime hearingStartDateTo) {
        isValidHmctsServiceCode(hmctsServiceCode);
        List<String> hearingIds = getUnNotifiedHearingResults(
            hmctsServiceCode, hearingStartDateFrom, hearingStartDateTo);
        Long totalCount = getUnNotifiedHearingsTotalCount(hmctsServiceCode, hearingStartDateFrom,
                                                          hearingStartDateTo
        );
        return getUnNotifiedHearingsResponse(hearingIds, totalCount);
    }

    private Long getUnNotifiedHearingsTotalCount(String hmctsServiceCode, LocalDateTime hearingStartDateFrom,
                                                 LocalDateTime hearingStartDateTo) {
        if (null != hearingStartDateTo) {
            return hearingDayDetailsRepository.getUnNotifiedHearingsTotalCountWithStartDateTo(
                hmctsServiceCode, hearingStartDateFrom, hearingStartDateTo);
        } else {
            return hearingDayDetailsRepository.getUnNotifiedHearingsTotalCountWithOutStartDateTo(
                hmctsServiceCode,
                hearingStartDateFrom
            );
        }
    }

    private UnNotifiedHearingsResponse getUnNotifiedHearingsResponse(List<String> hearingIds, Long totalCount) {
        UnNotifiedHearingsResponse response = new UnNotifiedHearingsResponse();
        response.setHearingIds(hearingIds);
        response.setTotalFound(totalCount);
        return response;
    }

    private List<String> getUnNotifiedHearingResults(String hmctsServiceCode, LocalDateTime hearingStartDateFrom,
                                                     LocalDateTime hearingStartDateTo) {
        Pageable limitUnNotifiedHearingsTo = PageRequest.of(FIRST_PAGE, UN_NOTIFIED_HEARINGS_LIMIT);
        if (null != hearingStartDateTo) {
            return hearingDayDetailsRepository.getUnNotifiedHearingsWithStartDateTo(
                hmctsServiceCode,
                hearingStartDateFrom,
                hearingStartDateTo,
                limitUnNotifiedHearingsTo
            );
        } else {
            return hearingDayDetailsRepository.getUnNotifiedHearingsWithOutStartDateTo(
                hmctsServiceCode,
                hearingStartDateFrom,
                limitUnNotifiedHearingsTo
            );
        }
    }

    private void isValidHmctsServiceCode(String hmctsServiceCode) {
        Long results = caseHearingRequestRepository.getHmctsServiceCodeCount(hmctsServiceCode);
        if (results == 0) {
            throw new BadRequestException(INVALID_HMCTS_SERVICE_CODE);
        }
    }

}
