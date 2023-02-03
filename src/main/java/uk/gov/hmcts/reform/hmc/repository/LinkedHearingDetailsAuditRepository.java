package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingDetailsAudit;

@Transactional(propagation = Propagation.REQUIRED)
@Repository
public interface LinkedHearingDetailsAuditRepository extends CrudRepository<LinkedHearingDetailsAudit, Long> {

    @Modifying
    @Query("DELETE FROM LinkedHearingDetailsAudit lgd WHERE lgd.linkedGroup.id = :groupId AND lgd.linkedGroupVersion = :version")
    void deleteLinkedHearingsDetailsAudit(Long groupId, Long version);
}
