package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetailsAudit;

public interface LinkedGroupDetailsAuditRepository extends CrudRepository<LinkedGroupDetailsAudit, Long> {
}
