package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;

@Transactional(propagation = Propagation.REQUIRED)
@Repository
public interface LinkedGroupDetailsRepository extends CrudRepository<LinkedGroupDetails, Long> {

    // TODO: implement DB query - https://tools.hmcts.net/jira/browse/HMAN-96
    default void deleteHearingGroup(Long hearingGroupId) {
    }
}
