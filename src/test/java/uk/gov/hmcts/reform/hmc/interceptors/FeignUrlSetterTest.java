package uk.gov.hmcts.reform.hmc.interceptors;


import feign.RequestTemplate;
import feign.Target;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.hmc.config.DataStoreUrlManager;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeignUrlSetterTest {

    @Mock
    DataStoreUrlManager dataStoreUrlManager;

    @Mock
    RequestTemplate requestTemplate;

    @Mock
    Target target;

    @InjectMocks
    FeignUrlSetter feignUrlSetter;

    @Test
    @SuppressWarnings("unchecked")
    void applyShouldSetActualHost() {
        when(dataStoreUrlManager.getActualHost()).thenReturn("http://example.org");
        when(dataStoreUrlManager.getClientName()).thenReturn("ccd-data-store");
        when(target.name()).thenReturn("ccd-data-store");
        when(requestTemplate.feignTarget()).thenReturn(target);

        feignUrlSetter.apply(requestTemplate);

        verify(requestTemplate, times(1)).target("http://example.org");
    }
}
