package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.LinkedHearingDetails;

import java.util.List;

@Transactional(propagation = Propagation.REQUIRES_NEW)
@Repository
public interface LinkedHearingDetailsRepository extends CrudRepository<LinkedHearingDetails, Long> {

    @Query("from LinkedHearingDetails lhd WHERE lhd.hearing.id = :hearingId ")
    LinkedHearingDetails getLinkedHearingDetailsByHearingId(Long hearingId);

    @Query("from LinkedHearingDetails lhd WHERE lhd.linkedGroup.requestId = :requestId ")
    List<LinkedHearingDetails> getLinkedHearingDetailsByRequestId(String requestId);

}


