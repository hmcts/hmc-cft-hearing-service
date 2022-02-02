package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.hmi.CancellationReason;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiDeleteHearingRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class HmiDeleteHearingRequestMapperTest {

    @InjectMocks
    private HmiDeleteHearingRequestMapper hmiDeleteHearingRequestMapper;

    @Test
    void shouldReturnHmiDeleteHearingRequestForDeleteHearingRequest() {
        DeleteHearingRequest deleteHearingRequest = new DeleteHearingRequest();
        deleteHearingRequest.setCancellationReasonCode("AMADEUPONE");
        deleteHearingRequest.setVersionNumber(23);

        CancellationReason cancellationReason = new CancellationReason();
        cancellationReason.setCancellationReasonCode(deleteHearingRequest.getCancellationReasonCode());

        HmiDeleteHearingRequest expectedHmiDeleteHearingRequest = HmiDeleteHearingRequest.builder()
                .cancellationReason(cancellationReason)
                .build();

        HmiDeleteHearingRequest actualHmiDeleteHearingRequest =
                hmiDeleteHearingRequestMapper
                .mapRequest(deleteHearingRequest);
        assertEquals(expectedHmiDeleteHearingRequest, actualHmiDeleteHearingRequest);
    }

}
