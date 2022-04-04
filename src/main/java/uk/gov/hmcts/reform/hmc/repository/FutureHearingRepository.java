package uk.gov.hmcts.reform.hmc.repository;

import uk.gov.hmcts.reform.hmc.client.futurehearing.AuthenticationResponse;
import uk.gov.hmcts.reform.hmc.client.futurehearing.HearingManagementInterfaceResponse;

public interface FutureHearingRepository {

    AuthenticationResponse retrieveAuthToken();

    HearingManagementInterfaceResponse deleteLinkedHearingGroup(String requestId);
}
