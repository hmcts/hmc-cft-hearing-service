package uk.gov.hmcts.reform.hmc.security.idam;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

@Component
@Slf4j
public class IdamRepository {

    private final IdamClient idamClient;
    private final ApplicationParams appParams;

    @Autowired
    public IdamRepository(IdamClient idamClient, ApplicationParams applicationParams) {
        this.idamClient = idamClient;
        this.appParams = applicationParams;
    }

    @Cacheable(value = "userInfoCache")
    public UserInfo getUserInfo(String jwtToken) {
        UserInfo userInfo = idamClient.getUserInfo("Bearer " + jwtToken);
        if (userInfo != null) {
            log.info("Queried user info from IDAM API. User Id={}. Roles={}.", userInfo.getUid(), userInfo.getRoles());
        }
        return userInfo;
    }
}
