package uk.gov.hmcts.reform.hmc.service;

import uk.gov.hmcts.reform.hmc.model.UnNotifiedHearingsResponse;

import java.time.LocalDate;
import java.util.Date;

public interface UnNotifiedHearingService {

    UnNotifiedHearingsResponse getUnNotifiedHearings(String hmctsServiceCode, LocalDate hearingStartDateFrom,
                                                     LocalDate hearingStartDateTo);
}
