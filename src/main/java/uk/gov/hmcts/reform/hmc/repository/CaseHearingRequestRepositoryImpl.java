package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.stereotype.Repository;

import uk.gov.hmcts.reform.hmc.data.CaseHearingRequestEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

@Repository
public class CaseHearingRequestRepositoryImpl implements CaseHearingRequestRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public boolean isValidVersionNumber(Long hearingId) {
        TypedQuery<Long> namedQuery = em.createNamedQuery(CaseHearingRequestEntity.GET_VERSION_NUMBER_BY_HEARING_ID, Long.class);
        namedQuery.setParameter("id", hearingId);
        return false;
    }
}
