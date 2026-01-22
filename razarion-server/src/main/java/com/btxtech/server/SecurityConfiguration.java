package com.btxtech.server;

import com.btxtech.server.user.UserService;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {
    // private final Logger logger = LoggerFactory.getLogger(SecurityConfiguration.class);
    @Value("${spring.websecurity.debug:false}")
    boolean webSecurityDebug;
    @Value("${jwt.keystore.location}")
    private String keystoreLocation;
    @Value("${jwt.keystore.password}")
    private String keystorePassword;
    @Value("${jwt.key.alias}")
    private String keyAlias;
    @Value("${jwt.key.password}")
    private String keyPassword;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, UserService userService) throws Exception {
        http
                .authorizeHttpRequests((authorize) -> authorize
                        .anyRequest().permitAll()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(Customizer.withDefaults())
                .oauth2ResourceServer((oauth2) -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter(userService)))
                )
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling((exceptions) -> exceptions
                        .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                        .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @SuppressWarnings("Convert2Lambda")
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter(UserService userService) {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // Remove the SCOPE_ prefix
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        // Must be anonymous class or Spring can not handle it
        return new Converter<>() {

            @Override
            public AbstractAuthenticationToken convert(Jwt jwt) {
                String email = (String) jwt.getClaims().get("sub");
                if (userService.shouldCheckRegisteredUser(email)) {
                    if (!userService.registeredUserExists(email)) {
                        throw new UsernameNotFoundException("User no longer exists: " + email);
                    }
                    userService.updateLastCheckedRegisteredUser(email);
                }

                var authorities = grantedAuthoritiesConverter.convert(jwt);
                return new JwtAuthenticationToken(jwt, authorities, email);
            }
        };
    }

    @Bean
    public KeyPair keyPair() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");

        try (InputStream inputStream = getInputStreamFromLocation(keystoreLocation)) {
            keyStore.load(inputStream, keystorePassword.toCharArray());
        }

        Key key = keyStore.getKey(keyAlias, keyPassword.toCharArray());
        if (!(key instanceof RSAPrivateKey privateKey)) {
            throw new IllegalStateException("Not an RSA private key");
        }

        RSAPublicKey publicKey = (RSAPublicKey) keyStore.getCertificate(keyAlias).getPublicKey();
        return new KeyPair(publicKey, privateKey);
    }

    private InputStream getInputStreamFromLocation(String location) throws IOException {
        if (location.startsWith("classpath:")) {
            String path = location.substring("classpath:".length());
            return new ClassPathResource(path).getInputStream();
        } else if (location.startsWith("file:")) {
            String path = location.substring("file:".length());
            return new FileInputStream(path);
        } else {
            return new FileInputStream(location);
        }
    }

    @Bean
    public JwtEncoder jwtEncoder(KeyPair keyPair) {
        JWK jwk = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                .build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    public JwtDecoder jwtDecoder(KeyPair keyPair) {
        return NimbusJwtDecoder.withPublicKey((RSAPublicKey) keyPair.getPublic()).build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.debug(webSecurityDebug);
    }

    }
