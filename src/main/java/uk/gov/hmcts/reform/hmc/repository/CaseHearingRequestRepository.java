package uk.gov.hmcts.reform.hmc.repository;

public interface CaseHearingRequestRepository {

    Integer getVersionNumber(String hearingId);
}
