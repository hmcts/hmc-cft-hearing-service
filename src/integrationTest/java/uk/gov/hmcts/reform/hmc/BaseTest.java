package uk.gov.hmcts.reform.hmc;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;
import uk.gov.hmcts.reform.hmc.repository.DefaultRoleAssignmentRepository;
import uk.gov.hmcts.reform.hmc.repository.RoleAssignmentRepository;

import javax.inject.Inject;

import static org.mockito.Mockito.when;

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
    }

    private Jwt dummyJwt() {
        return Jwt.withTokenValue("a dummy jwt token")
            .claim("aClaim", "aClaim")
            .header("aHeader", "aHeader")
            .build();
    }
}
