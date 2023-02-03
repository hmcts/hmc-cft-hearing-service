package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetailsAudit;

@Transactional(propagation = Propagation.REQUIRED)
@Repository
public interface LinkedGroupDetailsAuditRepository extends CrudRepository<LinkedGroupDetailsAudit, Long> {

    @Modifying
    @Query("DELETE FROM LinkedGroupDetailsAudit lgd WHERE lgd.linkedGroup.id = :groupId AND lgd.linkedGroupVersion = :version")
    void deleteLinkedGroupDetailsAudit(Long groupId, Long version);
}
