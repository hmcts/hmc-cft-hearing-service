package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.config.MessageSenderToTopicConfiguration;
import uk.gov.hmcts.reform.hmc.service.common.ObjectMapperService;

@Service
@Component
@Slf4j
public class HearingManagementServiceImpl implements HearingManagementService {

    private final MessageSenderToTopicConfiguration messageSenderToTopicConfiguration;

    private final ObjectMapperService objectMapperService;

    @Autowired
    public HearingManagementServiceImpl(MessageSenderToTopicConfiguration messageSenderToTopicConfiguration,
                                        ObjectMapperService objectMapperService) {
        this.messageSenderToTopicConfiguration = messageSenderToTopicConfiguration;
        this.objectMapperService = objectMapperService;
    }

    @Override
    public void sendResponse(String json) {
        sendRspToTopic(json);
    }

    private void sendRspToTopic(Object response) {
        var jsonNode  = objectMapperService.convertObjectToJsonNode(response);
        messageSenderToTopicConfiguration.sendMessage(jsonNode.toString());
    }
}
