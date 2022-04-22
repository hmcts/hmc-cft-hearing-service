package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;

@Transactional(propagation = Propagation.REQUIRED)
@Repository
public interface HearingPartyRepository extends JpaRepository<HearingPartyEntity, Long> {

}
