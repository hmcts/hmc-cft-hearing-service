package uk.gov.hmcts.reform.hmc.service.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.config.UrlManager;
import uk.gov.hmcts.reform.hmc.data.HearingStatusAuditEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingStatusAuditEntity;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.repository.HearingStatusAuditRepository;
import uk.gov.hmcts.reform.hmc.repository.LinkedHearingStatusAuditRepository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

import static uk.gov.hmcts.reform.hmc.constants.Constants.FH;
import static uk.gov.hmcts.reform.hmc.constants.Constants.OVERRIDE_URL;
import static uk.gov.hmcts.reform.hmc.constants.Constants.OVERRIDE_URL_EVENT;
import static uk.gov.hmcts.reform.hmc.data.SecurityUtils.SERVICE_AUTHORIZATION;

@Service
@RequiredArgsConstructor
@Slf4j
public class OverrideAuditService {

    private static final String PARAM_ATTRIBUTE =
        "org.springframework.web.servlet.HandlerMapping.uriTemplateVariables";

    private final HearingStatusAuditRepository hearingStatusAuditRepository;
    private final LinkedHearingStatusAuditRepository linkedHearingStatusAuditRepository;
    private final UrlManager roleAssignmentUrlManager;
    private final UrlManager dataStoreUrlManager;

    // To overcome circular dependency issue
    @Lazy @Autowired @Setter
    private SecurityUtils securityUtils;

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

        if (isRequestWithBody(request)) {
            root.put("requestBody", getRequestBody(request));
        }

        root.put("requestTimestamp", LocalDateTime.now(ZoneId.of("UTC")).toString());
        putUserId(root);

        String serviceName = getServiceName(request.getHeader(SERVICE_AUTHORIZATION));
        if (path.startsWith("/linkedHearingGroup")) {
            String groupId = getAttributeId(request, "id");
            saveLinkedHearingStatusAudit(groupId, serviceName, root);
        } else {
            String hearingId = getAttributeId(request, "id");
            saveHearingStatusAudit(hearingId, serviceName, root);
        }
    }

    private void putUserId(ObjectNode root) {
        try {
            root.put("user-id", securityUtils.getUserId());
        } catch (Exception e) {
            log.warn("Error getting user id", e);
            root.put("user-id", "n/a");
        }
    }

    private void saveLinkedHearingStatusAudit(String groupId, String serviceName, ObjectNode root) {
        LinkedHearingStatusAuditEntity auditEntity = new LinkedHearingStatusAuditEntity();
        auditEntity.setHmctsServiceId("n/a");
        auditEntity.setLinkedGroupId(groupId);
        auditEntity.setLinkedGroupVersion("n/a");
        auditEntity.setLinkedHearingEvent(OVERRIDE_URL_EVENT);
        auditEntity.setSource(serviceName);
        auditEntity.setTarget(FH);
        auditEntity.setOtherInfo(root);
        linkedHearingStatusAuditRepository.save(auditEntity);
    }

    private void saveHearingStatusAudit(String hearingId, String serviceName, ObjectNode root) {
        HearingStatusAuditEntity auditEntity = new HearingStatusAuditEntity();
        auditEntity.setHearingId(hearingId);
        auditEntity.setHmctsServiceId("n/a");
        auditEntity.setStatus(OVERRIDE_URL);
        auditEntity.setHearingEvent(OVERRIDE_URL_EVENT);
        auditEntity.setRequestVersion("n/a");
        auditEntity.setSource(serviceName);
        auditEntity.setTarget(FH);
        auditEntity.setOtherInfo(root);
        hearingStatusAuditRepository.save(auditEntity);
    }

    private String getRequestBody(HttpServletRequest request) {
        try {
            InputStream is = request.getInputStream();
            byte[] body = IOUtils.toByteArray(is);
            return new String(body, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("Error reading request body", e);
        }
        return "Failed to read request body";
    }

    private boolean isRequestWithBody(HttpServletRequest request) {
        return "post".equalsIgnoreCase(request.getMethod()) || "put".equalsIgnoreCase(request.getMethod());
    }

    /**
     * Assumption that log is required only if one or both custom urls are provided and
     * they are different from the default ones. If header given as an empty string, the
     * app will consider it as a default value, however log entry will be created.
     * @param roleAssignmentUrl - custom role assignment url
     * @param dataStoreUrl - custom data store url
     * @return whether log is required
     */
    private boolean isLogRequired(String roleAssignmentUrl, String dataStoreUrl) {
        boolean raUrlDefault = roleAssignmentUrl == null
            || roleAssignmentUrlManager.getHost().equals(roleAssignmentUrl);

        boolean dataStoreUrlDefault = dataStoreUrl == null
            || dataStoreUrlManager.getHost().equals(dataStoreUrl);

        // Check if any of the urls are different from the default ones
        return !raUrlDefault || !dataStoreUrlDefault;
    }

    private String getServiceName(String s2sToken) {
        if (s2sToken != null) {
            try {
                return securityUtils.getServiceNameFromS2SToken(s2sToken);
            } catch (Exception e) {
                log.warn("Error decoding S2S token", e);
                return "n/a";
            }
        }
        log.warn("Missing S2S token");
        return "n/a";
    }

    private String getAttributeId(HttpServletRequest request, String attribute) {
        try {
            Map<String, Object> attributes =  ((Map<String, Object>) request.getAttribute(PARAM_ATTRIBUTE));
            if (attributes != null && attributes.containsKey(attribute)) {
                return attributes.get(attribute).toString();
            }
        } catch (ClassCastException e) {
            log.warn("Error casting request attribute", e);
        }
        return "n/a";
    }
}
