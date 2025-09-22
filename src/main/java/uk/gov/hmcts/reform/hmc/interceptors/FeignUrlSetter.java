package uk.gov.hmcts.reform.hmc.interceptors;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.config.DataStoreUrlManager;

@Component
@RequiredArgsConstructor
public class FeignUrlSetter implements RequestInterceptor {

    private final DataStoreUrlManager dataStoreUrlManager;

    @Override
    public void apply(RequestTemplate template) {

        if (dataStoreUrlManager.getClientName().equals(template.feignTarget().name())) {
            template.target(dataStoreUrlManager.getActualHost());
        }
    }
}
