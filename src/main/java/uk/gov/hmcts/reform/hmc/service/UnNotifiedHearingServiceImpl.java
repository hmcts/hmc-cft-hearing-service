package uk.gov.hmcts.reform.hmc.service;

import com.microsoft.applicationinsights.core.dependencies.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.model.UnNotifiedHearingsResponse;
import uk.gov.hmcts.reform.hmc.repository.CaseHearingRequestRepository;
import uk.gov.hmcts.reform.hmc.repository.HearingResponseRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.hmc.constants.Constants.FIRST_PAGE;
import static uk.gov.hmcts.reform.hmc.constants.Constants.UN_NOTIFIED_HEARINGS_LIMIT;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HMCTS_SERVICE_CODE;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.HEARING_MANAGER;

@Service
@Component
@Slf4j
public class UnNotifiedHearingServiceImpl implements UnNotifiedHearingService {

    private final CaseHearingRequestRepository caseHearingRequestRepository;
    private final HearingResponseRepository hearingResponseRepository;
    private AccessControlService accessControlService;


    @Autowired
    public UnNotifiedHearingServiceImpl(CaseHearingRequestRepository caseHearingRequestRepository,
                                        HearingResponseRepository hearingResponseRepository,
                                        AccessControlService accessControlService) {
        this.caseHearingRequestRepository = caseHearingRequestRepository;
        this.hearingResponseRepository = hearingResponseRepository;
        this.accessControlService = accessControlService;
    }

    @Override
    public UnNotifiedHearingsResponse getUnNotifiedHearings(String hmctsServiceCode,
                                                            LocalDateTime hearingStartDateFrom,
                                                            LocalDateTime hearingStartDateTo) {
        isValidHmctsServiceCode(hmctsServiceCode);
        Page<Long> page = getUnNotifiedHearingResults(
            hmctsServiceCode, hearingStartDateFrom, hearingStartDateTo);
        accessControlService.verifyUserRoleAccess(Lists.newArrayList(HEARING_MANAGER));
        List<String> hearingIds = getHearingIdInStrings(page.getContent());
        return getUnNotifiedHearingsResponse(hearingIds, page.getTotalElements());
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

    private Page<Long> getUnNotifiedHearingResults(String hmctsServiceCode, LocalDateTime hearingStartDateFrom,
                                                                       LocalDateTime hearingStartDateTo) {
        Pageable limitUnNotifiedHearingsTo = PageRequest.of(FIRST_PAGE, UN_NOTIFIED_HEARINGS_LIMIT);
        if (null != hearingStartDateTo) {
            return hearingResponseRepository.getUnNotifiedHearingsWithStartDateTo(
                hmctsServiceCode,
                hearingStartDateFrom,
                hearingStartDateTo,
                limitUnNotifiedHearingsTo
            );
        } else {
            return hearingResponseRepository.getUnNotifiedHearingsWithOutStartDateTo(
                hmctsServiceCode,
                hearingStartDateFrom,
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
