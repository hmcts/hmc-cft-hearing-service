package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.CancellationReason;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiDeleteHearingRequest;

@Component
public class HmiDeleteHearingRequestMapper {

    /**
     * map Delete hearing Request to HMI Submit Delete Hearing Request.
     * @param hearingRequest delete hearing request
     * @return hmiDeleteHearingRequest HMI Delete Hearing request object
     */
    public HmiDeleteHearingRequest mapRequest(DeleteHearingRequest hearingRequest) {

        return HmiDeleteHearingRequest.builder()
                .cancellationReason(new CancellationReason(hearingRequest.getCancellationReasonCode()))
                .build();
    }

}
