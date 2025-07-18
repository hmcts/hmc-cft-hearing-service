package uk.gov.hmcts.reform.hmc.service;

import uk.gov.hmcts.reform.hmc.model.ManageExceptionsResponse;
import uk.gov.hmcts.reform.hmc.model.SupportRequests;

public interface ManageExceptionsService {

    ManageExceptionsResponse manageExceptions(SupportRequests supportRequests, String clientS2SToken);
}
