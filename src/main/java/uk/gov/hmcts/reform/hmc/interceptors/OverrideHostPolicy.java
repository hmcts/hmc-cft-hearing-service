package uk.gov.hmcts.reform.hmc.interceptors;

public interface OverrideHostPolicy {

    boolean isAllowed(String url);

}
