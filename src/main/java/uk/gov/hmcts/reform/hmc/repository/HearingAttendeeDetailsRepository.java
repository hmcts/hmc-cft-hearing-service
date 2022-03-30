package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.HearingAttendeeDetailsEntity;

@Transactional(propagation = Propagation.REQUIRES_NEW)
@Repository
public interface HearingAttendeeDetailsRepository extends JpaRepository<HearingAttendeeDetailsEntity, Long> {

    @Query("select partySubChannelType from HearingAttendeeDetailsEntity hade where hade.partyId = :partyId")
    String getHearingAttendeeByPartyId(String partyId);

}
