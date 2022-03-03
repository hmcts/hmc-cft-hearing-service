package uk.gov.hmcts.reform.hmc.service;

import uk.gov.hmcts.reform.hmc.model.UnNotifiedHearingsResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public interface UnNotifiedHearingService {

    UnNotifiedHearingsResponse getUnNotifiedHearings(String hmctsServiceCode, LocalDateTime hearingStartDateFrom,
                                                     LocalDateTime hearingStartDateTo);
}
