package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.constants.Constants;
import uk.gov.hmcts.reform.hmc.model.hmi.CancellationReason;
import uk.gov.hmcts.reform.hmc.model.hmi.HmiDeleteHearingRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class HmiDeleteHearingRequestMapperTest {

    @InjectMocks
    private HmiDeleteHearingRequestMapper hmiDeleteHearingRequestMapper;

    @Test
    void shouldReturnHmiDeleteHearingRequestForDeleteHearingRequest() {
        HmiDeleteHearingRequest expectedHmiDeleteHearingRequest = HmiDeleteHearingRequest.builder()
            .cancellationReason(new CancellationReason(Constants.CANCEL))
                .build();

        HmiDeleteHearingRequest actualHmiDeleteHearingRequest =
                hmiDeleteHearingRequestMapper
                .mapRequest();
        assertEquals(expectedHmiDeleteHearingRequest, actualHmiDeleteHearingRequest);
    }

}
