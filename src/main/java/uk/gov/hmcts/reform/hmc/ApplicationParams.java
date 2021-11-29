package uk.gov.hmcts.reform.hmc;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ApplicationParams {

    @Value("${jms.servicebus.internal.connection-string}")
    private String internalConnectionString;

    @Value("${jms.servicebus.internal.queue-name}")
    private String internalQueueName;

    @Value("${jms.servicebus.internal.exponential-multiplier}")
    private String exponentialMultiplier;

    @Value("${jms.servicebus.internal.max-retry-attempts}")
    private int maxRetryAttempts;

    @Value("${jms.servicebus.external.connection-string}")
    private String externalConnectionString;

    @Value("${jms.servicebus.external.topic-name}")
    private String externalTopicName;

}
