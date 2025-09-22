package uk.gov.hmcts.reform.hmc.config;

public interface UrlManager {

    String getUrlHeaderName();

    String getHost();

    String getActualHost();

    void setActualHost(String actualHost);
}
