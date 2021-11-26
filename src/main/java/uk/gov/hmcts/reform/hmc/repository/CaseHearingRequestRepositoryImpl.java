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
    public Integer getVersionNumber(String hearingId) {
        TypedQuery<Integer> namedQuery = em.createNamedQuery(CaseHearingRequestEntity.GET_VERSION_NUMBER_BY_HEARING_ID,
                                                             Integer.class);
        namedQuery.setParameter("id", hearingId);
        return namedQuery.getSingleResult();
    }
}
