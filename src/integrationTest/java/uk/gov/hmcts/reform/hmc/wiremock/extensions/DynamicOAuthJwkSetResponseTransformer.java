package uk.gov.hmcts.reform.hmc.wiremock.extensions;


import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.nimbusds.jose.JOSEException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import static uk.gov.hmcts.reform.hmc.utils.KeyGenerator.getRsaJwk;

@Slf4j
public class DynamicOAuthJwkSetResponseTransformer extends ResponseTransformer {

    @Override
    public String getName() {
        return "dynamic-oauth-jwk-set-response-transformer";
    }

    @Override
    public Response transform(Request request, Response response, FileSource files, Parameters parameters) {
        try {
            return Response.Builder.like(response)
                .but()
                .body(dynamicResponse(request, response, parameters))
                .build();

        } catch (SecurityException ex) {
            return Response.Builder.like(response)
                .but()
                .status(HttpStatus.UNAUTHORIZED.value())
                .statusMessage(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .build();
        }
    }

    protected String dynamicResponse(Request request, Response response, Parameters parameters) {
        try {
            return "{"
                + "\"keys\": [" + getRsaJwk().toPublicJWK().toJSONString() + "]"
                + "}";

        } catch (JOSEException ex) {
            log.error("Failure running RSA JWK Generator", ex);
        }

        return null;
    }

    @Override
    public boolean applyGlobally() {
        // This flag will ensure this transformer is used only for those request mappings that have the transformer
        // configured
        return false;
    }

}
