package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentResource;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentResponse;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultRoleAssignmentRepositoryTest {

    private static final String USER_ID = "1234";
    private static final String URL_ROLE_ASSIGNMENTS = "http://role.assignments";

    private static final String HEADER_ETAG = "ETag";
    private static final String ETAG_VALUE = "\"ebygum\"";
    private static final String ETAG_VALUE_GZIP_SUFFIX = "\"ebygum--gzip\"";

    private static final String ROLE_NAME_1 = "test role 1";
    private static final String ROLE_NAME_2 = "test role 2";

    @Mock
    private ApplicationParams applicationParams;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private RestTemplate restTemplate;

    private DefaultRoleAssignmentRepository defaultRoleAssignmentRepository;

    @BeforeEach
    void setUp() {
        defaultRoleAssignmentRepository =
            new DefaultRoleAssignmentRepository(applicationParams, securityUtils, restTemplate);

        when(applicationParams.amGetRoleAssignmentsUrl()).thenReturn(URL_ROLE_ASSIGNMENTS);
    }

    @ParameterizedTest
    @ValueSource(strings = {ETAG_VALUE, ETAG_VALUE_GZIP_SUFFIX})
    void shouldGetRoleAssignmentsThenCachedRoleAssignments(String responseETag) {
        HttpHeaders authHeaders = createAuthHeaders();
        when(securityUtils.authorizationHeaders()).thenReturn(authHeaders);

        ResponseEntity<RoleAssignmentResponse> responseEntityOk = createResponseEntityOk(responseETag, ROLE_NAME_1);

        when(restTemplate.exchange(any(URI.class),
                                   eq(HttpMethod.GET),
                                   argThat(entity -> entity.getHeaders().getIfNoneMatch().isEmpty()),
                                   eq(RoleAssignmentResponse.class)))
            .thenReturn(responseEntityOk);

        ResponseEntity<RoleAssignmentResponse> responseEntityNotModified =
            createResponseEntityNotModified(responseETag);

        when(restTemplate.exchange(any(URI.class),
                                   eq(HttpMethod.GET),
                                   argThat(entity -> entity.getHeaders().getIfNoneMatch().contains(ETAG_VALUE)),
                                   eq(RoleAssignmentResponse.class)))
            .thenReturn(responseEntityNotModified);

        RoleAssignmentResponse resultFirst = defaultRoleAssignmentRepository.getRoleAssignments(USER_ID);

        // First request so no entries in map and therefore if-none-match header should be empty
        assertTrue(authHeaders.getIfNoneMatch().isEmpty(), "if-none-match header should be empty");
        assertRoleAssignments(resultFirst.getRoleAssignments(), ROLE_NAME_1);

        RoleAssignmentResponse resultSecond = defaultRoleAssignmentRepository.getRoleAssignments(USER_ID);

        // Second request so if-none-match header should contain ETag value returned by first
        assertIfNoneMatchHeaderHasETag(authHeaders.getIfNoneMatch(), ETAG_VALUE);
        assertRoleAssignments(resultSecond.getRoleAssignments(), ROLE_NAME_1);

        verify(securityUtils, times(2)).authorizationHeaders();
        verify(applicationParams, times(2)).amGetRoleAssignmentsUrl();
        verify(restTemplate, times(2))
            .exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(RoleAssignmentResponse.class));
    }

    @Test
    void shouldGetRoleAssignmentsThenUpdatedRoleAssignments() {
        HttpHeaders authHeaders = createAuthHeaders();
        when(securityUtils.authorizationHeaders()).thenReturn(authHeaders);

        ResponseEntity<RoleAssignmentResponse> responseEntityOkFirst = createResponseEntityOk(ETAG_VALUE, ROLE_NAME_1);

        when(restTemplate.exchange(any(URI.class),
                                   eq(HttpMethod.GET),
                                   argThat(entity -> entity.getHeaders().getIfNoneMatch().isEmpty()),
                                   eq(RoleAssignmentResponse.class)))
            .thenReturn(responseEntityOkFirst);

        ResponseEntity<RoleAssignmentResponse> responseEntityOkSecond = createResponseEntityOk(ETAG_VALUE, ROLE_NAME_2);

        when(restTemplate.exchange(any(URI.class),
                                   eq(HttpMethod.GET),
                                   argThat(entity -> entity.getHeaders().getIfNoneMatch().contains(ETAG_VALUE)),
                                   eq(RoleAssignmentResponse.class)))
            .thenReturn(responseEntityOkSecond);

        RoleAssignmentResponse resultFirst = defaultRoleAssignmentRepository.getRoleAssignments(USER_ID);

        assertTrue(authHeaders.getIfNoneMatch().isEmpty(), "if-none-match header should be empty");
        assertRoleAssignments(resultFirst.getRoleAssignments(), ROLE_NAME_1);

        RoleAssignmentResponse resultSecond = defaultRoleAssignmentRepository.getRoleAssignments(USER_ID);

        assertIfNoneMatchHeaderHasETag(authHeaders.getIfNoneMatch(), ETAG_VALUE);
        assertRoleAssignments(resultSecond.getRoleAssignments(), ROLE_NAME_2);

        verify(securityUtils, times(2)).authorizationHeaders();
        verify(applicationParams, times(2)).amGetRoleAssignmentsUrl();
        verify(restTemplate, times(2))
            .exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(RoleAssignmentResponse.class));
    }

    @Test
    void cacheShouldNotBeUpdatedIfNoETag() {
        HttpHeaders authHeaders = createAuthHeaders();
        when(securityUtils.authorizationHeaders()).thenReturn(authHeaders);

        ResponseEntity<RoleAssignmentResponse> responseEntityOk = createResponseEntityOk(ETAG_VALUE, ROLE_NAME_1);
        ResponseEntity<RoleAssignmentResponse> responseEntityOkNoETag = createResponseEntityOkNoETag(ROLE_NAME_2);
        ResponseEntity<RoleAssignmentResponse> responseEntityNotModified = createResponseEntityNotModified(ETAG_VALUE);

        when(restTemplate.exchange(any(URI.class),
                                   eq(HttpMethod.GET),
                                   any(HttpEntity.class),
                                   eq(RoleAssignmentResponse.class)))
            .thenReturn(responseEntityOk)
            .thenReturn(responseEntityOkNoETag)
            .thenReturn(responseEntityNotModified);

        RoleAssignmentResponse responseOk = defaultRoleAssignmentRepository.getRoleAssignments(USER_ID);
        assertRoleAssignments(responseOk.getRoleAssignments(), ROLE_NAME_1);

        RoleAssignmentResponse responseOkNoETag = defaultRoleAssignmentRepository.getRoleAssignments(USER_ID);
        assertRoleAssignments(responseOkNoETag.getRoleAssignments(), ROLE_NAME_2);

        RoleAssignmentResponse responseNotModified = defaultRoleAssignmentRepository.getRoleAssignments(USER_ID);
        assertRoleAssignments(responseNotModified.getRoleAssignments(), ROLE_NAME_1);

        verify(securityUtils, times(3)).authorizationHeaders();
        verify(applicationParams, times(3)).amGetRoleAssignmentsUrl();
        verify(restTemplate, times(3))
            .exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(RoleAssignmentResponse.class));
    }

    @Test
    void shouldReturnNullRoleAssignmentResponseWhenNotModifiedResponseNotInCache() {
        HttpHeaders authHeaders = createAuthHeaders();
        when(securityUtils.authorizationHeaders()).thenReturn(authHeaders);

        ResponseEntity<RoleAssignmentResponse> responseEntity = createResponseEntityNotModified(ETAG_VALUE);

        when(restTemplate.exchange(any(URI.class),
                                   eq(HttpMethod.GET),
                                   argThat(entity -> entity.getHeaders().getIfNoneMatch().isEmpty()),
                                   eq(RoleAssignmentResponse.class)))
            .thenReturn(responseEntity);

        RoleAssignmentResponse result = defaultRoleAssignmentRepository.getRoleAssignments(USER_ID);

        assertNull(result, "RoleAssignmentResponse should be null");

        verify(securityUtils).authorizationHeaders();
        verify(applicationParams).amGetRoleAssignmentsUrl();
        verify(restTemplate)
            .exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(RoleAssignmentResponse.class));
    }

    @Test
    void shouldReturnNullRoleAssignmentResponseWhenOkResponseBodyNull() {
        HttpHeaders authHeaders = createAuthHeaders();
        when(securityUtils.authorizationHeaders()).thenReturn(authHeaders);

        ResponseEntity<RoleAssignmentResponse> responseEntity = createResponseEntityOkNullBody();

        when(restTemplate.exchange(any(URI.class),
                                   eq(HttpMethod.GET),
                                   any(HttpEntity.class),
                                   eq(RoleAssignmentResponse.class)))
            .thenReturn(responseEntity);

        RoleAssignmentResponse result = defaultRoleAssignmentRepository.getRoleAssignments(USER_ID);

        assertNull(result, "RoleAssignmentResponse should be null");

        verify(securityUtils).authorizationHeaders();
        verify(applicationParams).amGetRoleAssignmentsUrl();
        verify(restTemplate)
            .exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(RoleAssignmentResponse.class));
    }

    @Test
    void shouldReturnNullRoleAssignmentsWhenOkResponseRoleAssignmentsNull() {
        HttpHeaders authHeaders = createAuthHeaders();
        when(securityUtils.authorizationHeaders()).thenReturn(authHeaders);

        RoleAssignmentResponse roleAssignmentResponse = new RoleAssignmentResponse();
        ResponseEntity<RoleAssignmentResponse> responseEntity = createResponseEntityOk(roleAssignmentResponse);

        when(restTemplate.exchange(any(URI.class),
                                   eq(HttpMethod.GET),
                                   any(HttpEntity.class),
                                   eq(RoleAssignmentResponse.class)))
            .thenReturn(responseEntity);

        RoleAssignmentResponse result = defaultRoleAssignmentRepository.getRoleAssignments(USER_ID);

        assertNotNull(result, "RoleAssignmentResponse should not be null");
        assertNull(result.getRoleAssignments(), "RoleAssignments should be null");

        verify(securityUtils).authorizationHeaders();
        verify(applicationParams).amGetRoleAssignmentsUrl();
        verify(restTemplate)
            .exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(RoleAssignmentResponse.class));
    }

    @Test
    void shouldReturnEmptyRoleAssignmentsWhenOkResponseRoleAssignmentsEmpty() {
        HttpHeaders authHeaders = createAuthHeaders();
        when(securityUtils.authorizationHeaders()).thenReturn(authHeaders);

        RoleAssignmentResponse roleAssignmentResponse = new RoleAssignmentResponse();
        roleAssignmentResponse.setRoleAssignments(Collections.emptyList());

        ResponseEntity<RoleAssignmentResponse> responseEntity = createResponseEntityOk(roleAssignmentResponse);

        when(restTemplate.exchange(any(URI.class),
                                   eq(HttpMethod.GET),
                                   any(HttpEntity.class),
                                   eq(RoleAssignmentResponse.class)))
            .thenReturn(responseEntity);

        RoleAssignmentResponse result = defaultRoleAssignmentRepository.getRoleAssignments(USER_ID);

        assertNotNull(result, "RoleAssignmentResponse should not be null");

        List<RoleAssignmentResource> roleAssignments = result.getRoleAssignments();
        assertNotNull(roleAssignments, "RoleAssignments should not be null");
        assertTrue(roleAssignments.isEmpty(), "RoleAssignments should be empty");

        verify(securityUtils).authorizationHeaders();
        verify(applicationParams).amGetRoleAssignmentsUrl();
        verify(restTemplate)
            .exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(RoleAssignmentResponse.class));
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders authHeaders = new HttpHeaders();

        authHeaders.add("ServiceAuthorization", "test-token");
        authHeaders.add("user-id", USER_ID);
        authHeaders.add("user-roles", "test-role");

        return authHeaders;
    }

    private HttpHeaders createResponseHeaders() {
        return createResponseHeaders(ETAG_VALUE);
    }

    private HttpHeaders createResponseHeaders(String responseETag) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HEADER_ETAG, responseETag);

        return responseHeaders;
    }

    private ResponseEntity<RoleAssignmentResponse> createResponseEntityOk(String responseETag, String roleName) {
        HttpHeaders responseHeaders = createResponseHeaders(responseETag);

        RoleAssignmentResource roleAssignmentResource = new RoleAssignmentResource();
        roleAssignmentResource.setRoleName(roleName);

        RoleAssignmentResponse roleAssignmentResponse = new RoleAssignmentResponse();
        roleAssignmentResponse.setRoleAssignments(List.of(roleAssignmentResource));

        return new ResponseEntity<>(roleAssignmentResponse, responseHeaders, HttpStatus.OK);
    }

    private ResponseEntity<RoleAssignmentResponse> createResponseEntityOk(
        RoleAssignmentResponse roleAssignmentResponse) {
        HttpHeaders responseHeaders = createResponseHeaders();
        return new ResponseEntity<>(roleAssignmentResponse, responseHeaders, HttpStatus.OK);
    }

    private ResponseEntity<RoleAssignmentResponse> createResponseEntityOkNullBody() {
        HttpHeaders responseHeaders = createResponseHeaders();
        return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
    }

    private ResponseEntity<RoleAssignmentResponse> createResponseEntityOkNoETag(String roleName) {
        HttpHeaders responseHeaders = new HttpHeaders();

        RoleAssignmentResource roleAssignmentResource = new RoleAssignmentResource();
        roleAssignmentResource.setRoleName(roleName);

        RoleAssignmentResponse roleAssignmentResponse = new RoleAssignmentResponse();
        roleAssignmentResponse.setRoleAssignments(List.of(roleAssignmentResource));

        return new ResponseEntity<>(roleAssignmentResponse, responseHeaders, HttpStatus.OK);
    }

    private ResponseEntity<RoleAssignmentResponse> createResponseEntityNotModified(String responseETag) {
        HttpHeaders responseHeaders = createResponseHeaders(responseETag);
        return new ResponseEntity<>(responseHeaders, HttpStatus.NOT_MODIFIED);
    }

    private void assertRoleAssignments(List<RoleAssignmentResource> roleAssignments, String expectedRoleName) {
        assertEquals(1, roleAssignments.size(), "Unexpected number of role assignments");
        assertEquals(expectedRoleName, roleAssignments.getFirst().getRoleName(),
                     "Role assignments does not contain expected role name");
    }

    private void assertIfNoneMatchHeaderHasETag(List<String> ifNoneMatchList, String expectedETag) {
        assertEquals(1, ifNoneMatchList.size(), "if-none-match header has unexpected number of items");
        assertEquals(expectedETag, ifNoneMatchList.getFirst(), "if-none-match header does not contain expected ETag");
    }
}
