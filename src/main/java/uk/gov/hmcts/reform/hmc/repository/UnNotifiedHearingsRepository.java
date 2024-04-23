package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface UnNotifiedHearingsRepository {

    List<Long> getUnNotifiedHearingsWithOutStartDateTo(String hmctsServiceCode, LocalDateTime hearingStartDateFrom,
                                                       List<String> hearingStatus, Pageable pageable);

    List<Long> getUnNotifiedHearingsWithStartDateTo(String hmctsServiceCode, LocalDateTime hearingStartDateFrom,
                                                    LocalDateTime hearingStartDateTo, List<String> hearingStatus,
                                                    Pageable pageable);
}
