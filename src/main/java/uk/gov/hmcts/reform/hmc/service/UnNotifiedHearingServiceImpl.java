package uk.gov.hmcts.reform.hmc.service;

import com.microsoft.applicationinsights.core.dependencies.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.model.UnNotifiedHearingsResponse;
import uk.gov.hmcts.reform.hmc.repository.CaseHearingRequestRepository;
import uk.gov.hmcts.reform.hmc.repository.UnNotifiedHearingsRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.hmc.constants.Constants.EXCEPTION_STATUS;
import static uk.gov.hmcts.reform.hmc.constants.Constants.FIRST_PAGE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.UN_NOTIFIED_HEARINGS_LIMIT;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.HEARING_STATUS_EXCEPTION;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HMCTS_SERVICE_CODE;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.HEARING_MANAGER;

@Service
@Component
@Slf4j
public class UnNotifiedHearingServiceImpl implements UnNotifiedHearingService {

    private final CaseHearingRequestRepository caseHearingRequestRepository;
    private final UnNotifiedHearingsRepository unNotifiedHearingsRepository;
    private AccessControlService accessControlService;


    @Autowired
    public UnNotifiedHearingServiceImpl(CaseHearingRequestRepository caseHearingRequestRepository,
                                        UnNotifiedHearingsRepository unNotifiedHearingsRepository,
                                        AccessControlService accessControlService) {
        this.caseHearingRequestRepository = caseHearingRequestRepository;
        this.unNotifiedHearingsRepository = unNotifiedHearingsRepository;
        this.accessControlService = accessControlService;
    }

    @Override
    public UnNotifiedHearingsResponse getUnNotifiedHearings(String hmctsServiceCode,
                                                            LocalDateTime hearingStartDateFrom,
                                                            LocalDateTime hearingStartDateTo,
                                                            List<String> hearingStatus) {
        if (null != hearingStatus && hearingStatus.size() == 1
            && hearingStatus.get(0).equalsIgnoreCase(EXCEPTION_STATUS)) {
            throw new BadRequestException(HEARING_STATUS_EXCEPTION);
        }
        isValidHmctsServiceCode(hmctsServiceCode);
        List<Long> hearings = getUnNotifiedHearingResults(
            hmctsServiceCode, hearingStartDateFrom, hearingStartDateTo, hearingStatus);
        accessControlService.verifyUserRoleAccess(Lists.newArrayList(HEARING_MANAGER));
        List<String> hearingIds = getHearingIdInStrings(hearings);
        return getUnNotifiedHearingsResponse(hearingIds, Long.valueOf(hearings.size()));
    }

    private List<String> getHearingIdInStrings(List<Long> hearingIdsLong) {
        return hearingIdsLong.stream().map(Object::toString)
            .collect(Collectors.toList());
    }

    private UnNotifiedHearingsResponse getUnNotifiedHearingsResponse(List<String> hearingIds, Long totalCount) {
        UnNotifiedHearingsResponse response = new UnNotifiedHearingsResponse();
        response.setHearingIds(hearingIds);
        response.setTotalFound(totalCount);
        return response;
    }

    private List<Long> getUnNotifiedHearingResults(String hmctsServiceCode, LocalDateTime hearingStartDateFrom,
                                                                       LocalDateTime hearingStartDateTo,
                                                   List<String> hearingStatus) {
        Pageable limitUnNotifiedHearingsTo = PageRequest.of(FIRST_PAGE, UN_NOTIFIED_HEARINGS_LIMIT);
        if (null != hearingStartDateTo) {
            return unNotifiedHearingsRepository.getUnNotifiedHearingsWithStartDateTo(
                hmctsServiceCode,
                hearingStartDateFrom,
                hearingStartDateTo,
                hearingStatus,
                limitUnNotifiedHearingsTo
            );
        } else {
            return unNotifiedHearingsRepository.getUnNotifiedHearingsWithOutStartDateTo(
                hmctsServiceCode,
                hearingStartDateFrom,
                hearingStatus,
                limitUnNotifiedHearingsTo);
        }
    }

    private void isValidHmctsServiceCode(String hmctsServiceCode) {
        Long results = caseHearingRequestRepository.getHmctsServiceCodeCount(hmctsServiceCode);
        if (results == 0) {
            throw new BadRequestException(INVALID_HMCTS_SERVICE_CODE);
        }
    }

}
