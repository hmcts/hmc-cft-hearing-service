package uk.gov.hmcts.reform.hmc.controllers;

import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.reform.hmc.TestIdamConfiguration;
import uk.gov.hmcts.reform.hmc.config.SecurityConfiguration;
import uk.gov.hmcts.reform.hmc.security.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.hmc.service.PartiesNotifiedService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
class PartiesNotifiedControllerTest {

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

    @Test
    void shouldReturn200_whenNo_PartiesNotifiedDateTimes() {
        final Long hearingId = 2000000099L;
        List<LocalDateTime> partiesNotifiedDateTimeExpected = new ArrayList<>();
        when(partiesNotifiedService.getPartiesNotified(hearingId)).thenReturn(partiesNotifiedDateTimeExpected);

        PartiesNotifiedController controller = new PartiesNotifiedController(partiesNotifiedService);
        List<LocalDateTime> partiesNotifiedDateTimes = controller.getPartiesNotified(hearingId);
        assertTrue(partiesNotifiedDateTimes.isEmpty());
        verify(partiesNotifiedService, times(1)).getPartiesNotified(any());
    }

    @Test
    void shouldReturn200_whenSome_PartiesNotifiedDateTimes() {
        final Long hearingId = 2000000099L;
        List<LocalDateTime> partiesNotifiedDateTimeExpected = generatePartiesNotifiedDateTimes();
        when(partiesNotifiedService.getPartiesNotified(hearingId)).thenReturn(partiesNotifiedDateTimeExpected);

        PartiesNotifiedController controller = new PartiesNotifiedController(partiesNotifiedService);
        List<LocalDateTime> partiesNotifiedDateTimes = controller.getPartiesNotified(hearingId);
        assertFalse(partiesNotifiedDateTimes.isEmpty());
        verify(partiesNotifiedService, times(1)).getPartiesNotified(any());
    }

    @Test
    void shouldReturn400_when_null_hearingId() {
        final Long hearingId = null;
        PartiesNotifiedController controller = new PartiesNotifiedController(partiesNotifiedService);
        List<LocalDateTime> partiesNotifiedDateTimes = controller.getPartiesNotified(hearingId);
        assertTrue(partiesNotifiedDateTimes.isEmpty());
        verify(partiesNotifiedService, times(1)).getPartiesNotified(any());
    }

    @Test
    void shouldReturn400_when_invalid_hearingId() {
        final Long hearingId = 1000000099L;
        PartiesNotifiedController controller = new PartiesNotifiedController(partiesNotifiedService);
        List<LocalDateTime> partiesNotifiedDateTimes = controller.getPartiesNotified(hearingId);
        assertTrue(partiesNotifiedDateTimes.isEmpty());
        verify(partiesNotifiedService, times(1)).getPartiesNotified(any());
    }

    @Test
    void shouldReturn404_when_hearingIdNotFound() {
        final Long hearingId = 2000000099L;
        PartiesNotifiedController controller = new PartiesNotifiedController(partiesNotifiedService);
        List<LocalDateTime> partiesNotifiedDateTimes = controller.getPartiesNotified(hearingId);
        assertTrue(partiesNotifiedDateTimes.isEmpty());
        verify(partiesNotifiedService, times(1)).getPartiesNotified(any());
    }

    private List<LocalDateTime> generatePartiesNotifiedDateTimes() {
        List<LocalDateTime> partiesNotifiedDateTimes = new ArrayList<>();
        partiesNotifiedDateTimes.add(LocalDateTime.now().minusDays(6));
        partiesNotifiedDateTimes.add(LocalDateTime.now().minusDays(4));
        partiesNotifiedDateTimes.add(LocalDateTime.now().minusDays(2));
        return partiesNotifiedDateTimes;
    }

}
