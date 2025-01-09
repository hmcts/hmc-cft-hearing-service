package uk.gov.hmcts.reform.hmc;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.jsonwebtoken.Jwts;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.cloud.contract.wiremock.WireMockConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.hmc.config.MessageSenderToTopicConfiguration;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentAttributesResource;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentResource;
import uk.gov.hmcts.reform.hmc.data.RoleAssignmentResponse;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository;
import uk.gov.hmcts.reform.hmc.repository.RoleAssignmentRepository;
import uk.gov.hmcts.reform.hmc.wiremock.extensions.DynamicOAuthJwkSetResponseTransformer;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubReturn200RoleAssignments;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.HEARING_MANAGER;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.HEARING_VIEWER;
import static uk.gov.hmcts.reform.hmc.service.AccessControlServiceImpl.LISTED_HEARING_VIEWER;

@SpringBootTest(classes = {
    Application.class,
    TestIdamConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureWireMock(port = 0, stubs = "classpath:/wiremock-stubs")
@ActiveProfiles("itest")
public class BaseTest {

    @Inject
    @Qualifier(DefaultRoleAssignmentRepository.QUALIFIER)
    protected RoleAssignmentRepository roleAssignmentRepository;
    @Inject
    protected SecurityUtils securityUtils;
    @Inject
    protected ApplicationParams applicationParams;
    @MockBean
    protected MessageSenderToTopicConfiguration messageSenderToTopicConfiguration;

    @Value("${wiremock.server.port}")
    protected Integer wiremockPort;
    @Mock
    protected Authentication authentication;

    @BeforeEach
    void init() {
        final String hostUrl = "http://localhost:" + wiremockPort;
        ReflectionTestUtils.setField(roleAssignmentRepository, "securityUtils", securityUtils);

        Jwt jwt = dummyJwt();
        when(authentication.getPrincipal()).thenReturn(jwt);
        SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
        ReflectionTestUtils.setField(applicationParams, "roleAssignmentServiceHost", hostUrl);
        ReflectionTestUtils.setField(applicationParams, "hmctsDeploymentIdEnabled", false);

        stubRoleAssignments();
        stubFor(WireMock.get(urlMatching("/cases/.*"))
                    .willReturn(okJson("{\n"
                                           + "\t\"jurisdiction\": \"Jurisdiction1\",\n"
                                           + "\t\"case_type\": \"CaseType1\"\n"
                                           + "}")));
        
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Configuration
    static class WireMockTestConfiguration {
        @Bean
        public WireMockConfigurationCustomizer wireMockConfigurationCustomizer() {
            return config -> config.extensions(new WiremockFixtures.ConnectionClosedTransformer(),
                                               new DynamicOAuthJwkSetResponseTransformer());
        }
    }

    private Jwt dummyJwt() {
        return Jwt.withTokenValue("a dummy jwt token")
            .claim("aClaim", "aClaim")
            .header("aHeader", "aHeader")
            .build();
    }

    private void stubRoleAssignments() {
        RoleAssignmentResponse response = new RoleAssignmentResponse();
        response.setRoleAssignments(List.of(
            stubGenericRoleAssignment(HEARING_MANAGER),
            stubGenericRoleAssignment(HEARING_VIEWER),
            stubGenericRoleAssignment(LISTED_HEARING_VIEWER)
        ));
        stubReturn200RoleAssignments(".*", response);
    }

    private RoleAssignmentResource stubGenericRoleAssignment(String roleName) {
        RoleAssignmentResource resource = new RoleAssignmentResource();
        resource.setRoleName(roleName);
        resource.setRoleType("ORGANISATION");
        resource.setAttributes(new RoleAssignmentAttributesResource());
        return resource;
    }

    public static String generateDummyS2SToken(String serviceName) {
        return Jwts.builder()
            .subject(serviceName)
            .issuedAt(new Date())
            .signWith(Jwts.SIG.HS256.key().build(), Jwts.SIG.HS256)
            .compact();
    }
}
