package uk.gov.hmcts.reform.hmc;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ApplicationParams {

    @Value("${jms.servicebus.inbound.connection-string}")
    private String inboundConnectionString;

    @Value("${jms.servicebus.inbound.topic-name}")
    private String inboundTopicName;

    @Value("${jms.servicebus.inbound.subscription-name}")
    private String inboundSubscriptionName;

    @Value("${jms.servicebus.outbound.connection-string}")
    private String outboundConnectionString;

    @Value("${jms.servicebus.outbound.topic-name}")
    private String outboundTopicName;

}
