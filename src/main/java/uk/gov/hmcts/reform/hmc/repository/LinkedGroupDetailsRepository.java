package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;

@Transactional(propagation = Propagation.REQUIRES_NEW)
@Repository
public interface LinkedGroupDetailsRepository extends CrudRepository<LinkedGroupDetails, Long> {

    @Query("select lgd.linkedGroupId from LinkedGroupDetails lgd WHERE lgd.requestId = :requestId ")
    Long isFoundForRequestId(String requestId);

    @Query("from LinkedGroupDetails lgd WHERE lgd.requestId = :requestId ")
    LinkedGroupDetails getLinkedGroupDetailsByRequestId(String requestId);

    @Modifying
    @Query("UPDATE LinkedGroupDetails lgd SET lgd.status = :status WHERE lgd.requestId = :requestId ")
    void updateLinkedGroupDetailsStatus(String requestId, String status);

    @Modifying
    @Query("DELETE FROM LinkedGroupDetails lgd WHERE lgd.requestId = :requestId ")
    void deleteLinkedGroupDetailsStatus(String requestId);

}
