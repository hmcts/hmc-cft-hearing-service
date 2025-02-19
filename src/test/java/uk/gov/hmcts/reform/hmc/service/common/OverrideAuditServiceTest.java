package uk.gov.hmcts.reform.hmc.service.common;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import uk.gov.hmcts.reform.hmc.config.UrlManager;
import uk.gov.hmcts.reform.hmc.data.HearingStatusAuditEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingStatusAuditEntity;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.repository.HearingStatusAuditRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingStatusAuditRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
class OverrideAuditServiceTest {

    @SuppressWarnings({"checkstyle:linelength"})
    private static final String DUMMY =
        "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJteVNlcnZpY2VOYW1lIiwiZXhwIjoxNzE5OTI5MTQ1fQ.lVzHP-gt82qsFNc0VAQQdqahyDfD5cGWl6dtrmlrFfPw4D7imFe8Y_Qq-z_e9trpqRvBsOXTzSRUhzG7qY8oHg";

    @Mock
    private HearingStatusAuditRepository hearingStatusAuditRepository;

    @Mock
    private LinkedHearingStatusAuditRepository linkedHearingStatusAuditRepository;

    @Mock
    private UrlManager roleAssignmentUrlManager;

    @Mock
    private UrlManager dataStoreUrlManager;

    @Mock
    private SecurityUtils securityUtils;

    @Captor
    private ArgumentCaptor<HearingStatusAuditEntity> hearingStatusCaptor;

    private OverrideAuditService overrideAuditService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        overrideAuditService = new OverrideAuditService(
            hearingStatusAuditRepository,
            linkedHearingStatusAuditRepository,
            roleAssignmentUrlManager,
            dataStoreUrlManager
        );

        overrideAuditService.setSecurityUtils(securityUtils);

