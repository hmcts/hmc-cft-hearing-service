package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingDetailsAudit;

public interface LinkedHearingDetailsAuditRepository extends CrudRepository<LinkedHearingDetailsAudit, Long> {
}
