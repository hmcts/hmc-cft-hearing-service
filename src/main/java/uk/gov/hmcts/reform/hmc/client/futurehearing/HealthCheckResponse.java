package uk.gov.hmcts.reform.hmc.client.futurehearing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.actuate.health.Status;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class HealthCheckResponse {
    private Status status;
}
