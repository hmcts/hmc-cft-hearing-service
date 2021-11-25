package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.config.MessageSenderConfiguration;
import uk.gov.hmcts.reform.hmc.service.common.ObjectMapperService;

@Service
@Component
@Slf4j
public class HearingManagementServiceImpl implements HearingManagementService {

    private final MessageSenderConfiguration messageSenderConfiguration;

    private final ObjectMapperService objectMapperService;

    @Autowired
    public HearingManagementServiceImpl(MessageSenderConfiguration messageSenderConfiguration,
                                        ObjectMapperService objectMapperService) {
        this.messageSenderConfiguration = messageSenderConfiguration;
        this.objectMapperService = objectMapperService;
    }

    @Override
    public void sendResponse(String json) {
        sendRspToTopic(json);
    }

    private void sendRspToTopic(Object response) {
        var jsonNode  = objectMapperService.convertObjectToJsonNode(response);
        messageSenderConfiguration.sendMessage(jsonNode.toString());
    }
}
