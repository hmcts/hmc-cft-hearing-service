package uk.gov.hmcts.reform.hmc.repository;

import uk.gov.hmcts.reform.hmc.client.futurehearing.AuthenticationResponse;

public interface FutureHearingRepository {

    AuthenticationResponse retrieveAuthToken();

}
