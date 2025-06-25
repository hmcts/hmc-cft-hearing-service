package uk.gov.hmcts.reform.hmc.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;
import uk.gov.hmcts.reform.hmc.security.JwtGrantedAuthoritiesConverter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Value("${spring.security.oauth2.client.provider.oidc.issuer-uri}")
    private String issuerUri;

    @Value("${oidc.issuer}")
    private String issuerOverride;

    private final ServiceAuthFilter serviceAuthFilter;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    private static final String[] AUTH_ALLOWED_LIST = {
        "/swagger-resources/**",
        "/swagger-ui/**",
        "/webjars/**",
        "/v3/api-docs",
        "/v3/api-docs/**",
        "/health",
        "/health/liveness",
        "/health/readiness",
        "/info",
        "/favicon.ico",
        "/"
    };

    @Autowired
    public SecurityConfiguration(final ServiceAuthFilter serviceAuthFilter,
                                 final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter) {
        super();
        this.serviceAuthFilter = serviceAuthFilter;
        jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(AUTH_ALLOWED_LIST);
    }    

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .addFilterBefore(serviceAuthFilter, BearerTokenAuthenticationFilter.class)
            .sessionManagement(sm -> sm.sessionCreationPolicy(STATELESS))
            .csrf(csrf -> csrf.disable()) // NOSONAR
            .formLogin(fl -> fl.disable())
            .logout(lg -> lg.disable())
            .authorizeHttpRequests(ahr -> ahr.anyRequest().authenticated())
            .oauth2ResourceServer(oa -> oa.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
            .oauth2Client(Customizer.withDefaults())
            .build();
    }


    @Bean
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder)  JwtDecoders.fromOidcIssuerLocation(issuerUri);
        OAuth2TokenValidator<Jwt> withTimestamp = new JwtTimestampValidator();
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(withTimestamp);
        jwtDecoder.setJwtValidator(validator);
        return jwtDecoder;
    }
}
