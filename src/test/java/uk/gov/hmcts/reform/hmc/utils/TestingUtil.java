package uk.gov.hmcts.reform.hmc.utils;

import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;

public class TestingUtil {

    private TestingUtil() {
    }

    public static DeleteHearingRequest deleteHearingRequest() {
        DeleteHearingRequest request = new DeleteHearingRequest();
        request.setVersionNumber(1);
        request.setCancellationReasonCode("test");
        return request;
    }

}
