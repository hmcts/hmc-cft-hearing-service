package uk.gov.hmcts.reform.hmc;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

import javax.inject.Named;
import javax.inject.Singleton;

@Getter
@Named
@Singleton
public class ApplicationParams {

    @Value("${jms.servicebus.connection-string}")
    private String connectionString;

    @Value("${jms.servicebus.topic-name}")
    private String topicName;

    @Value("${jms.servicebus.subscription-name}")
    private String subscriptionName;

}
