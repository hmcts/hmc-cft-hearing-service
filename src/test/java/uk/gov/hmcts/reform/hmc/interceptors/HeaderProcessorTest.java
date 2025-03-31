package uk.gov.hmcts.reform.hmc.interceptors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.config.UrlManager;
import uk.gov.hmcts.reform.hmc.service.common.OverrideAuditService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
class HeaderProcessorTest {

    @Mock
    ApplicationParams params;

    @Mock
    private UrlManager dataStoreUrlManager;

    @Mock
    private OverrideAuditService overrideAuditService;

    @Mock
    private UrlManager roleAssignmentUrlManager;

    HeaderProcessor headerProcessor;

    @BeforeEach
    void setUp() {
        openMocks(this);
        headerProcessor = new HeaderProcessor(
            params, roleAssignmentUrlManager, dataStoreUrlManager, overrideAuditService);
    }

    @Test
    void preHandleShouldCallHandlers() throws Exception {
        when(params.isHmctsDeploymentIdEnabled()).thenReturn(true);
        when(dataStoreUrlManager.getUrlHeaderName()).thenReturn("dataStoreUrl");
        when(roleAssignmentUrlManager.getUrlHeaderName()).thenReturn("roleAssignmentUrl");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("roleAssignmentUrl", "roleAssignmentUrlValue");
        request.addHeader("dataStoreUrl", "dataStoreUrlValue");

        headerProcessor.preHandle(request, null, null);

        verify(roleAssignmentUrlManager, times(1)).setActualHost("roleAssignmentUrlValue");
        verify(dataStoreUrlManager, times(1)).setActualHost("dataStoreUrlValue");
    }

    @Test
    void preHandleShouldCallHandlersWithDefaultIfHeaderNotPresent() throws Exception {
        when(params.isHmctsDeploymentIdEnabled()).thenReturn(true);
        when(dataStoreUrlManager.getUrlHeaderName()).thenReturn("dataStoreUrl");
        when(roleAssignmentUrlManager.getUrlHeaderName()).thenReturn("roleAssignmentUrl");
        when(roleAssignmentUrlManager.getHost()).thenReturn("roleAssignmentDefaultHost");
        when(dataStoreUrlManager.getHost()).thenReturn("dataStoreDefaultHost");
        MockHttpServletRequest request = new MockHttpServletRequest();

        headerProcessor.preHandle(request, null, null);

        verify(roleAssignmentUrlManager, times(0)).setActualHost("roleAssignmentUrlValue");
        verify(dataStoreUrlManager, times(0)).setActualHost("dataStoreUrlValue");
        verify(roleAssignmentUrlManager, times(1)).setActualHost("roleAssignmentDefaultHost");
        verify(dataStoreUrlManager, times(1)).setActualHost("dataStoreDefaultHost");
    }

    @Test
    void preHandleShouldNotProcessHeadersIfHmctsDeploymentIdIsDisabled() throws Exception {
        when(params.isHmctsDeploymentIdEnabled()).thenReturn(false);
        when(roleAssignmentUrlManager.getHost()).thenReturn("http://example.org");
        when(dataStoreUrlManager.getHost()).thenReturn("http://example.org");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("roleAssignmentUrl", "roleAssignmentUrlValue");
        request.addHeader("dataStoreUrl", "dataStoreUrlValue");


        headerProcessor.preHandle(request, null, null);

        verify(roleAssignmentUrlManager, times(1)).getHost();
        verify(dataStoreUrlManager, times(1)).getHost();

        verify(roleAssignmentUrlManager, times(1)).setActualHost("http://example.org");
        verify(dataStoreUrlManager, times(1)).setActualHost("http://example.org");

    }
}
