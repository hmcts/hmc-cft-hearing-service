package uk.gov.hmcts.reform.hmc.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.config.MessageReaderFromQueueConfiguration;
import uk.gov.hmcts.reform.hmc.service.HearingManagementService;
import uk.gov.hmcts.reform.hmc.service.UnNotifiedHearingService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GetWelcomeTest extends BaseTest {

    @Autowired
    private transient MockMvc mockMvc;

    @MockBean
    private HearingManagementService hearingManagementService;

    @MockBean
    private MessageReaderFromQueueConfiguration messageReaderFromQueueConfiguration;

    @MockBean
    private ApplicationParams applicationParams;

    @MockBean
    private UnNotifiedHearingService unNotifiedHearingService;

    @DisplayName("Should welcome upon root request with 200 response code")
    @Test
    void welcomeRootEndpoint() throws Exception {
        MvcResult response = mockMvc.perform(get("/")).andExpect(status().isOk()).andReturn();
        assertThat(response.getResponse().getContentAsString()).startsWith("Welcome");
    }
}
