package uk.gov.hmcts.reform.hmc.repository;

public interface CaseHearingRequestRepository {

    boolean isValidVersionNumber(Long hearingId);
}
