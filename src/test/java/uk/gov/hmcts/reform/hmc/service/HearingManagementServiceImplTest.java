package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.hmc.config.MessageSenderConfiguration;
import uk.gov.hmcts.reform.hmc.service.common.ObjectMapperService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class HearingManagementServiceImplTest {

    private HearingManagementServiceImpl hearingManagementService;

    @Mock
    private MessageSenderConfiguration messageSenderConfiguration;

    @Mock
    private ObjectMapperService objectMapperService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        hearingManagementService = new HearingManagementServiceImpl(messageSenderConfiguration, objectMapperService);
    }

    @Test
    void shouldVerifySubsequentCalls() throws JsonProcessingException {
        String json = "{\"query\": {\"match\": \"blah blah\"}}";
        JsonNode jsonNode = new ObjectMapper().readTree("{\"query\": {\"match\": \"blah blah\"}}");
        when(objectMapperService.convertObjectToJsonNode(json)).thenReturn(jsonNode);
        doNothing().when(messageSenderConfiguration).sendMessage(Mockito.any());
        hearingManagementService.sendResponse(json);
        verify(objectMapperService, times(1)).convertObjectToJsonNode(any());
        verify(messageSenderConfiguration, times(1)).sendMessage(any());
    }

}
