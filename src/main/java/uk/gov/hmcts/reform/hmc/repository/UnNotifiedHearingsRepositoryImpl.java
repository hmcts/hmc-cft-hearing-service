package uk.gov.hmcts.reform.hmc.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static uk.gov.hmcts.reform.hmc.constants.Constants.CANCELLED;
import static uk.gov.hmcts.reform.hmc.constants.Constants.UN_NOTIFIED_HEARINGS_LIMIT;

@Slf4j
@Repository
public class UnNotifiedHearingsRepositoryImpl implements UnNotifiedHearingsRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Long> getUnNotifiedHearingsWithOutStartDateTo(String hmctsServiceCode,
                                                              LocalDateTime hearingStartDateFrom,
                                                              List<String> hearingStatus) {
        StringBuilder hqlQuery = getQueryForUnNotifiedHearings(hearingStatus);
        if (null != hearingStatus && hearingStatus.stream().anyMatch(e -> e.equalsIgnoreCase(CANCELLED))) {
            hqlQuery.append("OR hdd.startDateTime IS NULL");
        }

        Query query = em.createQuery(hqlQuery.toString());
        List hearings = query
            .setParameter("hmctsServiceCode", hmctsServiceCode)
            .setParameter("hearingStartDateFrom", hearingStartDateFrom)
            .setMaxResults(UN_NOTIFIED_HEARINGS_LIMIT)
            .getResultList();
        return getHearings(hearings);
    }

    @Override
    public List<Long> getUnNotifiedHearingsWithStartDateTo(String hmctsServiceCode, LocalDateTime hearingStartDateFrom,
                                                           LocalDateTime hearingStartDateTo,
                                                           List<String> hearingStatus) {
        StringBuilder hqlQuery = getQueryForUnNotifiedHearings(hearingStatus);
        hqlQuery.append("AND MAX(hdd.endDateTime) <= :hearingStartDateTo ");
        if (null != hearingStatus && hearingStatus.stream().anyMatch(e -> e.equalsIgnoreCase(CANCELLED))) {
            hqlQuery.append("OR hdd.startDateTime IS NULL");
        }
        Query query = em.createQuery(hqlQuery.toString());
        List hearings = query
            .setParameter("hmctsServiceCode", hmctsServiceCode)
            .setParameter("hearingStartDateFrom", hearingStartDateFrom)
            .setParameter("hearingStartDateTo", hearingStartDateTo)
            .setMaxResults(UN_NOTIFIED_HEARINGS_LIMIT)
            .getResultList();
        return getHearings(hearings);
    }

    private StringBuilder getQueryForUnNotifiedHearings(List<String> hearingStatus) {
        StringBuilder hqlQuery = new StringBuilder("select distinct hr.hearing.id FROM HearingResponseEntity hr "
                                                     + "JOIN CaseHearingRequestEntity csr "
                                                     + "ON hr.hearing.id = csr.hearing.id "
                                                     + "join MaxHearingRequestVersionView mrv "
                                                     + "ON csr.hearing.id = mrv.hearingId "
                                                     + "JOIN HearingDayDetailsEntity hdd ON "
                                                     + "hr.hearingResponseId = hdd.hearingResponse.hearingResponseId "
                                                     + "JOIN HearingEntity he ON hr.hearing.id = he.id "
                                                     + "where csr.hmctsServiceCode = :hmctsServiceCode "
                                                     + "and (hr.requestVersion = mrv.maxHearingRequestVersion");
        hqlQuery.append(") and hr.partiesNotifiedDateTime IS NULL ");
        if (null != hearingStatus && hearingStatus.stream().anyMatch(e -> e.equalsIgnoreCase(CANCELLED))) {
            hqlQuery.append("AND (hdd.startDateTime >= :hearingStartDateFrom OR hdd.startDateTime IS NULL)");
        }
        if (null != hearingStatus && hearingStatus.size() == 1 && hearingStatus.get(0).equalsIgnoreCase(CANCELLED)) {
            hqlQuery.append(" AND he.status = 'CANCELLED'");
        }
        hqlQuery.append(" GROUP BY hr.hearing.id, hdd.startDateTime ");
        hqlQuery.append("HAVING MIN(hdd.startDateTime) >= :hearingStartDateFrom ");
        return hqlQuery;
    }

    private List<Long> getHearings(List hearings) {
        List<Long> hearingsLong = new ArrayList<>();
        Iterator it = hearings.iterator();
        while (it.hasNext()) {
            hearingsLong.add((Long) it.next());
        }
        return hearingsLong;
    }

}
