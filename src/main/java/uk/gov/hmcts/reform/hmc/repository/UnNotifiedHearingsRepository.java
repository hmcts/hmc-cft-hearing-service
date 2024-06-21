package uk.gov.hmcts.reform.hmc.repository;

import java.time.LocalDateTime;
import java.util.List;

public interface UnNotifiedHearingsRepository {

    List<Long> getUnNotifiedHearingsWithOutStartDateTo(String hmctsServiceCode, LocalDateTime hearingStartDateFrom,
                                                       List<String> hearingStatus);

    List<Long> getUnNotifiedHearingsWithStartDateTo(String hmctsServiceCode, LocalDateTime hearingStartDateFrom,
                                                    LocalDateTime hearingStartDateTo, List<String> hearingStatus);
}
