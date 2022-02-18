package uk.gov.hmcts.reform.hmc.service;

import java.time.LocalDateTime;
import java.util.List;

public interface PartiesNotifiedService {

    List<LocalDateTime> getPartiesNotified(Long hearingId);

}