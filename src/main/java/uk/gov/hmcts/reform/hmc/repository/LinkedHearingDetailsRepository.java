package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingDetailsEntity;

import java.util.List;

@Transactional(propagation = Propagation.REQUIRES_NEW)
@Repository
public interface LinkedHearingDetailsRepository extends CrudRepository<LinkedHearingDetailsEntity, Long> {

    @Query("from LinkedHearingDetailsEntity lhde where lhde.linkedGroup.linkedGroupId = :linkedGroupId")
    List<LinkedHearingDetailsEntity> getLinkedHearingDetails(Long linkedGroupId);

    default void deleteHearingGroup(Long hearingGroupId) {
        // TODO: implement
    }
}
