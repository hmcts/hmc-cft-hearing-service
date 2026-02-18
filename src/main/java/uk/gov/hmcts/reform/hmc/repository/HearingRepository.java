package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.LinkedGroupDetails;

import java.util.List;

@Transactional(propagation = Propagation.REQUIRED)
@Repository
public interface HearingRepository extends CrudRepository<HearingEntity, Long> {

    @Query("SELECT status from HearingEntity where id = :hearingId")
    String getStatus(Long hearingId);

    @Query("from HearingEntity he where he.linkedGroupDetails.linkedGroupId = :linkedGroupId")
    List<HearingEntity> findByLinkedGroupId(Long linkedGroupId);

    @Query("FROM HearingEntity he WHERE he.linkedGroupDetails.requestId = :requestId")
    List<HearingEntity> findByRequestId(String requestId);

    @Query("FROM HearingEntity he WHERE id in :hearingIds")
    List<HearingEntity> getHearings(List<Long> hearingIds);

    @Modifying
    @Query("UPDATE HearingEntity h "
        + "SET linkedGroupDetails = :linkedGroupDetails, linkedOrder = :linkedOrder WHERE h.id = :hearingId")
    void updateLinkedGroupDetailsAndOrder(Long hearingId, LinkedGroupDetails linkedGroupDetails, Long linkedOrder);

    @Modifying
    @Query("UPDATE HearingEntity h SET linkedGroupDetails = null, linkedOrder = null WHERE h.id = :hearingId")
    void removeLinkedGroupDetailsAndOrder(Long hearingId);
}
