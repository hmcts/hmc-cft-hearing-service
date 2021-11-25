package uk.gov.hmcts.reform.hmc.client.datastore;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import uk.gov.hmcts.reform.hmc.data.SecurityUtils;

import static uk.gov.hmcts.reform.hmc.data.SecurityUtils.SERVICE_AUTHORIZATION;

public class SystemUserAuthHeadersInterceptor implements RequestInterceptor {

    private static final String EXPERIMENTAL = "experimental";

    private final SecurityUtils securityUtils;

    public SystemUserAuthHeadersInterceptor(SecurityUtils securityUtils) {
        this.securityUtils = securityUtils;
    }

    @Override
    public void apply(RequestTemplate template) {
        if (!template.headers().containsKey(SERVICE_AUTHORIZATION)) {
            template.header(SERVICE_AUTHORIZATION, securityUtils.getS2SToken());
        }
        // TODO: will be removed once ccd cleaned in their end
        template.header(EXPERIMENTAL, "true");
    }
}
