package uk.gov.hmcts.reform.hmc.client.hmi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SyncResponse {

    private Integer listAssistHttpStatus;
    private Integer listAssistErrorCode;
    private String listAssistErrorDescription;

    public boolean isSuccess() {
        return HttpStatus.valueOf(listAssistHttpStatus).is2xxSuccessful();
    }
}
