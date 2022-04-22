package uk.gov.hmcts.reform.hmc.client.futurehearing;

public class AuthenticationRequest {

    private String grantType;

    private String clientId;

    private String scope;

    private String clientSecret;

    public AuthenticationRequest(String grantType, String clientId, String scope, String clientSecret) {
        this.clientSecret = clientSecret;
        this.clientId = clientId;
        this.grantType = grantType;
        this.scope = scope;
    }

    public String getRequest() {
        return "grant_type=" + grantType + "&client_id=" + clientId + "&scope=" + scope + "&client_secret="
            + clientSecret;
    }

}
