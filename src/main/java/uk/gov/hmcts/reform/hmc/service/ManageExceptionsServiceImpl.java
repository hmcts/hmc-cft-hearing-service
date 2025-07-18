package uk.gov.hmcts.reform.hmc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.model.ManageExceptionsResponse;
import uk.gov.hmcts.reform.hmc.model.SupportRequests;

@Service
@Slf4j
public class ManageExceptionsServiceImpl implements ManageExceptionsService {

    @Override
    public ManageExceptionsResponse manageExceptions(SupportRequests supportRequests, String clientS2SToken) {
        return null;
    }
}
