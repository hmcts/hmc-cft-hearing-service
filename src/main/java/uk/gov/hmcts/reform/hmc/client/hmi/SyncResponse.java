package uk.gov.hmcts.reform.hmc.client.hmi;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@Builder
public class SyncResponse {

    private Integer listAssistHttpStatus;
    private Integer listAssistErrorCode;
    private String listAssistErrorDescription;

    public boolean isSuccess() {
        return HttpStatus.valueOf(listAssistHttpStatus).is2xxSuccessful();
    }
}
