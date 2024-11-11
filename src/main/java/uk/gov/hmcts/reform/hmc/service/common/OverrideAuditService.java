package uk.gov.hmcts.reform.hmc.service.common;

import com.auth0.jwt.JWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.config.UrlManager;
import uk.gov.hmcts.reform.hmc.data.HearingStatusAuditEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingStatusAuditEntity;
import uk.gov.hmcts.reform.hmc.repository.HearingStatusAuditRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingStatusAuditRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.data.util.CastUtils.cast;
import static uk.gov.hmcts.reform.hmc.constants.Constants.OVERRIDE_URL;
import static uk.gov.hmcts.reform.hmc.constants.Constants.OVERRIDE_URL_EVENT;
import static uk.gov.hmcts.reform.hmc.data.SecurityUtils.SERVICE_AUTHORIZATION;

@Service
@RequiredArgsConstructor
public class OverrideAuditService {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String PARAM_ATTRIBUTE =
        "org.springframework.web.servlet.HandlerMapping.uriTemplateVariables";

    private final HearingStatusAuditRepository hearingStatusAuditRepository;
    private final LinkedHearingStatusAuditRepository linkedHearingStatusAuditRepository;
    private final Clock utcClock;
    private final UrlManager roleAssignmentUrlManager;
    private final UrlManager dataStoreUrlManager;

    public void logOverrideAudit(HttpServletRequest request) {
        String roleAssignmentUrl = request.getHeader(roleAssignmentUrlManager.getUrlHeaderName());
        String dataStoreUrl = request.getHeader(dataStoreUrlManager.getUrlHeaderName());

        if (!isLogRequired(roleAssignmentUrl, dataStoreUrl)) {
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        String path = request.getRequestURI();
        root.put("path", path);

        if (roleAssignmentUrl != null && !roleAssignmentUrlManager.getHost().equals(roleAssignmentUrl)) {
            root.put("role-assignment-url", roleAssignmentUrl);
        }

        if (dataStoreUrl != null && !dataStoreUrlManager.getHost().equals(dataStoreUrl)) {
            root.put("data-store-url", dataStoreUrl);
        }

        String s2sToken = request.getHeader(SERVICE_AUTHORIZATION);
        root.put("hmctsServiceName", getServiceNameFromS2SToken(s2sToken));

        if (isRequestWithBody(request)) {
            root.put("requestBody", getRequestBody(request));
        }

        if (path.startsWith("/linkedHearingGroup")) {
            saveLinkedHearingStatusAudit(root);
        } else {
            String hearingId = getHearingId(request);
            saveHearingStatusAudit(hearingId, root);
        }
    }

    private void saveLinkedHearingStatusAudit(ObjectNode root) {
        LinkedHearingStatusAuditEntity auditEntity = new LinkedHearingStatusAuditEntity();
        auditEntity.setHmctsServiceId("n/a");
        auditEntity.setLinkedGroupId("n/a");
        auditEntity.setLinkedGroupVersion("n/a");
        auditEntity.setLinkedHearingEvent("n/a");
        auditEntity.setOtherInfo(root);
        linkedHearingStatusAuditRepository.save(auditEntity);
    }

    private void saveHearingStatusAudit(String hearingId, ObjectNode root) {
        HearingStatusAuditEntity auditEntity = new HearingStatusAuditEntity();
        auditEntity.setHearingId(hearingId);
        auditEntity.setHmctsServiceId("n/a");
        auditEntity.setStatus(OVERRIDE_URL);
        auditEntity.setHearingEvent(OVERRIDE_URL_EVENT);
        auditEntity.setRequestVersion("n/a");
        auditEntity.setResponseDateTime(now());
        auditEntity.setOtherInfo(root);
        hearingStatusAuditRepository.save(auditEntity);
    }

    private String getRequestBody(HttpServletRequest request) {
        try (BufferedReader reader = request.getReader()) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException ioe) {
            return "{\"requestBody\": \"Error reading request body\"}";
        }
    }

    private boolean isRequestWithBody(HttpServletRequest request) {
        return "post".equalsIgnoreCase(request.getMethod()) || "put".equalsIgnoreCase(request.getMethod());
    }

    /**
     * Assumption that log is required only if one or both custom urls are provided and
     * they are different from the default ones.
     * @param roleAssignmentUrl - custom role assignment url
     * @param dataStoreUrl - custom data store url
     * @return whether log is required
     */
    private boolean isLogRequired(String roleAssignmentUrl, String dataStoreUrl) {

        // No custom urls provided
        if (roleAssignmentUrl == null && dataStoreUrl == null) {
            return false;
        }

        // Check if any of the urls are different from the default ones
        return !roleAssignmentUrlManager.getHost().equals(roleAssignmentUrl)
            || !dataStoreUrlManager.getHost().equals(dataStoreUrl);
    }

    private String getServiceNameFromS2SToken(String s2sToken) {
        // NB: this grabs the servce name straight from the token under the assumption
        // that the S2S token has already been verified elsewhere

        if (s2sToken != null && s2sToken.startsWith(BEARER_PREFIX)) {
            return JWT.decode(s2sToken.substring(BEARER_PREFIX.length())).getSubject();
        }
        return null;
    }

    private String getHearingId(HttpServletRequest request) {
        Map<String, Object> attributes = cast(request.getAttribute(PARAM_ATTRIBUTE));
        if (attributes.containsKey("id")) {
            return attributes.get("id").toString();
        }
        return "n/a";
    }

    private LocalDateTime now() {
        return LocalDateTime.now(utcClock);
    }
}
