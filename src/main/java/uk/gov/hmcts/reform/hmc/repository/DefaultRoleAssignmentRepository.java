package uk.gov.hmcts.reform.hmc.repository;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentResource;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentResponse;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.ServiceException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.microsoft.applicationinsights.core.dependencies.google.common.collect.Maps.newHashMap;
import static org.springframework.http.HttpHeaders.ETAG;

@Slf4j
@Repository
@Qualifier(DefaultRoleAssignmentRepository.QUALIFIER)
public class DefaultRoleAssignmentRepository implements RoleAssignmentRepository {
    public static final String QUALIFIER = "default";
    public static final String ROLE_ASSIGNMENTS_NOT_FOUND =
        "No Role Assignments found for userId=%s when getting from Role Assignment Service";
    public static final String ROLE_ASSIGNMENTS_CLIENT_ERROR =
        "Client error when %s Role Assignments from Role Assignment Service because of %s";
    public static final String ROLE_ASSIGNMENT_SERVICE_ERROR =
        "Problem %s Role Assignments from Role Assignment Service because of %s";
    public static final String ROLE_ASSIGNMENT_INVALID_ROLE =
        "User requires a Role Assignment with a valid organisational hearing role";
    public static final String ROLE_ASSIGNMENT_INVALID_ATTRIBUTES =
        "User requires a Role Assignment with attributes matching the case's jurisdiction or case type, if present";
    public static final String ROLE_ASSIGNMENT_MISSING_REQUIRED = "Required Role Assignments are missing for Hearing";

    private static final String GZIP_POSTFIX = "--gzip";

    private final ApplicationParams applicationParams;
    private final SecurityUtils securityUtils;
    private final RestTemplate restTemplate;

    private final Map<String, Pair<String, RoleAssignmentResponse>> roleAssignments = newHashMap();

    public DefaultRoleAssignmentRepository(final ApplicationParams applicationParams,
                                           final SecurityUtils securityUtils,
                                           @Qualifier("restTemplate") final RestTemplate restTemplate) {
        this.applicationParams = applicationParams;
        this.securityUtils = securityUtils;
        this.restTemplate = restTemplate;
    }

    @Override
    public RoleAssignmentResponse getRoleAssignments(String userId) {
        try {
            HttpHeaders headers = securityUtils.authorizationHeaders();
            addETagHeader(userId, headers);

            final HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

            return getRoleAssignmentResponse(userId, requestEntity);
        } catch (Exception e) {
            log.warn("Error while retrieving Role Assignments", e);
            throw mapException(e, "getting");
        }
    }

    private void addETagHeader(String userId, HttpHeaders headers) {
        if (roleAssignments.containsKey(userId)) {
            Pair<String, RoleAssignmentResponse> stringRoleAssignmentResponsePair = roleAssignments.get(userId);
            headers.setIfNoneMatch(stringRoleAssignmentResponsePair.getKey());
        }
    }

    private RoleAssignmentResponse getRoleAssignmentResponse(String userId, HttpEntity<Object> requestEntity)
        throws URISyntaxException {

        ResponseEntity<RoleAssignmentResponse> exchange = exchangeGet(userId, requestEntity);
        log.debug("GET RoleAssignments for user={} returned response status={}", userId, exchange.getStatusCode());

        if (exchange.getStatusCode() == HttpStatus.NOT_MODIFIED && roleAssignments.containsKey(userId)) {
            return roleAssignments.get(userId).getRight();
        }
        if (exchange.getHeaders().containsKey(ETAG) && exchange.getHeaders().getETag() != null) {
            log.debug("GET RoleAssignments response contains header ETag={}", exchange.getHeaders().getETag());
            if (thereAreRoleAssignmentsInTheBody(exchange)) {
                roleAssignments.put(userId, Pair.of(getETag(exchange.getHeaders().getETag()), exchange.getBody()));
            }
        }

        return exchange.getBody();
    }

    private ResponseEntity<RoleAssignmentResponse> exchangeGet(String userId, HttpEntity<Object> requestEntity)
        throws URISyntaxException {
        final Map<String, String> queryParams = new HashMap<>();
        queryParams.put("uid", ApplicationParams.encode(userId.toLowerCase()));

        final String encodedUrl = UriComponentsBuilder.fromHttpUrl(applicationParams.amGetRoleAssignmentsUrl())
            .buildAndExpand(queryParams).toUriString();

        return restTemplate.exchange(new URI(encodedUrl),
                                     HttpMethod.GET, requestEntity,
                                     RoleAssignmentResponse.class);
    }

    private boolean thereAreRoleAssignmentsInTheBody(ResponseEntity<RoleAssignmentResponse> exchange) {
        RoleAssignmentResponse body = exchange.getBody();
        if (body == null) {
            return false;
        }

        List<RoleAssignmentResource> roleAssignments = body.getRoleAssignments();
        if (roleAssignments == null) {
            return false;
        }

        return !roleAssignments.isEmpty();
    }

    private String getETag(String etag) {
        if (etag != null && etag.endsWith(GZIP_POSTFIX + "\"")) {
            return etag.substring(0, etag.length() - GZIP_POSTFIX.length() - 1) + "\"";
        }
        return etag;
    }

    private RuntimeException mapException(Exception exception, String processDescription) {

        if (exception instanceof HttpClientErrorException
            && HttpStatus.valueOf(((HttpClientErrorException) exception).getRawStatusCode()).is4xxClientError()) {
            return new BadRequestException(
                String.format(ROLE_ASSIGNMENTS_CLIENT_ERROR, processDescription, exception.getMessage()));
        } else {
            return new ServiceException(
                String.format(ROLE_ASSIGNMENT_SERVICE_ERROR, processDescription, exception.getMessage()));
        }
    }
}
