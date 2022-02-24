package uk.gov.hmcts.reform.hmc.service;

import uk.gov.hmcts.reform.hmc.model.UnNotifiedHearingsResponse;

public interface UnNotifiedHearingService {

    UnNotifiedHearingsResponse getUnNotifiedHearings(String hmctsServiceCode, String hearingStartDateFrom,
                                                     String hearingStartDateTo);
}
