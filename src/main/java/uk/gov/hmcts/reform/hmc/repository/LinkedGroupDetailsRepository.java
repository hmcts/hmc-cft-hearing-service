package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;

import java.util.List;

@Transactional(propagation = Propagation.REQUIRES_NEW)
@Repository
public interface LinkedGroupDetailsRepository extends CrudRepository<LinkedGroupDetails, Long> {

    @Query("from LinkedGroupDetails lgd WHERE lgd.requestId = :requestId ")
    List<LinkedGroupDetails> getLinkedGroupDetailsById(long requestId);
}
