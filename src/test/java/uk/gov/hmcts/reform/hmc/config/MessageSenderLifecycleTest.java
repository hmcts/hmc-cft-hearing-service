package uk.gov.hmcts.reform.hmc.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import uk.gov.hmcts.reform.hmc.ApplicationParams;

import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class MessageSenderLifecycleTest {

    @Test
    void reusesAndClosesTheQueueSender() {
        assertSenderLifecycle(
            MessageSenderToQueueConfiguration.class,
            (sender, message) -> sender.sendMessageToQueue(message, 123L, "AMEND_HEARING", "deployment")
        );
    }

    @Test
    void reusesAndClosesTheTopicSender() {
        assertSenderLifecycle(MessageSenderToTopicConfiguration.class,
                              (sender, message) -> sender.sendMessage(message, "ABA5", "123", "deployment"));
    }

    private <T> void assertSenderLifecycle(Class<T> senderType, BiConsumer<T, String> send) {
        var client = mock(ServiceBusSenderClient.class);
        var senderBuilder = mock(ServiceBusClientBuilder.ServiceBusSenderClientBuilder.class, RETURNS_SELF);
        when(senderBuilder.buildClient()).thenReturn(client);

        try (var builders = mockConstruction(
            ServiceBusClientBuilder.class,
            withSettings().defaultAnswer(RETURNS_SELF),
            (builder, context) -> when(builder.sender()).thenReturn(senderBuilder)
        )) {
            try (var application = new AnnotationConfigApplicationContext()) {
                application.getBeanFactory().registerSingleton("applicationParams", new ApplicationParams());
                application.register(senderType);
                application.refresh();

                var sender = application.getBean(senderType);
                assertThat(builders.constructed()).isEmpty();

                send.accept(sender, "first message");
                send.accept(sender, "second message");

                var messages = ArgumentCaptor.forClass(ServiceBusMessage.class);
                verify(client, times(2)).sendMessage(messages.capture());
                assertThat(messages.getAllValues()).extracting(message -> message.getBody().toString())
                    .containsExactly("first message", "second message");
                assertThat(builders.constructed()).hasSize(1);
                verify(senderBuilder).buildClient();
                verify(client, never()).close();
            }
            verify(client).close();
        }
    }
}
