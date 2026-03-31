package uk.gov.hmcts.reform.hmc.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.config.UrlManager;
import uk.gov.hmcts.reform.hmc.service.common.OverrideAuditService;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@Slf4j
@RequiredArgsConstructor
public class HeaderProcessor implements HandlerInterceptor {

    private final ApplicationParams params;
    private final UrlManager roleAssignmentUrlManager;
    private final UrlManager dataStoreUrlManager;
    private final OverrideAuditService overrideAuditService;
    private final OverrideHostPolicy overrideHostPolicy;

    /**
     * Check if role assignment and/or ccd data store url headers are present in the request.
     * If any of the headers are present, set the actual host to the value of the header.
     * @param request current HTTP request
     * @param response current HTTP response
     * @param handler chosen handler to execute, for type and/or instance evaluation
     * @return boolean
     * @throws Exception – in case of errors
     */
    @Override
    public boolean preHandle(
        HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (params.isHmctsDeploymentIdEnabled()) {
            overrideAuditService.logOverrideAudit(request);
            processHeader(request, roleAssignmentUrlManager);
            processHeader(request, dataStoreUrlManager);
        } else {
            roleAssignmentUrlManager.setActualHost(roleAssignmentUrlManager.getHost());
            dataStoreUrlManager.setActualHost(dataStoreUrlManager.getHost());
        }

        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    private void processHeader(HttpServletRequest request, UrlManager urlManager) {
        String url = request.getHeader(urlManager.getUrlHeaderName());
        if (isNotBlank(url) && overrideHostPolicy.isAllowed(url)) {
            urlManager.setActualHost(url);
        } else {
            if (isNotBlank(url)) {
                log.warn("Rejected override url for header {}: {}", urlManager.getUrlHeaderName(), url);
            }
            urlManager.setActualHost(urlManager.getHost());
        }
    }
}