        when(roleAssignmentUrlManager.getUrlHeaderName()).thenReturn("Role-Assignment-Url");
        when(dataStoreUrlManager.getUrlHeaderName()).thenReturn("Data-Store-Url");
    }

    @Test
    void logOverrideAuditShouldCallHearingStatusRepositorySave() {
        // given
        commonSetup();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(
            "org.springframework.web.servlet.HandlerMapping.uriTemplateVariables",
            Map.of("id", "1234"));
        request.addHeader("Role-Assignment-Url", "http://role-assignment.example.org");
        request.addHeader("Data-Store-Url", "http://data-store.example.org");

        // when
        overrideAuditService.logOverrideAudit(request);

        // then
        verify(hearingStatusAuditRepository, times(1)).save(hearingStatusCaptor.capture());
        HearingStatusAuditEntity entity = hearingStatusCaptor.getValue();
        assertThat(entity.getHearingId()).isEqualTo("1234");
        JsonNode otherInfo = entity.getOtherInfo();
        assertThat(otherInfo.get("role-assignment-url").asText()).isEqualTo("http://role-assignment.example.org");
        assertThat(otherInfo.get("data-store-url").asText()).isEqualTo("http://data-store.example.org");
        assertThat(toLocalDateTime(otherInfo.get("requestTimestamp"))).isCloseToUtcNow(within(1, ChronoUnit.SECONDS));
        assertThat(otherInfo.get("user-id").asText()).isEqualTo("user1234");
    }

    @Test
    void logOverrideAuditShouldSaveAuditEntryIfAlternativeUrlIsEmpty() {
        // given
        when(roleAssignmentUrlManager.getHost()).thenReturn("http://role-assignment.internal");
        when(securityUtils.getUserId()).thenReturn("user1234");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(
            "org.springframework.web.servlet.HandlerMapping.uriTemplateVariables",
            Map.of("id", "1234"));
        request.addHeader("Role-Assignment-Url", "");

        // when
        overrideAuditService.logOverrideAudit(request);

        // then
        verify(hearingStatusAuditRepository, times(1)).save(hearingStatusCaptor.capture());
        HearingStatusAuditEntity entity = hearingStatusCaptor.getValue();
        assertThat(entity.getHearingId()).isEqualTo("1234");
        JsonNode otherInfo = entity.getOtherInfo();
        assertThat(otherInfo.get("role-assignment-url").asText()).isEqualTo("");
        assertThat(toLocalDateTime(otherInfo.get("requestTimestamp"))).isCloseToUtcNow(within(1, ChronoUnit.SECONDS));
        assertThat(otherInfo.get("user-id").asText()).isEqualTo("user1234");
    }

    @Test
    void logOverrideAuditShouldSaveBody() {
        when(dataStoreUrlManager.getHost()).thenReturn("http://data-store.internal");
        when(securityUtils.getUserId()).thenReturn("user1234");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Data-Store-Url", "http://data-store.example.org");
        request.setRequestURI("/hearing");
        request.setMethod("POST");
        request.setContent("{\"body\":\"value\"}".getBytes(StandardCharsets.UTF_8));

        overrideAuditService.logOverrideAudit(request);

        verify(hearingStatusAuditRepository, times(1)).save(hearingStatusCaptor.capture());
        HearingStatusAuditEntity entity = hearingStatusCaptor.getValue();
        JsonNode otherInfo = entity.getOtherInfo();
        assertThat(otherInfo.get("role-assignment-url")).isNull();
        assertThat(otherInfo.get("data-store-url").asText()).isEqualTo("http://data-store.example.org");
        assertThat(otherInfo.get("user-id").asText()).isEqualTo("user1234");
        assertThat(otherInfo.get("requestBody").asText()).isEqualTo("{\"body\":\"value\"}");
    }

    @Test
    void logOverrideAuditShouldGetServiceName() {
        when(dataStoreUrlManager.getHost()).thenReturn("http://data-store.internal");
        when(securityUtils.getServiceNameFromS2SToken("Bearer " + DUMMY)).thenReturn("myServiceName");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Data-Store-Url", "http://data-store.example.org");
        request.addHeader("ServiceAuthorization", "Bearer " + DUMMY);

        overrideAuditService.logOverrideAudit(request);

        verify(hearingStatusAuditRepository, times(1)).save(hearingStatusCaptor.capture());
        HearingStatusAuditEntity entity = hearingStatusCaptor.getValue();
        assertThat(entity.getSource()).isEqualTo("myServiceName");
    }

    @Test
    void logOverrideAuditShouldCallLinkedHearingStatusRepositorySave() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        commonSetup();
        request.setRequestURI("/linkedHearingGroup/1234");
        request.setAttribute(
            "org.springframework.web.servlet.HandlerMapping.uriTemplateVariables",
            Map.of("id", "1234"));
        request.addHeader("Role-Assignment-Url", "http://role-assignment.example.org");
        request.addHeader("Data-Store-Url", "http://data-store.example.org");

        // when
        overrideAuditService.logOverrideAudit(request);

        // then
        verify(linkedHearingStatusAuditRepository, times(1))
            .save(any(LinkedHearingStatusAuditEntity.class));
    }

    @Test
    void logOverrideAuditShouldNotSaveIfAlternativeUrlsNotGiven() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(
            "org.springframework.web.servlet.HandlerMapping.uriTemplateVariables",
            Map.of("id", "1234"));
        overrideAuditService.logOverrideAudit(request);
        verify(hearingStatusAuditRepository, never()).save(any());
        verify(linkedHearingStatusAuditRepository, never()).save(any());
    }

    @Test
    void logOverrideAuditShouldNotSaveIfOneAlternativeUrlMatchDefault() {
        when(roleAssignmentUrlManager.getHost()).thenReturn("http://role-assignment.internal");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(
            "org.springframework.web.servlet.HandlerMapping.uriTemplateVariables",
            Map.of("id", "1234"));
        request.addHeader("Role-Assignment-Url", "http://role-assignment.internal");

        overrideAuditService.logOverrideAudit(request);
        verify(hearingStatusAuditRepository, never()).save(any());
        verify(linkedHearingStatusAuditRepository, never()).save(any());
    }


    @Test
    void logOverrideAuditShouldNotSaveIfBothAlternativeUrlsMatchDefault() {
        when(roleAssignmentUrlManager.getHost()).thenReturn("http://role-assignment.internal");
        when(dataStoreUrlManager.getHost()).thenReturn("http://data-store.internal");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(
            "org.springframework.web.servlet.HandlerMapping.uriTemplateVariables",
            Map.of("id", "1234"));
        request.addHeader("Role-Assignment-Url", "http://role-assignment.internal");
        request.addHeader("Data-Store-Url", "http://data-store.internal");

        overrideAuditService.logOverrideAudit(request);
        verify(hearingStatusAuditRepository, never()).save(any());
        verify(linkedHearingStatusAuditRepository, never()).save(any());
    }


    private void commonSetup() {
        when(roleAssignmentUrlManager.getHost()).thenReturn("http://role-assignment.internal");
        when(dataStoreUrlManager.getHost()).thenReturn("http://data-store.internal");
        when(securityUtils.getUserId()).thenReturn("user1234");
    }

    private LocalDateTime toLocalDateTime(JsonNode dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr.asText());
    }
}
