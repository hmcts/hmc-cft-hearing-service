package uk.gov.hmcts.reform.hmc.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.hmc.PartiesNotifiedCommonGeneration;
import uk.gov.hmcts.reform.hmc.TestIdamConfiguration;
import uk.gov.hmcts.reform.hmc.config.SecurityConfiguration;
import uk.gov.hmcts.reform.hmc.model.partiesnotified.PartiesNotified;
import uk.gov.hmcts.reform.hmc.model.partiesnotified.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.hmc.model.partiesnotified.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.hmc.security.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.hmc.service.PartiesNotifiedService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = PartiesNotifiedController.class,
        excludeFilters = @ComponentScan.Filter(type = ASSIGNABLE_TYPE, classes =
                {SecurityConfiguration.class, JwtGrantedAuthoritiesConverter.class}))
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(TestIdamConfiguration.class)
class PartiesNotifiedControllerTest extends PartiesNotifiedCommonGeneration {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    WebApplicationContext webApplicationContext;

    @MockBean
    private PartiesNotifiedService partiesNotifiedService;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Nested
    @DisplayName("PutPartiesNotified")
    class PutPartiesNotified {
        @Test
        void shouldReturn200_whenRequestDetailsArePresent() throws JsonProcessingException {
            JsonNode jsonNode = new ObjectMapper().readTree("{\"query\": {\"match\": \"blah blah\"}}");
            PartiesNotified partiesNotified = new PartiesNotified();
            partiesNotified.setServiceData(jsonNode);

            doNothing().when(partiesNotifiedService).getPartiesNotified(
                anyLong(),
                anyInt(),
                any(PartiesNotified.class)
            );

            PartiesNotifiedController controller = new PartiesNotifiedController(partiesNotifiedService);
            controller.putPartiesNotified(partiesNotified, 1L, 10);
            verify(partiesNotifiedService, times(1))
                .getPartiesNotified(anyLong(), anyInt(), any(PartiesNotified.class));
        }
    }

    @Nested
    @DisplayName("GetPartiesNotified")
    class GetPartiesNotified {
        @Test
        void shouldReturn200_whenNo_PartiesNotifiedDateTimes() {
            final Long hearingId = 2000000099L;
            PartiesNotifiedResponses responsesExpected = new PartiesNotifiedResponses();
            responsesExpected.setHearingID(hearingId);
            responsesExpected.setResponses(new ArrayList<>());
            when(partiesNotifiedService.getPartiesNotified(hearingId)).thenReturn(responsesExpected);

            PartiesNotifiedController controller = new PartiesNotifiedController(partiesNotifiedService);
            PartiesNotifiedResponses responses = controller.getPartiesNotified(hearingId);
            assertTrue(responses.getResponses().isEmpty());
            verify(partiesNotifiedService, times(1)).getPartiesNotified(any());
        }

        @Test
        void shouldReturn200_whenSome_PartiesNotifiedDateTimes() {
            final Long hearingId = 2000000099L;
            PartiesNotifiedResponses responsesExpected = new PartiesNotifiedResponses();
            responsesExpected.setHearingID(hearingId);
            List<PartiesNotifiedResponse> partiesNotifiedExpectedList = generateResponses();
            responsesExpected.setResponses(partiesNotifiedExpectedList);
            when(partiesNotifiedService.getPartiesNotified(hearingId)).thenReturn(responsesExpected);

            PartiesNotifiedController controller = new PartiesNotifiedController(partiesNotifiedService);
            PartiesNotifiedResponses responses = controller.getPartiesNotified(hearingId);
            assertFalse(responses.getResponses().isEmpty());
            verify(partiesNotifiedService, times(1)).getPartiesNotified(any());
        }

        @Test
        void shouldReturn400_when_null_hearingId() {
            final Long hearingId = null;
            PartiesNotifiedController controller = new PartiesNotifiedController(partiesNotifiedService);
            PartiesNotifiedResponses responses = controller.getPartiesNotified(hearingId);
            assertNull(responses);
            verify(partiesNotifiedService, times(1)).getPartiesNotified(any());
        }

        @Test
        void shouldReturn400_when_invalid_hearingId() {
            final Long hearingId = 1000000099L;
            PartiesNotifiedController controller = new PartiesNotifiedController(partiesNotifiedService);
            PartiesNotifiedResponses responses = controller.getPartiesNotified(hearingId);

            assertNull(responses);
            verify(partiesNotifiedService, times(1)).getPartiesNotified(any());
        }

        @Test
        void shouldReturn404_when_hearingIdNotFound() {
            final Long hearingId = 2000000099L;
            PartiesNotifiedController controller = new PartiesNotifiedController(partiesNotifiedService);
            PartiesNotifiedResponses responses = controller.getPartiesNotified(hearingId);
            assertNull(responses);
            verify(partiesNotifiedService, times(1)).getPartiesNotified(any());
        }
    }
}
